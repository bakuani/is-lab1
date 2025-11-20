package ru.ani.islab1.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.ani.islab1.models.StudyGroup;
import ru.ani.islab1.services.ImportHistoryService;
import ru.ani.islab1.services.StudyGroupService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/studygroups")
@RequiredArgsConstructor
@Validated
public class StudyGroupController {

    private final StudyGroupService service;
    private final ImportHistoryService importHistoryService;


    @PostMapping
    public ResponseEntity<StudyGroup> create(@Valid @RequestBody StudyGroup studyGroup) {
        StudyGroup created = service.create(studyGroup);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<StudyGroup>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyGroup> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyGroup> update(@PathVariable Integer id,
                                             @Valid @RequestBody StudyGroup studyGroup) {
        StudyGroup updated = service.update(id, studyGroup);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<?> importGroups(@Valid @RequestBody List<StudyGroup> studyGroups) {
        
        final String DUMMY_USER = "system-user";

        try {
            List<StudyGroup> imported = service.importGroups(studyGroups);

            importHistoryService.logSuccess(DUMMY_USER, imported.size());

            return ResponseEntity.status(HttpStatus.CREATED).body(imported);

        } catch (Exception e) {
            importHistoryService.logFailure(DUMMY_USER, e.getMessage());

            Map<String, String> body = new HashMap<>();
            body.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }
}