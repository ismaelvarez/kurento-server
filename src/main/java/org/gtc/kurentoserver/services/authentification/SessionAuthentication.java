package org.gtc.kurentoserver.services.authentification;

import org.gtc.kurentoserver.api.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SessionAuthentication implements SessionManager
{
    private static final Logger log = LoggerFactory.getLogger(SessionAuthentication.class);
    private final Map<String, LocalTime> sessionsLogged;

    public SessionAuthentication() {
        sessionsLogged = new HashMap<>();
    }

    @Override
    public boolean sessionAlive(String sessionId) {
        return sessionsLogged.containsKey(sessionId);
    }

    @Override
    public void destroySession(String sessionId) {
        sessionsLogged.remove(sessionId);
    }

    @Override
    public String createSession(String sessionId) {
        sessionsLogged.put(sessionId, LocalTime.now());
        return sessionId;
    }

    private LocalTime getTimeAlive(String sessionId) {
        return sessionsLogged.getOrDefault(sessionId, null);
    }


    @Scheduled(fixedRate = 3600000)
    public void refresh() {
        try {
            sessionsLogged.forEach((key, value) -> {
                if (ChronoUnit.HOURS.between(LocalTime.now(), value) > 6) {
                    destroySession(key);
                }
            });
        } catch (Exception ex) {
            log.warn("An error has occurred while checking sessions. "  + ex.getMessage());
        }
    }
}
