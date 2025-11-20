package ru.ani.islab1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ani.islab1.models.ImportOperation;
import ru.ani.islab1.services.ImportHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/import-history")
@RequiredArgsConstructor
public class ImportHistoryController {

    private final ImportHistoryService importHistoryService;

    @GetMapping
    public ResponseEntity<List<ImportOperation>> getHistory() {
        return ResponseEntity.ok(importHistoryService.getHistory());
    }
}