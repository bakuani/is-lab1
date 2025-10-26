package ru.ani.islab1.repositories;

import org.springframework.data.jpa.repository.Query;
import ru.ani.islab1.models.Coordinates;
import ru.ani.islab1.models.Person;
import ru.ani.islab1.models.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Integer> {
    boolean existsByCoordinatesAndIdNot(Coordinates coordinates, Integer id);

    boolean existsByGroupAdminAndIdNot(Person admin, Integer id);

    List<StudyGroup> findByGroupAdmin_IdLessThan(Integer adminId);

    @Query("select distinct sg.shouldBeExpelled from StudyGroup sg where sg.shouldBeExpelled is not null")
    List<Long> findDistinctShouldBeExpelled();
}