package com.labinvent.analyzer.service.notify;

import com.labinvent.analyzer.entity.AnalysisResultStatus;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotifyStatus {
    AnalysisResultStatus status();
    boolean withProgress() default false;
}
