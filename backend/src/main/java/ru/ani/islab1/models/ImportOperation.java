package ru.ani.islab1.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.ani.islab1.models.enums.ImportStatus;
import ru.ani.islab1.models.enums.ImportPhase;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_operation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp 
    private LocalDateTime operationTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportPhase phase;

    @Column(length = 64)
    private String txId;

    @Column(nullable = false)
    private String userName;

    private Integer importedCount;
    
    @Column(length = 2048) 
    private String errorMessage;

    @Column(length = 255)
    private String minioFileKey;
}