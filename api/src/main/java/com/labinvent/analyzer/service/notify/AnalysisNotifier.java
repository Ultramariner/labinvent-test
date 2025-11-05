package com.labinvent.analyzer.service.notify;

import com.labinvent.analyzer.dto.HistoryItemDto;
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
        messagingTemplate.convertAndSend("/topic/analysis",
                Map.of("id", id, "status", status, "progress", progress));
    }

    public void notifyDone(HistoryItemDto dto) {
        messagingTemplate.convertAndSend("/topic/analysis", dto);
    }
}
