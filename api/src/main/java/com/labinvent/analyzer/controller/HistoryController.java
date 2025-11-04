package com.labinvent.analyzer.controller;

import com.labinvent.analyzer.dto.AnalysisDetailDto;
import com.labinvent.analyzer.dto.HistoryItemDto;
import com.labinvent.analyzer.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "История анализов")
public class HistoryController {

    private final AnalysisService analysisService;

    @Operation(summary = "Получить список анализов")
    @GetMapping
    public ResponseEntity<List<HistoryItemDto>> history() {
        log.debug("Запрос списка анализов");
        return ResponseEntity.ok(analysisService.getHistory());
    }

    @Operation(summary = "Получить детали анализа")
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisDetailDto> detail(@PathVariable Long id) {
        log.debug("Запрос деталей анализа id={}", id);
        return ResponseEntity.ok(analysisService.getDetail(id));
    }

    @Operation(summary = "Удалить анализ")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Запрос на удаление анализа id={}", id);
        analysisService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
