package ru.ani.islab1.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ani.islab1.models.StudyGroup;
import ru.ani.islab1.services.ImportHistoryService;
import ru.ani.islab1.services.StudyGroupService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/studygroups")
@RequiredArgsConstructor
@Validated
@Slf4j
public class StudyGroupController {

    private final StudyGroupService service;
    private final ImportHistoryService importHistoryService;
    private final ObjectMapper objectMapper;


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

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importGroups(@RequestParam("file") MultipartFile file) {

        final String DUMMY_USER = "system-user";

        try {
            List<StudyGroup> studyGroups;
            try (InputStream inputStream = file.getInputStream()) {
                studyGroups = objectMapper.readValue(
                        inputStream,
                        new TypeReference<List<StudyGroup>>(){}
                );
            }

            int count = service.importGroupsWithFile(studyGroups, file, DUMMY_USER);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("imported", count));

        } catch (Exception e) {
            log.error("Import failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Import failed"));
        }
    }
}