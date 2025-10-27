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

    @NotBlank(message = "Название группы обязательно")
    private String name;

    @NotNull
    @Valid
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "coordinates_id")
    private Coordinates coordinates;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());

    @NotNull
    @Min(value = 1, message = "Students Count должно быть ≥ 1")
    private Long studentsCount;

    @NotNull
    @Min(value = 0, message = "Expelled Students должно быть ≥ 0")
    private Integer expelledStudents;

    @NotNull
    @Min(value = 0, message = "Transferred Students должно быть ≥ 0")
    private Long transferredStudents;

    @NotNull(message = "Form of Education обязательно")
    @Enumerated(EnumType.STRING)
    private FormOfEducation formOfEducation;

    @NotNull
    @Min(value = 1, message = "Should Be Expelled должно быть ≥ 1")
    private Long shouldBeExpelled;

    @NotNull
    @Min(value = 1, message = "Average Mark должно быть ≥ 1")
    private Long averageMark;

    @Enumerated(EnumType.STRING)
    private Semester semesterEnum;

    @NotNull
    @Valid
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "group_admin_id")
    private Person groupAdmin;
}
