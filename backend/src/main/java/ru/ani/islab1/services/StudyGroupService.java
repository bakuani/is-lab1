package ru.ani.islab1.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.ani.islab1.exceptions.CannotDeleteStudyGroupException;
import ru.ani.islab1.exceptions.DuplicateEntityException;
import ru.ani.islab1.exceptions.ErrorMessages;
import ru.ani.islab1.exceptions.ImportProcessException;
import ru.ani.islab1.exceptions.ResourceNotFoundException;
import ru.ani.islab1.models.Coordinates;
import ru.ani.islab1.models.Location;
import ru.ani.islab1.models.Person;
import ru.ani.islab1.models.StudyGroup;
import ru.ani.islab1.models.enums.FormOfEducation;
import ru.ani.islab1.models.enums.Semester;
import ru.ani.islab1.repositories.CoordinatesRepository;
import ru.ani.islab1.repositories.LocationRepository;
import ru.ani.islab1.repositories.PersonRepository;
import ru.ani.islab1.repositories.StudyGroupRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository repository;
    private final CoordinatesRepository coordinatesRepository;
    private final PersonRepository personRepository;
    private final LocationRepository locationRepository;
    private final ImportHistoryService importHistoryService;
    private final MinioService minioService;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public List<StudyGroup> importGroups(List<StudyGroup> studyGroups) {
        if (studyGroups == null || studyGroups.isEmpty()) {
            return List.of();
        }

        List<StudyGroup> savedGroups = new ArrayList<>();

        for (StudyGroup sg : studyGroups) {
            sg.setId(null);
            if (sg.getCoordinates() != null) {
                sg.getCoordinates().setId(null);
            }
            if (sg.getGroupAdmin() != null) {
                sg.getGroupAdmin().setId(null);
                if (sg.getGroupAdmin().getLocation() != null) {
                    sg.getGroupAdmin().getLocation().setId(null);
                }
            }
            savedGroups.add(this.create(sg));
        }

        return savedGroups;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(
            retryFor = { ConcurrencyFailureException.class, CannotAcquireLockException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public StudyGroup create(StudyGroup sg) {
        checkUniqueStudyGroup(sg.getName(), sg.getFormOfEducation(), null);
        sg.setCoordinates(findOrCreateCoordinates(sg.getCoordinates()));
        sg.setGroupAdmin(processPersonWithConstraint(sg.getGroupAdmin()));
        sg.setCreationDate(new Date());
        return repository.save(sg);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public StudyGroup update(Integer id, StudyGroup updated) {
        StudyGroup existing = getById(id);

        String effectiveName = updated.getName() != null ? updated.getName() : existing.getName();
        FormOfEducation effectiveForm = updated.getFormOfEducation() != null ? updated.getFormOfEducation() : existing.getFormOfEducation();

        checkUniqueStudyGroup(effectiveName, effectiveForm, id);

        if (updated.getName() != null) {
            existing.setName(updated.getName());
        }
        if (updated.getStudentsCount() != null) {
            existing.setStudentsCount(updated.getStudentsCount());
        }
        if (updated.getExpelledStudents() != null) {
            existing.setExpelledStudents(updated.getExpelledStudents());
        }
        if (updated.getTransferredStudents() != null) {
            existing.setTransferredStudents(updated.getTransferredStudents());
        }
        if (updated.getFormOfEducation() != null) {
            existing.setFormOfEducation(updated.getFormOfEducation());
        }
        if (updated.getShouldBeExpelled() != null) {
            existing.setShouldBeExpelled(updated.getShouldBeExpelled());
        }
        if (updated.getAverageMark() != null) {
            existing.setAverageMark(updated.getAverageMark());
        }

        if (updated.getSemesterEnum() != null) {
            existing.setSemesterEnum(updated.getSemesterEnum());
        }

        if (updated.getCoordinates() != null) {
            existing.setCoordinates(findOrCreateCoordinates(updated.getCoordinates()));
        }

        if (updated.getGroupAdmin() != null) {
            existing.setGroupAdmin(processPersonWithConstraint(updated.getGroupAdmin()));
        }

        return repository.save(existing);
    }

    @Transactional(readOnly = true)
    public StudyGroup getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.STUDYGROUP_NOT_FOUND.format(id)));
    }

    @Transactional(readOnly = true)
    public List<StudyGroup> getAll() {
        return repository.findAll();
    }

    @Transactional
    public void delete(Integer id) {
        StudyGroup group = repository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.ID_NULL.format()));

        if (repository.existsByCoordinatesAndIdNot(group.getCoordinates(), id)) {
            throw new CannotDeleteStudyGroupException(ErrorMessages.COORDINATES_IN_USE.format());
        }

        if (group.getGroupAdmin() != null && group.getGroupAdmin().getLocation() != null &&
                personRepository.existsByLocationAndIdNot(group.getGroupAdmin().getLocation(), group.getGroupAdmin().getId())) {
            throw new CannotDeleteStudyGroupException(ErrorMessages.LOCATION_IN_USE.format());
        }

        if (group.getGroupAdmin() != null &&
                repository.existsByGroupAdminAndIdNot(group.getGroupAdmin(), id)) {
            throw new CannotDeleteStudyGroupException(ErrorMessages.GROUP_ADMIN_USED.format());
        }

        repository.delete(group);
    }

    @Transactional
    public StudyGroup addStudentToGroup(Integer groupId) {
        StudyGroup group = repository.findByIdWithLock(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.STUDYGROUP_NOT_FOUND.format(groupId)));

        Long studentsCount = group.getStudentsCount() == null ? 0L : group.getStudentsCount();
        group.setStudentsCount(studentsCount + 1);

        return repository.save(group);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public StudyGroup changeFormOfEducation(Integer groupId, FormOfEducation newForm) {
        StudyGroup group = repository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.STUDYGROUP_NOT_FOUND.format(groupId)));

        checkUniqueStudyGroup(group.getName(), newForm, group.getId());

        group.setFormOfEducation(newForm);
        return repository.save(group);
    }

    @Transactional(readOnly = true)
    public long countBySemesterLessThan(Semester semester) {
        if (semester == null) {
            return 0;
        }

        return repository.findAll().stream()
                .filter(g -> g.getSemesterEnum() != null)
                .filter(g -> g.getSemesterEnum().ordinal() < semester.ordinal())
                .count();
    }

    @Transactional(readOnly = true)
    public List<StudyGroup> groupsWithAdminIdLessThan(Integer adminId) {
        if (adminId == null) {
            return List.of();
        }

        return repository.findByGroupAdmin_IdLessThan(adminId);
    }

    @Transactional(readOnly = true)
    public List<Long> distinctShouldBeExpelled() {
        return repository.findDistinctShouldBeExpelled();
    }

    private void checkUniqueStudyGroup(String name, FormOfEducation form, Integer excludeId) {
        boolean exists;
        if (excludeId == null) {
            exists = repository.existsByNameAndFormOfEducation(name, form);
        } else {
            exists = repository.existsByNameAndFormOfEducationAndIdNot(name, form, excludeId);
        }

        if (exists) {
            throw new DuplicateEntityException(
                    String.format("Группа с названием '%s' и формой обучения '%s' уже существует.", name, form)
            );
        }
    }

    private Person processPersonWithConstraint(Person incoming) {
        if (incoming == null) {
            return null;
        }

        if (incoming.getId() != null) {
            return mergePerson(incoming);
        }

        Location loc = findOrCreateLocation(incoming.getLocation());
        incoming.setLocation(loc);

        boolean duplicateExists = personRepository.existsByNameAndLocation_XAndLocation_YAndLocation_Name(
                incoming.getName(),
                loc.getX(),
                loc.getY(),
                loc.getName()
        );

        if (duplicateExists) {
            throw new DuplicateEntityException(
                    String.format("Человек с именем '%s', проживающий по адресу '%s' (X:%d, Y:%f), уже существует.",
                            incoming.getName(), loc.getName(), loc.getX(), loc.getY())
            );
        }

        return personRepository.save(incoming);
    }

    private Person mergePerson(Person incoming) {
        if (incoming.getId() == null) {
            throw new IllegalArgumentException(ErrorMessages.PERSON_ID_NULL.format());
        }

        Person managed = personRepository.findByIdWithLock(incoming.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.PERSON_NOT_FOUND.format(incoming.getId())));

        String targetName = incoming.getName() != null ? incoming.getName() : managed.getName();

        Location targetLocation = managed.getLocation();
        if (incoming.getLocation() != null) {
            if (incoming.getLocation().getId() != null) {
                targetLocation = locationRepository.findById(incoming.getLocation().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.LOCATION_NOT_FOUND.format(incoming.getLocation().getId())));
            } else {
                targetLocation = findOrCreateLocation(incoming.getLocation());
            }
        }

        boolean duplicateExists = personRepository.existsByNameAndLocation_XAndLocation_YAndLocation_NameAndIdNot(
                targetName,
                targetLocation.getX(),
                targetLocation.getY(),
                targetLocation.getName(),
                managed.getId()
        );

        if (duplicateExists) {
            throw new DuplicateEntityException("Невозможно обновить Person: человек с таким именем и адресом уже существует.");
        }

        if (incoming.getName() != null) {
            managed.setName(incoming.getName());
        }

        if (incoming.getWeight() != null) {
            managed.setWeight(incoming.getWeight());
        }

        if (incoming.getEyeColor() != null) {
            managed.setEyeColor(incoming.getEyeColor());
        }

        if (incoming.getHairColor() != null) {
            managed.setHairColor(incoming.getHairColor());
        }

        if (incoming.getNationality() != null) {
            managed.setNationality(incoming.getNationality());
        }

        managed.setLocation(targetLocation);

        return personRepository.save(managed);
    }

    private Coordinates findOrCreateCoordinates(Coordinates incoming) {
        if (incoming == null) {
            return null;
        }

        Double x = incoming.getX();
        Double y = incoming.getY();

        if (x == null || y == null) {
            throw new IllegalArgumentException(ErrorMessages.COORDINATES_MISSING.format());
        }

        return coordinatesRepository.findByXAndY(x, y)
                .orElseGet(() -> coordinatesRepository.save(new Coordinates(null, x, y)));
    }

    private Location findOrCreateLocation(Location loc) {
        if (loc == null) {
            return null;
        }

        Long x = loc.getX();
        Double y = loc.getY();
        String name = loc.getName();

        if (x == null || y == null || name == null) {
            throw new IllegalArgumentException(ErrorMessages.LOCATION_INVALID.format());
        }

        return locationRepository.findByXAndYAndName(x, y, name)
                .orElseGet(() -> locationRepository.save(new Location(null, x, y, name)));
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public int importGroupsWithFile(List<StudyGroup> groups, MultipartFile file, String userName) {
        String txId = java.util.UUID.randomUUID().toString();
        String pendingKey = txId + ".pending-" + file.getOriginalFilename();
        String finalKey = txId + "-" + file.getOriginalFilename();

        importHistoryService.logPrepared(txId, userName, pendingKey);

        try {
            minioService.uploadFile(pendingKey, file.getInputStream(), file.getContentType());
        } catch (Exception e) {
            String reason = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
            importHistoryService.markRolledBack(txId, reason);
            throw new ImportProcessException("Failed to upload file to MinIO: " + reason, e);
        }

        try {
            int savedCount = processDbImport(groups);

            minioService.copyFile(pendingKey, finalKey);
            minioService.deleteFile(pendingKey);

            importHistoryService.markCommitted(txId, userName, savedCount, finalKey);
            return savedCount;
        } catch (Exception e) {
            safeDeleteQuiet(finalKey);
            safeDeleteQuiet(pendingKey);

            String reason = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
            importHistoryService.markRolledBack(txId, reason);

            throw new ImportProcessException("Distributed import failed, rolled back: " + reason, e);
        }
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public int processDbImport(List<StudyGroup> groups) {
        List<StudyGroup> saved = this.importGroups(groups);
        return saved.size();
    }

    private void safeDeleteQuiet(String objectKey) {
        try {
            minioService.deleteFile(objectKey);
        } catch (Exception ignored) {
        }
    }
}