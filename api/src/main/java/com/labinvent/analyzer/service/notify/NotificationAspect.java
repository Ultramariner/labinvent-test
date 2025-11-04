package com.labinvent.analyzer.service.notify;

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

    @AfterReturning(pointcut = "@annotation(annotation)")
    public void sendNotification(JoinPoint jp, NotifyStatus annotation) {
        try {
            MethodSignature signature = (MethodSignature) jp.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = jp.getArgs();

            Long id = null;
            for (int i = 0; i < paramNames.length; i++) {
                if ("id".equals(paramNames[i]) && args[i] instanceof Long) {
                    id = (Long) args[i];
                    break;
                }
            }

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

            notifier.notifyStatus(id, annotation.status().name(), progress);

        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления: {}", e.getMessage(), e);
        }
    }
}
