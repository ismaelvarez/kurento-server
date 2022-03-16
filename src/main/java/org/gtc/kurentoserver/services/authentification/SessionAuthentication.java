package org.gtc.kurentoserver.services.authentification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SessionAuthentication
{
    private static final Logger log = LoggerFactory.getLogger(SessionAuthentication.class);
    private final ConcurrentHashMap<String, LocalTime> sessionsLogged;

    public SessionAuthentication() {
        sessionsLogged = new ConcurrentHashMap<>();
    }

    public boolean isLogged(String sessionId) {
        return sessionsLogged.containsKey(sessionId);
    }

    public void logIn(String sessionId) {
        sessionsLogged.put(sessionId, LocalTime.now());
    }

    public LocalTime getTimeAlive(String sessionId) {
        return sessionsLogged.getOrDefault(sessionId, null);
    }

    public void logOut(String sessionId) {
        sessionsLogged.remove(sessionId);
    }

    @Scheduled(fixedRate = 3600000)
    public void refresh() {
        try {
            sessionsLogged.forEach((key, value) -> {
                if (ChronoUnit.HOURS.between(LocalTime.now(), value) > 6) {
                    logOut(key);
                }
            });
        } catch (Exception ex) {
            log.warn("An error has occurred while checking sessions. "  + ex.getMessage());
        }
    }
}
