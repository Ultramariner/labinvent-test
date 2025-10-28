package com.labinvent.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "analysis_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private Long fileSizeBytes;

    private String tempFilePath;

    private Instant uploadedAt;

    private Instant processedAt;

    private Long processDurationMillis;

    private Long count;

    private Double minValue;

    private Double maxValue;

    private Double avg;

    private Double stdDev;

    private Long skipCount;

    private Long uniqueCount;

    @Enumerated(EnumType.STRING)
    private AnalysisRecordStatus status;
}