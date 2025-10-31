package com.labinvent.analyzer.controller;

import com.labinvent.analyzer.dto.AnalysisDetailDto;
import com.labinvent.analyzer.dto.HistoryItemDto;
import com.labinvent.analyzer.service.analysis.AnalysisService;
import com.labinvent.analyzer.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//todo сделать два контроллера
//todo slf4j
//todo заменить комментарии на swagger
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AnalysisController {

    private final StorageService storageService;
    private final AnalysisService analysisService;

    /**
     * POST /analyze — загрузка файла и запуск анализа.
     * Файл сохраняется, создаётся запись и сразу запускается асинхронный анализ.
     */
    @PostMapping("/analyze")
    public ResponseEntity<Void> analyze(@RequestParam("file") MultipartFile file) {
        String path = storageService.saveTempFile(file);
        Long id = analysisService.registerFile(file.getOriginalFilename(), file.getSize(), path);
        return ResponseEntity.accepted().build();
    }

    /**
     * POST /analyze/{id}/start — повторный запуск анализа.
     */
    @PostMapping("/analyze/{id}/start")
    public ResponseEntity<Void> start(@PathVariable Long id) {
        analysisService.startAnalysis(id);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /analyze/{id}/cancel — отмена анализа.
     */
    @PostMapping("/analyze/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        analysisService.cancel(id);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /analyze/{id}/progress — прогресс анализа.
     */
    @GetMapping("/analyze/{id}/progress")
    public ResponseEntity<Integer> progress(@PathVariable Long id) {
        return ResponseEntity.ok(analysisService.getProgress(id));
    }

    /**
     * GET /history — список анализов.
     */
    @GetMapping("/history")
    public ResponseEntity<List<HistoryItemDto>> history() {
        return ResponseEntity.ok(
                analysisService.getHistory()
        );
    }

    /**
     * GET /history/{id} — детали анализа.
     */
    @GetMapping("/history/{id}")
    public ResponseEntity<AnalysisDetailDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(analysisService.getDetail(id));
    }

    /**
     * DELETE /history/{id} — удаление анализа.
     */
    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        analysisService.delete(id);
        return ResponseEntity.noContent().build();
    }
}