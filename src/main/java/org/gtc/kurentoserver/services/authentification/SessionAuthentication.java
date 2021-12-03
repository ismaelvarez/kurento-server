package org.gtc.kurentoserver.services.authentification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;


public class SessionAuthentication
{
    private final Map<String, LocalTime> sessionsLogged;

    public SessionAuthentication() {
        sessionsLogged = new HashMap<>();
    }

    public boolean isLogged(String sessionId) {
        return sessionsLogged.containsKey(sessionId);
    }

    public void logIn(String sessionId) {
        sessionsLogged.put(sessionId, LocalTime.now());
    }

    public void logOut(String sessionId) {
        sessionsLogged.remove(sessionId);
    }


    public void refresh() {
        sessionsLogged.forEach((key, value) -> {
            if (ChronoUnit.HOURS.between(LocalTime.now(), value) > 6) {
                logOut(key);
            }
        });
    }
}
