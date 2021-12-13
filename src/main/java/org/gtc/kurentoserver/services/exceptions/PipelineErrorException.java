package org.gtc.kurentoserver.services.exceptions;

public class PipelineErrorException extends RuntimeException {
    public PipelineErrorException() {
        super("Error while creating a pipeline");
    }
}
