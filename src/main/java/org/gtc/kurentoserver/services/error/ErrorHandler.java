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


    
}
