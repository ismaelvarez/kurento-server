package org.gtc.kurentoserver.pipeline.types;

import org.gtc.kurentoserver.pipeline.KurentoPipeline;
import org.gtc.kurentoserver.services.orion.publisher.CarDetectionPublisher;
import org.gtc.kurentoserver.services.restful.entities.Camera;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaElement;
import org.kurento.client.PlayerEndpoint;
import org.kurento.module.cardetector.CarDetector;
import org.kurento.module.recordermodule.RecorderModule;
import org.kurento.orion.connector.OrionConnectorConfiguration;
import org.kurento.orion.connector.OrionConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline to visualice a camera
 */
public class CameraViewerPipeline extends KurentoPipeline {
    private static final Logger log = LoggerFactory.getLogger(CameraViewerPipeline.class);
    
    private Camera camera;
    private PlayerEndpoint playerEndpoint;
    private CarDetectionPublisher carPublisher = new CarDetectionPublisher(new OrionConnectorConfiguration());
    private CarDetector carDetector;
    private RecorderModule recorderModule;

    public CameraViewerPipeline(KurentoClient kurentoClient, Camera camera) {
        super(kurentoClient);
        this.camera = camera;
    }

    @Override
    public void construct() {
        log.info("Constructing ViewerPipeline of camera : {}", camera.getId());
        playerEndpoint = new PlayerEndpoint.Builder(pipe, camera.getUrlWithCredentials()).build();

        MediaElement last = playerEndpoint;

        // Configuration for the Recorder Module
        if (camera.getKurentoConfig().getOrDefault("recorder", false)) {
            recorderModule = new RecorderModule.Builder(pipe, "/tmp/kurento/images/"+camera.getId(), camera.getId()).build();
            recorderModule.addRecorderModuleFrameSavedListener(event -> {
                log.debug("Frame saved with name {}", event.getPathToFile());
            });
            last.connect(recorderModule);

            last = recorderModule;
        }

        // Configuration for the Car Detection
        if (camera.getKurentoConfig().getOrDefault("carDetection", false)) {
            carDetector = new CarDetector.Builder(pipe, System.getenv().get("CAR_DETECTOR_CASCADE_XML"), camera.getId(),
                Double.parseDouble(configuration.getProperty("kurento.cardetector.scalefactor")),
                Integer.parseInt(configuration.getProperty("kurento.cardetector.minneighbors")),
                Integer.parseInt(configuration.getProperty("kurento.cardetector.width")),
                Integer.parseInt(configuration.getProperty("kurento.cardetector.height"))).build();
            carDetector.addCarsDetectedListener(event -> {
                log.debug("Camera: {} -- Cars Detected={}", this.camera.getId(), event.getCarsDetected());
                try {
                    carPublisher.publish(event);
                } catch (OrionConnectorException ex) {
                    carPublisher.update(event);
                }
            });

            last.connect(carDetector);

            last = carDetector;
        }

        playerEndpoint.play();
        setEndHubSource(last);
    }

    @Override
    public void release() {
        playerEndpoint.release();
        super.release();
    }
    
}
