package com.labinvent.analyzer.dto;

import java.time.Instant;

public record AnalysisDetailDto(
        Long id,
        String fileName,
        Long fileSizeBytes,
        String tempFilePath,
        Instant uploadedAt,
        Instant processedAt,
        Long processDurationMillis,
        Long count,
        Double minValue,
        Double maxValue,
        Double avg,
        Double stdDev,
        Long skipCount,
        Long uniqueCount,
        String status
) {}