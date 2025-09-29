package ru.ani.islab1.models;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.ani.islab1.models.enums.FormOfEducation;
import ru.ani.islab1.models.enums.Semester;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "study_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull @NotBlank
    private String name;

    @NotNull
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "coordinates_id")
    private Coordinates coordinates;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());


    @Min(1)
    private long studentsCount;

    @NotNull
    @Min(1)
    private Integer expelledStudents;

    @Min(1)
    private long transferredStudents;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FormOfEducation formOfEducation;

    @Min(1)
    private Long shouldBeExpelled; // nullable

    @Min(1)
    private long averageMark;

    @Enumerated(EnumType.STRING)
    private Semester semesterEnum; // nullable

    @NotNull
    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "group_admin_id")
    private Person groupAdmin;
}