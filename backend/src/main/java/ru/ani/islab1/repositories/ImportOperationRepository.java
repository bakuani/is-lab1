package ru.ani.islab1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ani.islab1.models.ImportOperation;

import java.util.List;

public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {
    
    List<ImportOperation> findAllByOrderByOperationTimeDesc();
}