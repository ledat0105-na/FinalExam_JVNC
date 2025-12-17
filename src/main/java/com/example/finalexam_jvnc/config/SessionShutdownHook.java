package com.example.finalexam_jvnc.config;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;


@Component
public class SessionShutdownHook {
    
    @PreDestroy
    public void onShutdown() {
        SessionListener.invalidateAllSessions();
    }
}

