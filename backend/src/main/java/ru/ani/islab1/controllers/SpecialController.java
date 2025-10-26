package ru.ani.islab1.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.ani.islab1.models.StudyGroup;
import ru.ani.islab1.models.enums.FormOfEducation;
import ru.ani.islab1.models.enums.Semester;
import ru.ani.islab1.services.StudyGroupService;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@RestController
@RequestMapping("/api/special")
@RequiredArgsConstructor
@Validated
public class SpecialController {

    private final StudyGroupService service;

    @GetMapping("/count-by-semester")
    public ResponseEntity<Long> countBySemester(@RequestParam(value = "semester") @NotNull Semester semester) {
        long cnt = service.countBySemesterLessThan(semester);
        return ResponseEntity.ok(cnt);
    }

    @GetMapping("/groups-with-admin-less-than")
    public ResponseEntity<List<StudyGroup>> groupsWithAdminLess(@RequestParam("adminId") @Positive Integer adminId) {
        List<StudyGroup> list = service.groupsWithAdminIdLessThan(adminId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/distinct-should-be-expelled")
    public ResponseEntity<List<Long>> distinctShouldBeExpelled() {
        List<Long> vals = service.distinctShouldBeExpelled();
        return ResponseEntity.ok(vals);
    }

    @PostMapping("/studygroups/{id}/add-student")
    public ResponseEntity<?> addStudentToGroup(@PathVariable("id") @NotNull @Positive Integer id) {
        StudyGroup updated = service.addStudentToGroup(id);
        return ResponseEntity.ok(updated);
    }

    public static class ChangeFormDto {
        @NotBlank
        public String form;
    }

    @PostMapping("/studygroups/{id}/change-form")
    public ResponseEntity<StudyGroup> changeFormOfGroup(@PathVariable("id") @NotNull @Positive Integer id,
                                                        @Valid @RequestBody ChangeFormDto dto) {
        FormOfEducation newForm = FormOfEducation.valueOf(dto.form);
        StudyGroup updated = service.changeFormOfEducation(id, newForm);
        return ResponseEntity.ok(updated);
    }
}