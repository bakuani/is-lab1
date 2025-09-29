package ru.ani.islab1.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ani.islab1.exceptions.ResourceNotFoundException;
import ru.ani.islab1.models.Coordinates;
import ru.ani.islab1.models.Location;
import ru.ani.islab1.models.Person;
import ru.ani.islab1.models.StudyGroup;
import ru.ani.islab1.repositories.CoordinatesRepository;
import ru.ani.islab1.repositories.LocationRepository;
import ru.ani.islab1.repositories.PersonRepository;
import ru.ani.islab1.repositories.StudyGroupRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository repository;
    private final CoordinatesRepository coordinatesRepository;
    private final PersonRepository personRepository;
    private final LocationRepository locationRepository;

    @Transactional
    public StudyGroup create(StudyGroup sg) {
        processCoordinatesForCreate(sg);
        processGroupAdminForCreate(sg);
        sg.setCreationDate(new Date());
        return repository.save(sg);
    }

    private void processCoordinatesForCreate(StudyGroup sg) {
        if (sg.getCoordinates() == null) {
            return;
        }

        Coordinates incoming = sg.getCoordinates();
        incoming.setId(null);

        Double x = incoming.getX();
        Double y = incoming.getY();

        if (x == null || y == null) {
            throw new IllegalArgumentException("Coordinates must contain both x and y");
        }

        Optional<Coordinates> found = coordinatesRepository.findByXAndY(x, y);
        if (found.isPresent()) {
            sg.setCoordinates(found.get());
        } else {
            Coordinates toSave = new Coordinates();
            toSave.setX(x);
            toSave.setY(y);
            Coordinates saved = coordinatesRepository.save(toSave);
            sg.setCoordinates(saved);
        }
    }

    private void processGroupAdminForCreate(StudyGroup sg) {
        if (sg.getGroupAdmin() == null) {
            return;
        }

        Person incomingAdmin = sg.getGroupAdmin();

        if (incomingAdmin.getId() != null) {
            Person managedAdmin = mergePerson(incomingAdmin);
            sg.setGroupAdmin(managedAdmin);
            return;
        }

        if (incomingAdmin.getLocation() != null) {
            Location loc = incomingAdmin.getLocation();
            if (loc.getId() != null) {
                Location managedLoc = locationRepository.findById(loc.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Location not found with id " + loc.getId()));
                if (loc.getName() != null) managedLoc.setName(loc.getName());
                if (loc.getX() != null) managedLoc.setX(loc.getX());
                if (loc.getY() != null) managedLoc.setY(loc.getY());
                incomingAdmin.setLocation(managedLoc);
            } else {
                Location savedLoc = locationRepository.save(new Location(null, loc.getX(), loc.getY(), loc.getName()));
                incomingAdmin.setLocation(savedLoc);
            }
        }

        Person savedPerson = personRepository.save(incomingAdmin);
        sg.setGroupAdmin(savedPerson);
    }

    private Person mergePerson(Person incoming) {
        Integer id = incoming.getId();
        Person managed = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id " + id));

        if (incoming.getName() != null) managed.setName(incoming.getName());
        if (incoming.getEyeColor() != null) managed.setEyeColor(incoming.getEyeColor());
        if (incoming.getHairColor() != null) managed.setHairColor(incoming.getHairColor());
        if (incoming.getWeight() != null) managed.setWeight(incoming.getWeight());
        if (incoming.getNationality() != null) managed.setNationality(incoming.getNationality());

        if (incoming.getLocation() != null) {
            Location locIncoming = incoming.getLocation();
            if (locIncoming.getId() != null) {
                Location managedLoc = locationRepository.findById(locIncoming.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Location not found with id " + locIncoming.getId()));
                if (locIncoming.getName() != null) managedLoc.setName(locIncoming.getName());
                if (locIncoming.getX() != null) managedLoc.setX(locIncoming.getX());
                if (locIncoming.getY() != null) managedLoc.setY(locIncoming.getY());
                managed.setLocation(managedLoc);
            } else {
                Location savedLoc = locationRepository.save(new Location(null, locIncoming.getX(), locIncoming.getY(), locIncoming.getName()));
                managed.setLocation(savedLoc);
            }
        }

        return personRepository.save(managed);
    }

    @Transactional
    public StudyGroup update(Integer id, StudyGroup updated) {
        StudyGroup existing = getById(id);

        if (updated.getName() != null) existing.setName(updated.getName());

        if (updated.getCoordinates() != null && updated.getCoordinates().getId() != null) {
            Coordinates managedCoords = coordinatesRepository.findById(updated.getCoordinates().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Coordinates not found with id " + updated.getCoordinates().getId()));
            existing.setCoordinates(managedCoords);
        } else if (updated.getCoordinates() != null) {
            existing.setCoordinates(updated.getCoordinates());
        }

        if (updated.getGroupAdmin() != null && updated.getGroupAdmin().getId() != null) {
            Person mergedAdmin = mergePerson(updated.getGroupAdmin());
            existing.setGroupAdmin(mergedAdmin);
        } else if (updated.getGroupAdmin() != null) {
            existing.setGroupAdmin(updated.getGroupAdmin());
        }

        existing.setStudentsCount(updated.getStudentsCount());
        existing.setExpelledStudents(updated.getExpelledStudents());
        existing.setTransferredStudents(updated.getTransferredStudents());
        existing.setFormOfEducation(updated.getFormOfEducation());
        existing.setShouldBeExpelled(updated.getShouldBeExpelled());
        existing.setAverageMark(updated.getAverageMark());
        existing.setSemesterEnum(updated.getSemesterEnum());

        return repository.save(existing);
    }

    public StudyGroup getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StudyGroup not found with id " + id));
    }

    public List<StudyGroup> getAll() {
        return repository.findAll();
    }

    @Transactional
    public void delete(Integer id) {
        StudyGroup sg = getById(id);
        repository.delete(sg);
    }
}
