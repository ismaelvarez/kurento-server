package org.gtc.kurentoserver;

import javax.annotation.PostConstruct;

import org.gtc.kurentoserver.dao.CameraDAO;
import org.gtc.kurentoserver.model.Camera;
import org.gtc.kurentoserver.services.pipeline.PipelineManager;
import org.gtc.kurentoserver.services.pipeline.types.GTCPipeline;
import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kurento Configuration Module
 */
@Component
public class KurentoServerHelper {
    private static final Logger log = LoggerFactory.getLogger(KurentoServerHelper.class);

    @Autowired
    private KurentoClient kurentoClient;
	@Autowired
	private PipelineManager manager;
	@Autowired
	private CameraDAO repository;

	@Value( "${kurento.images.location}" )
    public static String kurentoImagesLocation;
    

	/**
	 * Init pipelines
	 */
	@PostConstruct
	public void init() {
		log.trace("KurentoServerHelper::init()");
		try {
			for (Camera c : repository.getAll(0, 0).getResults()) {
				if (c.getCameraMode().equalsIgnoreCase("stream"))
					createPipelineWithCamera(c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return if pipeline is loaded in PipelineManager
	 * @param pipelineID Pipeline id
	 * @return True if is loaded
	 */
	public boolean contains(String pipelineID) {
		return manager.get(pipelineID) != null;
	}

	/**
	 * Creates a new pipeline for a new camera. Id of pipeline equals CameraId
	 * @param camera Camera
	 */
	public void createPipelineWithCamera(Camera camera) {
		log.trace("KurentoServerHelper::createPipelineWithCamera({})", camera);
		manager.add(camera.getId(), new GTCPipeline(kurentoClient, camera));
	}


	/**
	 * Reload the pipeline with the new camera values
	 * @param camera Updated camera
	 */
	public void reloadPipelineOfCamera(Camera camera) {
		log.trace("KurentoServerHelper::reloadPipelineOfCamera({})", camera);
		deletePipelineOfCamera(camera.getId());
		createPipelineWithCamera(camera);
	}

	/**
	 * Delete the pipeline of camera
	 * @param camera Updated camera
	 */
	public void deletePipelineOfCamera(Camera camera) {
		log.trace("KurentoServerHelper::deletePipelineOfCamera({})", camera);
		deletePipelineOfCamera(camera.getId());
	}

	/**
	 * Delete the pipeline of camera id
	 * @param cameraId Id camera
	 */
	public void deletePipelineOfCamera(String cameraId) {
		log.trace("KurentoServerHelper::deletePipelineOfCamera({})", cameraId);
		manager.disconnectPipeline(cameraId);
	}

	/**
	 * Get Default Image Folder Path
	 */
	public String getKurentoImagesLocation() {
		return kurentoImagesLocation;
	}

}
