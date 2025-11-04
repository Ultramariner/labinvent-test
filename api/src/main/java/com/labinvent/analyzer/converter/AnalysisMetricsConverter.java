package com.labinvent.analyzer.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labinvent.analyzer.entity.AnalysisMetrics;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter()
public class AnalysisMetricsConverter implements AttributeConverter<AnalysisMetrics, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(AnalysisMetrics metrics) {
        try {
            return metrics == null ? null : mapper.writeValueAsString(metrics);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Ошибка сериализации метрик", e);
        }
    }

    @Override
    public AnalysisMetrics convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : mapper.readValue(dbData, AnalysisMetrics.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Ошибка десериализации метрик", e);
        }
    }
}
