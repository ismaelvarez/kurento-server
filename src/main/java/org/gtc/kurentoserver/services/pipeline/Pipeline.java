package org.gtc.kurentoserver.services.pipeline;

import org.gtc.kurentoserver.services.exceptions.PipelineErrorException;

public interface Pipeline {

    void construct() throws PipelineErrorException;

    boolean isPlaying();

    void release();
    
}
