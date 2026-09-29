package com.example.spring_ai_demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationStartupLogger {

    @Value("${spring.ai.openai.admin-api-key}")
    private String adminApiKey;

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        log.warn("  ");
        log.warn("Spring AI Demo application READY");
        if (adminApiKey!=null && adminApiKey.length()>10) log.warn("  open-AI API-key ACTIVE !!!");
        log.warn("================================================");
    }

}
