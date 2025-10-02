package ru.ani.islab1.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ani.islab1.exceptions.CannotDeleteStudyGroupException;
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
@Transactional
public class StudyGroupService {

    private final StudyGroupRepository repository;
    private final CoordinatesRepository coordinatesRepository;
    private final PersonRepository personRepository;
    private final LocationRepository locationRepository;

    public StudyGroup create(StudyGroup sg) {
        sg.setCoordinates(findOrCreateCoordinates(sg.getCoordinates()));
        sg.setGroupAdmin(findOrCreatePerson(sg.getGroupAdmin()));
        sg.setCreationDate(new Date());
        return repository.save(sg);
    }

    public StudyGroup update(Integer id, StudyGroup updated) {
        StudyGroup existing = getById(id);

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getCoordinates() != null)
            existing.setCoordinates(findOrCreateCoordinates(updated.getCoordinates()));
        if (updated.getGroupAdmin() != null)
            existing.setGroupAdmin(findOrCreatePerson(updated.getGroupAdmin()));

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

    public void delete(Integer id) {
            StudyGroup group = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("StudyGroup not found"));

            if (repository.existsByCoordinatesAndIdNot(group.getCoordinates(), id)) {
                throw new CannotDeleteStudyGroupException(
                        "Cannot delete: Coordinates are used by another study group"
                );
            }

            if (group.getGroupAdmin().getLocation() != null &&
                    personRepository.existsByLocationAndIdNot(group.getGroupAdmin().getLocation(), id)) {
                throw new CannotDeleteStudyGroupException(
                        "Cannot delete: Location is used by another person"
                );
            }

            if (group.getGroupAdmin() != null &&
                    repository.existsByGroupAdminAndIdNot(group.getGroupAdmin(), id)) {
                throw new CannotDeleteStudyGroupException(
                        "Cannot delete: Admin is assigned to another study group"
                );
            }

            repository.delete(group);
    }


    private Coordinates findOrCreateCoordinates(Coordinates incoming) {
        if (incoming == null) return null;

        Double x = incoming.getX();
        Double y = incoming.getY();

        if (x == null || y == null)
            throw new IllegalArgumentException("Coordinates must contain both x and y");

        return coordinatesRepository.findByXAndY(x, y)
                .orElseGet(() -> coordinatesRepository.save(new Coordinates(null, x, y)));
    }

    private Location findOrCreateLocation(Location loc) {
        if (loc == null) return null;

        Long x = loc.getX();
        Double y = loc.getY();
        String name = loc.getName();

        if (x == null || y == null || name == null)
            throw new IllegalArgumentException("Location must contain x, y and name");

        return locationRepository.findByXAndYAndName(x, y, name)
                .orElseGet(() -> locationRepository.save(new Location(null, x, y, name)));
    }

    private Person findOrCreatePerson(Person incoming) {
        if (incoming == null) return null;

        Location loc = findOrCreateLocation(incoming.getLocation());
        incoming.setLocation(loc);

        if (incoming.getId() != null) {
            return mergePerson(incoming);
        }

        Optional<Person> existing = personRepository
                .findByNameAndWeightAndEyeColorAndHairColorAndNationalityAndLocationXAndLocationYAndLocationName(
                        incoming.getName(),
                        incoming.getWeight(),
                        incoming.getEyeColor(),
                        incoming.getHairColor(),
                        incoming.getNationality(),
                        loc.getX(),
                        loc.getY(),
                        loc.getName()
                );

        return existing.orElseGet(() -> personRepository.save(incoming));
    }

    private Person mergePerson(Person incoming) {
        if (incoming.getId() == null)
            throw new IllegalArgumentException("Person id must not be null for merge");

        Person managed = personRepository.findById(incoming.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id " + incoming.getId()));

        if (incoming.getName() != null) managed.setName(incoming.getName());
        if (incoming.getWeight() != null) managed.setWeight(incoming.getWeight());
        if (incoming.getEyeColor() != null) managed.setEyeColor(incoming.getEyeColor());
        if (incoming.getHairColor() != null) managed.setHairColor(incoming.getHairColor());
        if (incoming.getNationality() != null) managed.setNationality(incoming.getNationality());

        if (incoming.getLocation() != null) {
            Location locIncoming = incoming.getLocation();
            Location managedLoc;
            if (locIncoming.getId() != null) {
                managedLoc = locationRepository.findById(locIncoming.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Location not found with id " + locIncoming.getId()));
                if (locIncoming.getX() != null) managedLoc.setX(locIncoming.getX());
                if (locIncoming.getY() != null) managedLoc.setY(locIncoming.getY());
                if (locIncoming.getName() != null) managedLoc.setName(locIncoming.getName());
            } else {
                managedLoc = findOrCreateLocation(locIncoming);
            }
            managed.setLocation(managedLoc);
        }

        return personRepository.save(managed);
    }
}
