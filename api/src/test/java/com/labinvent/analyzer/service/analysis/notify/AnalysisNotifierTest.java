package com.labinvent.analyzer.service.analysis.notify;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

class AnalysisNotifierTest {

    @Test
    void testNotifyStatus() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        AnalysisNotifier notifier = new AnalysisNotifier(template);

        notifier.notifyStatus(1L, "PROCESSING", 50);

        verify(template).convertAndSend(eq("/topic/analysis"), anyMap());
    }
}
