package org.gtc.kurentoserver;

import javax.annotation.PostConstruct;

import org.gtc.kurentoserver.pipeline.PipelineManager;
import org.gtc.kurentoserver.pipeline.types.CameraViewerPipeline;
import org.gtc.kurentoserver.services.restful.entities.Camera;
import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KurentoServerConfig {
    private static final Logger log = LoggerFactory.getLogger(KurentoServerConfig.class);

    @Autowired
    private KurentoClient kurentoClient;
	@Autowired
	private PipelineManager manager;
    

	@PostConstruct
	public void init() {
		log.info("KurentoServerConfig::init()");
		for (Camera camera : Camera.getCameras()) {
			manager.add(camera.getId(), new CameraViewerPipeline(kurentoClient, camera));
		}
	}
}
