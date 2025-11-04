package com.labinvent.analyzer.service.notify;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalysisNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyStatus(Long id, String status, Integer progress) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", id);
        payload.put("status", status);
        if (progress != null) {
            payload.put("progress", progress);
        }
        messagingTemplate.convertAndSend("/topic/analysis", payload);
    }
}
