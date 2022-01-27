package org.gtc.kurentoserver.services.pipeline.types;

import org.gtc.kurentoserver.entities.Camera;
import org.gtc.kurentoserver.services.exceptions.PipelineErrorException;
import org.gtc.kurentoserver.services.orion.publisher.CarDetectionPublisher;
import org.gtc.kurentoserver.services.pipeline.WebRtcPipeline;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaElement;
import org.kurento.client.MediaFlowState;
import org.kurento.client.PlayerEndpoint;
import org.kurento.module.cardetector.CarDetector;
import org.kurento.module.recordermodule.RecorderModule;
import org.kurento.orion.connector.OrionConnectorConfiguration;
import org.kurento.orion.connector.OrionConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.System.getenv;

/**
 * Pipeline to visualice a camera
 */
public class GTCPipeline extends WebRtcPipeline {
    private static final Logger log = LoggerFactory.getLogger(GTCPipeline.class);

    private Camera camera;
    private PlayerEndpoint playerEndpoint;
    private CarDetectionPublisher carPublisher;
    private CarDetector carDetector;
    private RecorderModule recorderModule;
    private boolean isPlaying;

    public GTCPipeline(KurentoClient kurentoClient, Camera camera) {
        super(kurentoClient);
        this.camera = camera;
        OrionConnectorConfiguration orionConnectorConfiguration = new OrionConnectorConfiguration();
        orionConnectorConfiguration.setOrionHost(getenv().getOrDefault("ORION_HOST",
                configuration.getProperty("orion.host")));
        carPublisher = new CarDetectionPublisher(orionConnectorConfiguration);
    }

    @Override
    public void construct() throws PipelineErrorException {
        log.info("Constructing ViewerPipeline of camera : {}", camera.getId());
        playerEndpoint = new PlayerEndpoint.Builder(pipe, camera.getUrlWithCredentials()).build();

        playerEndpoint.addErrorListener(event -> {
            if (isPlaying) {
                log.info("Pipeline {} offline. No video playing...", camera.getId());
                stop();
            }
        });

        playerEndpoint.addEndOfStreamListener(event -> {
            if (isPlaying) {
                log.info("Pipeline {} offline. No video playing...", camera.getId());
                stop();
            }
        });

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
            carDetector = new CarDetector.Builder(pipe, getenv().getOrDefault("CAR_DETECTOR_CASCADE_XML",
            configuration.getProperty("kurento.cardetector.cascadexml.location")), camera.getId(),
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
        isPlaying = true;
        setEndHubSource(last);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void release() {
        playerEndpoint.release();
        if (carDetector!=null)
            carDetector.release();
        
        if (recorderModule != null ) 
            recorderModule.release();

        super.release();
    }

    private synchronized void stop() {
        isPlaying = false;
    }
    
}
