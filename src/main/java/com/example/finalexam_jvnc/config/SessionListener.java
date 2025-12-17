package com.example.finalexam_jvnc.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.concurrent.ConcurrentHashMap;
public class SessionListener implements HttpSessionListener, ServletContextListener {
    
    private static final ConcurrentHashMap<String, HttpSession> activeSessions = new ConcurrentHashMap<>();
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        activeSessions.put(session.getId(), session);
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        activeSessions.remove(session.getId());
    }
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        activeSessions.clear();
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        invalidateAllSessions();
    }

    public static void invalidateAllSessions() {
        for (HttpSession session : activeSessions.values()) {
            try {
                if (session != null) {
                    session.invalidate();
                }
            } catch (Exception e) {
            }
        }
        activeSessions.clear();
    }
    
    public static int getActiveSessionCount() {
        return activeSessions.size();
    }
}

