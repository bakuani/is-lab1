package ru.ani.islab1.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.ani.islab1.models.enums.FormOfEducation;
import ru.ani.islab1.models.enums.Semester;

@Data
public class StudyGroupDto {
    @NotBlank
    private String name;

    @NotNull
    private Integer coordinatesId; // существующий объект

    @NotNull
    private Integer groupAdminId;  // существующий объект

    @Min(1)
    private long studentsCount;

    @NotNull
    @Min(0)
    private Integer expelledStudents;

    @Min(0)
    private long transferredStudents;

    @NotNull
    private FormOfEducation formOfEducation;

    // nullable
    private Long shouldBeExpelled;

    @Min(0)
    private long averageMark;

    private Semester semesterEnum;
}
