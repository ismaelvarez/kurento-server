package org.gtc.kurentoserver.pipeline;

import org.kurento.client.PlayerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewerPipeline extends AbstractPipeline {
    private static final Logger log = LoggerFactory.getLogger(ViewerPipeline.class);
    
    private String camera;
    private PlayerEndpoint playerEndpoint;

    public ViewerPipeline(String camera) {
        this.camera = camera;
    }

    @Override
    public void construct() {

        playerEndpoint = new PlayerEndpoint.Builder(pipe, camera).build();
        setEndHubSource(playerEndpoint);
        
    }
    
}
