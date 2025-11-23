package ru.ani.islab1.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import ru.ani.islab1.models.Coordinates;
import ru.ani.islab1.models.Person;
import ru.ani.islab1.models.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ani.islab1.models.enums.FormOfEducation;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Integer> {
    boolean existsByCoordinatesAndIdNot(Coordinates coordinates, Integer id);

    boolean existsByGroupAdminAndIdNot(Person admin, Integer id);

    List<StudyGroup> findByGroupAdmin_IdLessThan(Integer adminId);

    @Query("SELECT DISTINCT s.shouldBeExpelled FROM StudyGroup s")
    List<Long> findDistinctShouldBeExpelled();

    boolean existsByNameAndFormOfEducation(String name, FormOfEducation formOfEducation);

    boolean existsByNameAndFormOfEducationAndIdNot(String name, FormOfEducation formOfEducation, Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StudyGroup s WHERE s.id = :id")
    Optional<StudyGroup> findByIdWithLock(Integer id);
}