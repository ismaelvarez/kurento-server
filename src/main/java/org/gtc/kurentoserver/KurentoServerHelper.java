package org.gtc.kurentoserver;

import javax.annotation.PostConstruct;

import org.gtc.kurentoserver.pipeline.PipelineManager;
import org.gtc.kurentoserver.pipeline.types.CameraViewerPipeline;
import org.gtc.kurentoserver.services.restful.entities.Camera;
import org.gtc.kurentoserver.services.restful.repository.CameraRepository;
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
	private CameraRepository repository;

	@Value( "${kurento.images.location}" )
    public static String kurentoImagesLocation;
    

	/**
	 * Init pipelines
	 */
	@PostConstruct
	public void init() {
		log.trace("KurentoServerHelper::init()");
		for (Camera camera : repository.getAll()) {
			manager.add(camera.getId(), new CameraViewerPipeline(kurentoClient, camera));
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
		manager.add(camera.getId(), new CameraViewerPipeline(kurentoClient, camera));
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
		log.info("Pipeline with id {} disconnected...", cameraId);
	}

	/**
	 * Get Default Image Folder Path
	 */
	public String getKurentoImagesLocation() {
		return kurentoImagesLocation;
	}
}
