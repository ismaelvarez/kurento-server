package org.gtc.kurentoserver.services.error;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.gtc.kurentoserver.services.orion.OrionContextBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Error Handler Module
 */
@ControllerAdvice
public class ErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @Autowired
    private OrionContextBroker orion;

    /**
     * Unsubscribe to Orion if an exception is throwed
     * @param ex
     */
    @ExceptionHandler(value= Exception.class)
    public void onErrorDeleteSubscription(Exception ex) {
        log.error("An exception has ocurred: {}",ex.getMessage());
        orion.unsubscribe();
    }
    
}
