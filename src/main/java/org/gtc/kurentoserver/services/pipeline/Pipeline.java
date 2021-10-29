package org.gtc.kurentoserver.services.pipeline;

public interface Pipeline {

    void construct();

    boolean isPlaying();

    void release();
    
}
