package org.gtc.kurentoserver.pipeline.types;

import org.gtc.kurentoserver.pipeline.KurentoPipeline;
import org.gtc.kurentoserver.services.restful.entities.Camera;
import org.kurento.client.KurentoClient;
import org.kurento.client.PlayerEndpoint;
import org.kurento.module.recordermodule.RecorderModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline to visualice a camera
 */
public class CameraViewerPipeline extends KurentoPipeline {
    private static final Logger log = LoggerFactory.getLogger(CameraViewerPipeline.class);
    
    private Camera camera;
    private PlayerEndpoint playerEndpoint;
    private RecorderModule recorderModule;

    public CameraViewerPipeline(KurentoClient kurentoClient, Camera camera) {
        super(kurentoClient);
        this.camera = camera;
    }

    @Override
    public void construct() {
        log.info("Constructing ViewerPipeline with id : {}", camera.getId());
        playerEndpoint = new PlayerEndpoint.Builder(pipe, camera.getUrl()).build();
        recorderModule = new RecorderModule.Builder(pipe, "/tmp/kurento/images/"+camera.getId(), camera.getId()).build();
        recorderModule.addRecorderModuleFrameSavedListener(event -> {
            log.debug("Frame saved with name {}", event.getPathToFile());
        });
        playerEndpoint.connect(recorderModule);
        setEndHubSource(recorderModule);
        playerEndpoint.play();
    }

    @Override
    public void release() {
        playerEndpoint.release();
        super.release();
    }
    
}
