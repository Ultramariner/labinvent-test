package com.labinvent.analyzer.controller;

import com.labinvent.analyzer.service.AnalysisService;
import com.labinvent.analyzer.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/analyze")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "Операции загрузки файла и выполнения анализа")
public class AnalysisController {

    private final StorageService storageService;
    private final AnalysisService analysisService;

    @Operation(summary = "Загрузить файл и запустить анализ")
    @PostMapping
    public ResponseEntity<Void> analyze(@RequestParam("file") MultipartFile file) {
        log.debug("Запрос на загрузку файла: {}", file.getOriginalFilename());
        String path = storageService.saveTempFile(file);
        analysisService.registerFile(file.getOriginalFilename(), file.getSize(), path);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Запуск анализа")
    @PostMapping("/{id}/start")
    public ResponseEntity<Void> start(@PathVariable Long id) {
        log.debug("Запрос на запуск анализа id={}", id);
        analysisService.startAnalysis(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отмена анализа")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        log.debug("Запрос на отмену анализа id={}", id);
        analysisService.cancel(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить прогресс анализа")
    @GetMapping("/{id}/progress")
    public ResponseEntity<Integer> progress(@PathVariable Long id) {
        log.debug("Запрос прогресса анализа id={}", id);
        return ResponseEntity.ok(analysisService.getProgress(id));
    }
}