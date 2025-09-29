package ru.ani.islab1.repositories;

import ru.ani.islab1.models.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Integer> {
}