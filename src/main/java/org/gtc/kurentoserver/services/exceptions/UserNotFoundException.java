package org.gtc.kurentoserver.services.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String user) {
        super("Could not find user " + user);
    }
}