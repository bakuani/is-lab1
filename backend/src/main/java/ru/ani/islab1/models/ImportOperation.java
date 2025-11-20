package ru.ani.islab1.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.ani.islab1.models.enums.ImportStatus;

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

    @Column(nullable = false)
    private String userName;

    private Integer importedCount;
    
    @Column(length = 2048) 
    private String errorMessage;
}