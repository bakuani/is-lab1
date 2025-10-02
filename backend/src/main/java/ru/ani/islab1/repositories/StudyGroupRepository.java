package ru.ani.islab1.repositories;

import jakarta.persistence.criteria.CriteriaBuilder;
import ru.ani.islab1.models.Coordinates;
import ru.ani.islab1.models.Location;
import ru.ani.islab1.models.Person;
import ru.ani.islab1.models.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Integer> {
    boolean existsByCoordinatesAndIdNot(Coordinates coordinates, Integer id);

    boolean existsByGroupAdminAndIdNot(Person admin, Integer id);
}