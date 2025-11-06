package com.labinvent.analyzer.service.scheduler;


import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisStartupRecovery {

    private final AnalysisService analysisService;
    private final AnalysisResultRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverInterruptedAnalyses() {
        var interrupted = repository.findAllByStatus(AnalysisResultStatus.PROCESSING);
        if (!interrupted.isEmpty()) {
            log.info("Найдено {} незавершённых анализов, перезапускаем...", interrupted.size());
            interrupted.forEach(result -> {
                try {
                    analysisService.startAnalysis(result);
                } catch (org.springframework.dao.DataAccessException dae) {
                    log.error("Ошибка доступа к БД при перезапуске анализа id={}", result.getId(), dae);
                } catch (IllegalStateException | IllegalArgumentException ex) {
                    log.error("Некорректное состояние при перезапуске анализа id={}", result.getId(), ex);
                } catch (RuntimeException re) {
                    log.error("Неожиданная ошибка при перезапуске анализа id={}", result.getId(), re);
                }
            });
        } else {
            log.debug("Незавершённых анализов не найдено");
        }
    }
}
