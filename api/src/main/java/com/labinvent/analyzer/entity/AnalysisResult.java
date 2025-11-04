package com.labinvent.analyzer.entity;

import com.labinvent.analyzer.converter.AnalysisMetricsConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "analysis_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private Long fileSizeBytes;
    private String tempFilePath;
    private Instant uploadedAt;
    private Instant processedAt;
    private Long processDurationMillis;

    @Convert(converter = AnalysisMetricsConverter.class)
    @Column(columnDefinition = "TEXT")
    private AnalysisMetrics metrics;

    @Enumerated(EnumType.STRING)
    private AnalysisResultStatus status;
}