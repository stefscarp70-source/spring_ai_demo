package com.example.spring_ai_demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStartupLogger {

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        log.warn("  ");
        log.warn("Spring AI Demo application READY");
        log.warn("================================================");
    }

}
