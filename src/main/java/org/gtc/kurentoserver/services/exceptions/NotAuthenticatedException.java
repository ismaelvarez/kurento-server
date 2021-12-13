package org.gtc.kurentoserver.services.exceptions;

public class NotAuthenticatedException extends RuntimeException {
    public NotAuthenticatedException() {
        super("This operation requires to be authenticated");
    }
}
