package com.labinvent.analyzer.service.notify;

import com.labinvent.analyzer.dto.HistoryItemDto;
import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.mapper.AnalysisMapper;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.progress.ProgressRegistry;
import com.labinvent.analyzer.service.progress.ProgressState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class NotificationAspect {

    private final AnalysisNotifier notifier;
    private final ProgressRegistry progressRegistry;
    private final AnalysisResultRepository repository;
    private final AnalysisMapper mapper;

    @AfterReturning(pointcut = "@annotation(annotation)")
    public void sendNotification(JoinPoint jp, NotifyStatus annotation) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Object[] args = jp.getArgs();

        Long id = extractId(signature, args);
        if (id == null) {
            log.warn("Не удалось найти параметр id для метода {}", signature.getMethod().getName());
            return;
        }

        Integer progress = null;
        if (annotation.withProgress()) {
            ProgressState state = progressRegistry.get(id);
            if (state != null) {
                progress = state.getProgress();
            }
        }

        try {
            if (annotation.status() == AnalysisResultStatus.DONE) {
                AnalysisResult entity = repository.findById(id).orElse(null);
                if (entity != null) {
                    HistoryItemDto dto = mapper.toHistoryItem(entity);
                    notifier.notifyDone(dto);
                }
            } else {
                notifier.notifyStatus(id, annotation.status().name(), progress);
            }
        } catch (RuntimeException ex) {
            log.error("Ошибка при отправке уведомления id={}", id, ex);
        }
    }

    private Long extractId(MethodSignature signature, Object[] args) {
        String[] paramNames = signature.getParameterNames();
        for (int i = 0; i < paramNames.length; i++) {
            Object arg = args[i];
            if ("id".equals(paramNames[i]) && arg instanceof Long) {
                return (Long) arg;
            }
            //todo возможно заменить на передачу id в параметрах
            if (arg instanceof AnalysisResult record && record.getId() != null) {
                return record.getId();
            }
        }
        return null;
    }
}
