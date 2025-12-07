package ru.ani.islab1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ani.islab1.models.ImportOperation;
import ru.ani.islab1.models.enums.ImportStatus;
import ru.ani.islab1.services.ImportHistoryService;
import ru.ani.islab1.services.MinioService;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/import-history")
@RequiredArgsConstructor
public class ImportHistoryController {

    private final ImportHistoryService importHistoryService;
    private final MinioService minioService;

    @GetMapping
    public ResponseEntity<List<ImportOperation>> getHistory() {
        return ResponseEntity.ok(importHistoryService.getHistory());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<InputStreamResource> downloadImportFile(@PathVariable Long id) {

        ImportOperation op = importHistoryService.getById(id);

        if (op.getMinioFileKey() == null || op.getStatus() != ImportStatus.SUCCESS) {
            return ResponseEntity.notFound().build();
        }

        try {
            InputStream stream = minioService.getFile(op.getMinioFileKey());

            String fullKey = op.getMinioFileKey();
            String filename = fullKey.substring(fullKey.indexOf('-') + 1);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}