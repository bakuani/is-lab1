package ru.ani.islab1.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.ani.islab1.exceptions.ResourceNotFoundException;
import ru.ani.islab1.models.StudyGroup;
import ru.ani.islab1.models.enums.FormOfEducation;
import ru.ani.islab1.models.enums.Semester;
import ru.ani.islab1.services.StudyGroupService;
import jakarta.validation.constraints.*;


import java.util.List;

@RestController
@RequestMapping("/api/special")
@RequiredArgsConstructor
@Validated
public class SpecialController {

    private final StudyGroupService service;

    @GetMapping("/count-by-semester")
    public ResponseEntity<Long> countBySemester(@RequestParam("semester") Semester semester) {
        if (semester == null) {
            return ResponseEntity.badRequest().build();
        }
        long cnt = service.countBySemesterLessThan(semester);
        return ResponseEntity.ok(cnt);
    }

    @GetMapping("/groups-with-admin-less-than")
    public ResponseEntity<List<StudyGroup>> groupsWithAdminLess(@RequestParam("adminId") Integer adminId) {
        if (adminId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<StudyGroup> list = service.groupsWithAdminIdLessThan(adminId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/distinct-should-be-expelled")
    public ResponseEntity<List<Long>> distinctShouldBeExpelled() {
        List<Long> vals = service.distinctShouldBeExpelled();
        return ResponseEntity.ok(vals);
    }

    @PostMapping("/studygroups/{id}/add-student")
    public ResponseEntity<?> addStudentToGroup(@PathVariable("id") Integer id) {
        try {
            StudyGroup updated = service.addStudentToGroup(id);
            return ResponseEntity.ok(updated);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(ex.getMessage());
        }

    }

    public static class ChangeFormDto {
        @NotBlank
        public String form;
    }

    @PostMapping("/studygroups/{id}/change-form")
    public ResponseEntity<StudyGroup> changeFormOfGroup(@PathVariable("id") Integer id,
                                                        @Valid @RequestBody ChangeFormDto dto) {
        try {
            FormOfEducation newForm = FormOfEducation.valueOf(dto.form);
            StudyGroup updated = service.changeFormOfEducation(id, newForm);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            return ResponseEntity.status(500).build();
        }
    }
}
