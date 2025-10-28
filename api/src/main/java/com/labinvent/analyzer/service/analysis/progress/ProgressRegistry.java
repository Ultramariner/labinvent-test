package com.labinvent.analyzer.service.analysis.progress;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ProgressRegistry {
    private final ConcurrentMap<Long, ProgressState> registry = new ConcurrentHashMap<>();

    public ProgressState getOrCreate(Long analysisId) {
        return registry.computeIfAbsent(analysisId, id -> new ProgressState());
    }

    public ProgressState get(Long analysisId) {
        return registry.get(analysisId);
    }

    public void remove(Long analysisId) {
        registry.remove(analysisId);
    }
}