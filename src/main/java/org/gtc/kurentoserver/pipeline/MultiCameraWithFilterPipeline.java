package org.gtc.kurentoserver.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.kurento.client.Composite;
import org.kurento.client.DispatcherOneToMany;
import org.kurento.client.HubPort;
import org.kurento.client.PlayerEndpoint;
import org.kurento.module.crowddetector.CrowdDetectorFilter;
import org.kurento.module.denoisecvmodule.denoiseCVModule;
import org.kurento.module.recordermodule.RecorderModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiCameraWithFilterPipeline extends AbstractPipeline {
    private static final String PLAZA_WEBCAM = "http://192.164.234.66/cgi-bin/faststream.jpg?0";
    private static final String VALENCIA_WEBCAM = "http://79.108.129.167:9000/mjpg/video.mjpg?0";
    private static final String ROAD_WEBCAM = "http://166.165.35.32/mjpg/video.mjpg?0";
    private static final String BEACH_WEBCAM = "http://185.10.80.33:8082/cgi-bin/faststream.jpg?0";
    private static final int ROAD_PLAYER = 0;
    private static final int PLAZA_PLAYER = 1;
    private static final int BEACH_PLAYER = 2;
    private static final int VALENCIA_PLAYER = 3;
    private static final int ROAD_PORT = 0;
    private static final int PLAZA_PORT = 1;
    private static final int BEACH_PORT = 2;
    private static final int VALENCIA_PORT = 3;
    private static final int OUT_PORT = 4;

    private static final Logger log = LoggerFactory.getLogger(MultiCameraWithFilterPipeline.class);

    private List<CrowdDetectorFilter> filters = new ArrayList<>();
    private PlayerEndpoint[] playerEndpoints;
    private RecorderModule recorder;

    private HubPort[] hubPorts;

    private void initMediaElements() {
        PlayerEndpoint[] playerEndpoints = {
            new PlayerEndpoint.Builder(pipe, ROAD_WEBCAM).build(),
            new PlayerEndpoint.Builder(pipe, PLAZA_WEBCAM).build(),
            new PlayerEndpoint.Builder(pipe, BEACH_WEBCAM).build(),
            new PlayerEndpoint.Builder(pipe, VALENCIA_WEBCAM).build()
        };

        this.playerEndpoints = playerEndpoints;

        Composite hub = new Composite.Builder(pipe).build();

        HubPort[] hubPorts = {
            new HubPort.Builder(hub).build(),
            new HubPort.Builder(hub).build(),
            new HubPort.Builder(hub).build(),
            new HubPort.Builder(hub).build(),
            new HubPort.Builder(hub).build()
        };

        this.hubPorts = hubPorts;
    }

    private void playPlayerEndpoints() {
        for (PlayerEndpoint p : playerEndpoints) {
            p.play();
        }
    }

    @Override
    public void construct() {
        initMediaElements();
        
        playerEndpoints[BEACH_PLAYER].connect(hubPorts[BEACH_PORT]);
  
        playPlayerEndpoints();

        createFilters(playerEndpoints[ROAD_PLAYER], hubPorts[ROAD_PORT], "Road Webcam Event", true);

        createFilters(playerEndpoints[PLAZA_PLAYER], hubPorts[PLAZA_PORT], "Plaza Webcam Event", false);
  
        DispatcherOneToMany oneToMany = new DispatcherOneToMany.Builder(pipe).build();
        HubPort in = new HubPort.Builder(oneToMany).build();
        HubPort outRecord = new HubPort.Builder(oneToMany).build();
        HubPort outCrowdFilter = new HubPort.Builder(oneToMany).build();

        recorder = new RecorderModule.Builder(pipe, "/tmp/images", "image").build();
        recorder.addRecorderModuleFrameSavedListener(event -> {
            log.info("Frame saved:  " + event.getPathToFile());
        });

        CrowdDetectorFilter crowdDetectorFilter = CrowdDetectorFilterConfig.create(pipe, CrowdDetectorFilterConfig.getValenciaROIs());
        saveCrowdDetector(crowdDetectorFilter);
        crowdDetectorFilter.addCrowdDetectorFluidityListener(event -> {
            log.info("Valencia Webcam Event " + event.getFluidityPercentage());
          });
        
        denoiseCVModule denoiseFilter = new denoiseCVModule.Builder(pipe).build();
        playerEndpoints[VALENCIA_PLAYER].connect(denoiseFilter);
        
        denoiseFilter.connect(in);
        oneToMany.setSource(in);
        outRecord.connect(recorder);
        outCrowdFilter.connect(crowdDetectorFilter);
        crowdDetectorFilter.connect(hubPorts[VALENCIA_PORT]);

        log.info("FINISH START IN CONSTRUCTOR");

        setEndHubSource(hubPorts[OUT_PORT]);
    }

    private void createFilters(PlayerEndpoint endpoint, HubPort port, String message, boolean withDenoise) {
        CrowdDetectorFilter crowdDetectorFilter = CrowdDetectorFilterConfig.create(pipe, CrowdDetectorFilterConfig.getValenciaROIs());
        saveCrowdDetector(crowdDetectorFilter);
        if (withDenoise) {
            denoiseCVModule denoiseFilter = new denoiseCVModule.Builder(pipe).build();
            endpoint.connect(denoiseFilter);
            denoiseFilter.connect(crowdDetectorFilter);
        } else {
            endpoint.connect(crowdDetectorFilter);
        }
        
        crowdDetectorFilter.connect(port);
        crowdDetectorFilter.addCrowdDetectorFluidityListener(event -> {
            log.info(message + " " + event.getFluidityPercentage());
          });
    }

    private void saveCrowdDetector(CrowdDetectorFilter crowdDetectorFilter2) {
        filters.add(crowdDetectorFilter2);
    }
    
}
