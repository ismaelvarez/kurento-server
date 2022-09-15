package org.gtc.kurentoserver.api;

public interface SessionManager {
    String createSession(String username);
    boolean sessionAlive(String token);

    void destroySession(String token);
}
