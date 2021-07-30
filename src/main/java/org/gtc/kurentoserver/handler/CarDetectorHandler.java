package org.gtc.kurentoserver.handler;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.gtc.kurentoserver.pipeline.CrowdDetectorFilterConfig;
import org.gtc.kurentoserver.pipeline.PipelineOld;
import org.kurento.client.Composite;
import org.kurento.client.DispatcherOneToMany;
import org.kurento.client.HubPort;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaPipeline;
import org.kurento.client.MediaProfileSpecType;
import org.kurento.client.MediaType;
import org.kurento.client.PlayerEndpoint;
import org.kurento.client.RecorderEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.kurento.module.crowddetector.CrowdDetectorFilter;
import org.kurento.module.denoisecvmodule.denoiseCVModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class CarDetectorHandler extends TextWebSocketHandler {
    private static final String PLAZA_WEBCAM = "http://192.164.234.66/cgi-bin/faststream.jpg?0";
    private static final String VALENCIA_WEBCAM = "http://79.108.129.167:9000/mjpg/video.mjpg?0";
    private static final String ROAD_WEBCAM = "http://166.165.35.32/mjpg/video.mjpg?0";
    private static final String BEACH_WEBCAM = "http://185.10.80.33:8082/cgi-bin/faststream.jpg?0";
    private static final Logger log = LoggerFactory.getLogger(CarDetectorHandler.class);
    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private PipelineOld mainPipeline;


    public void init() {

      MediaPipeline mediaPipeline = this.mainPipeline.getPipe();

      PlayerEndpoint player1 = mainPipeline.constructPlayerEndpoint(ROAD_WEBCAM);
      PlayerEndpoint player2 = mainPipeline.constructPlayerEndpoint(PLAZA_WEBCAM);
      PlayerEndpoint player3 = mainPipeline.constructPlayerEndpoint(BEACH_WEBCAM);
      PlayerEndpoint player4 = mainPipeline.constructPlayerEndpoint(VALENCIA_WEBCAM);
      

      Composite hub = new Composite.Builder(mediaPipeline).build();

      HubPort player1Port = new HubPort.Builder(hub).build();
      HubPort player2Port = new HubPort.Builder(hub).build();
      HubPort player3Port = new HubPort.Builder(hub).build();
      HubPort player4Port = new HubPort.Builder(hub).build();

      HubPort outPort = new HubPort.Builder(hub).build();
      
      player3.connect(player3Port);

      player1.play();
      player2.play();
      player3.play();
      player4.play();

      //Create pipeline for Player1 using filter
      //Raw Player2


      mainPipeline.setRecorderEndpoint(new RecorderEndpoint.Builder(mediaPipeline, "file:///home/images/last.JPEG").withMediaProfile(MediaProfileSpecType.JPEG_VIDEO_ONLY).build());
      mainPipeline.getRecorderEndpoint().addRecordingListener(event -> log.info("Starting to record"));
      denoiseCVModule denoiseFilter = new denoiseCVModule.Builder(mediaPipeline).build();
      CrowdDetectorFilter crowddetector = CrowdDetectorFilterConfig.create(mediaPipeline, CrowdDetectorFilterConfig.getGTCCamROI());
      mainPipeline.setCrowdDetectorFilter(crowddetector);
      
      
      player1.connect(denoiseFilter);

      CrowdDetectorFilter crowdDetectorFilter2 = CrowdDetectorFilterConfig.create(mediaPipeline, CrowdDetectorFilterConfig.getValenciaROIs());
      mainPipeline.setCrowdDetectorFilter(crowdDetectorFilter2);
      denoiseCVModule denoiseFilter2 = new denoiseCVModule.Builder(mediaPipeline).build();
      player4.connect(denoiseFilter2);
      denoiseFilter2.connect(crowdDetectorFilter2);
      crowdDetectorFilter2.connect(player4Port);
      CrowdDetectorFilter crowdDetectorFilter3 = CrowdDetectorFilterConfig.create(mediaPipeline, CrowdDetectorFilterConfig.getPlazaROI());
      mainPipeline.setCrowdDetectorFilter(crowdDetectorFilter3);
      player2.connect(crowdDetectorFilter3);
      crowdDetectorFilter3.connect(player2Port);

      crowdDetectorFilter3.addCrowdDetectorFluidityListener(event -> {
        log.info("Filter3 - Fluidity Level: " + event.getFluidityPercentage());
      });

      crowdDetectorFilter2.addCrowdDetectorFluidityListener(event -> {
        log.info("Filter2 - Fluidity Level: " + event.getFluidityPercentage());
      });

      DispatcherOneToMany oneToMany = new DispatcherOneToMany.Builder(mediaPipeline).build();
      HubPort in = new HubPort.Builder(oneToMany).build();
      HubPort outRecord = new HubPort.Builder(oneToMany).build();
      HubPort outCrowdFilter = new HubPort.Builder(oneToMany).build();

      denoiseFilter.connect(in);
      oneToMany.setSource(in);
      outRecord.connect(mainPipeline.getRecorderEndpoint());
      outCrowdFilter.connect(crowddetector);


      crowddetector.connect(player1Port);
      log.info("FINISH START IN CONSTRUCTOR");

      mainPipeline.getRecorderEndpoint().record();
      
    }


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
      JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

      log.debug("Incoming message: {}", jsonMessage);

      switch (jsonMessage.get("id").getAsString()) {
      case "start":
        try {
            start(session, jsonMessage);
        } catch (Throwable t) {
            sendError(session, t.getMessage());
        }
        break;

      case "onIceCandidate": {
        JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();

        IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
            candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
        this.mainPipeline.addCandidate(cand, session.getId());
        break;
      }

      default:
        sendError(session, "Invalid message with id " + jsonMessage.get("id").getAsString());
        break;
      }
    }

    private static void sendError(WebSocketSession session, String message) {
        try {
          JsonObject response = new JsonObject();
          response.addProperty("id", "error");
          response.addProperty("message", message);
          session.sendMessage(new TextMessage(response.toString()));
        } catch (IOException e) {
          log.error("Exception sending message", e);
        }
      }


    private void start(final WebSocketSession session, JsonObject jsonMessage) throws IOException {

        log.info("Starting...");

        MediaPipeline mediaPipeline = this.mainPipeline.getPipe();

        PlayerEndpoint player1 = mainPipeline.constructPlayerEndpoint(ROAD_WEBCAM);
        PlayerEndpoint player2 = mainPipeline.constructPlayerEndpoint(PLAZA_WEBCAM);
        PlayerEndpoint player3 = mainPipeline.constructPlayerEndpoint(BEACH_WEBCAM);
        PlayerEndpoint player4 = mainPipeline.constructPlayerEndpoint(VALENCIA_WEBCAM);
        

        Composite hub = new Composite.Builder(mediaPipeline).build();

        HubPort player1Port = new HubPort.Builder(hub).build();
        HubPort player2Port = new HubPort.Builder(hub).build();
        HubPort player3Port = new HubPort.Builder(hub).build();
        HubPort player4Port = new HubPort.Builder(hub).build();

        HubPort outPort = new HubPort.Builder(hub).build();

        //player1.connect(player1Port);
        //player2.connect(player2Port);
        player3.connect(player3Port);
        //player4.connect(player4Port);


        /*if (this.mainPipeline.getPlayerEndpoint() == null) {
          try {
            JsonObject response = new JsonObject();
            response.addProperty("id", "noPlayer");
            session.sendMessage(new TextMessage(response.toString()));
          } catch (IOException a) {
            log.error("Exception sending message", a);
          }
          return;
        }*/
    
    
        WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(mediaPipeline).build();
    
        webRtcEndpoint.addIceCandidateFoundListener(event -> {
          JsonObject response = new JsonObject();
          response.addProperty("id", "iceCandidate");
          response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
          try {
            log.error(response.toString(), event);
            synchronized (session) {
              session.sendMessage(new TextMessage(response.toString()));
            }
          } catch (IOException e) {
            log.debug(e.getMessage());
          }
        });
    
        this.mainPipeline.setWebRtcEndpoint(session.getId(), webRtcEndpoint);

        player1.play();
        player2.play();
        player3.play();
        player4.play();

        //Create pipeline for Player1 using filter
        //Raw Player2


        mainPipeline.setRecorderEndpoint(new RecorderEndpoint.Builder(mediaPipeline, "/home/images/last.webm").build());
        mainPipeline.getRecorderEndpoint().addRecordingListener(event -> log.info("Starting to record"));
        denoiseCVModule denoiseFilter = new denoiseCVModule.Builder(mediaPipeline).build();
        mainPipeline.setCrowdDetectorFilter(CrowdDetectorFilterConfig.create(mediaPipeline, CrowdDetectorFilterConfig.getGTCCamROI()));
        

        player1.connect(denoiseFilter);

        CrowdDetectorFilter crowdDetectorFilter2 = CrowdDetectorFilterConfig.create(mediaPipeline, CrowdDetectorFilterConfig.getValenciaROIs());
        
        denoiseCVModule denoiseFilter2 = new denoiseCVModule.Builder(mediaPipeline).build();
        player4.connect(denoiseFilter2);
        denoiseFilter2.connect(crowdDetectorFilter2);
        crowdDetectorFilter2.connect(player4Port);

        CrowdDetectorFilter crowdDetectorFilter3 = CrowdDetectorFilterConfig.create(mediaPipeline, CrowdDetectorFilterConfig.getPlazaROI());
        player2.connect(crowdDetectorFilter3);
        crowdDetectorFilter3.connect(player2Port);

        crowdDetectorFilter3.addCrowdDetectorFluidityListener(event -> {
          log.info("Filter3 - Fluidity Level: " + event.getFluidityPercentage());
        });

        crowdDetectorFilter2.addCrowdDetectorFluidityListener(event -> {
          log.info("Filter2 - Fluidity Level: " + event.getFluidityPercentage());
        });

        DispatcherOneToMany oneToMany = new DispatcherOneToMany.Builder(mediaPipeline).build();
        HubPort in = new HubPort.Builder(oneToMany).build();
        HubPort outRecord = new HubPort.Builder(oneToMany).build();
        HubPort outCrowdFilter = new HubPort.Builder(oneToMany).build();

        denoiseFilter.connect(in);
        oneToMany.setSource(in);
        outRecord.connect(mainPipeline.getRecorderEndpoint(), MediaType.VIDEO);
        //outCrowdFilter.connect(mainPipeline.getCrowdDetectorFilter());


        //mainPipeline.getCrowdDetectorFilter().connect(player1Port);


        outPort.connect(webRtcEndpoint);
    
        // SDP negotiation (offer and answer)
        String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
        String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);
    
        // Sending response back to client
        JsonObject response = new JsonObject();
        response.addProperty("id", "startResponse");
        response.addProperty("sdpAnswer", sdpAnswer);

        synchronized (session) {
          session.sendMessage(new TextMessage(response.toString()));
        }


        mainPipeline.getRecorderEndpoint().record();
        log.info("WebRTCEndpoint gathering...");
        webRtcEndpoint.gatherCandidates();
      }


}
