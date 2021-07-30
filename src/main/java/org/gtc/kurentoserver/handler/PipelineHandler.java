package org.gtc.kurentoserver.handler;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.gtc.kurentoserver.pipeline.AbstractPipeline;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class PipelineHandler extends TextWebSocketHandler {


    private AbstractPipeline pipe;


    public PipelineHandler(AbstractPipeline pipeline) {
      super();
      this.pipe = pipeline;
    }


    private static final Logger log = LoggerFactory.getLogger(PipelineHandler.class);
    private static final Gson gson = new GsonBuilder().create();

    
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

      case "stop":
        try {
            stop(session);
        } catch (Throwable t) {
            sendError(session, t.getMessage());
        }
        break;

      case "onIceCandidate": {
        JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();

        IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
            candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
        this.pipe.addCandidate(cand, session.getId());
        break;
      }

      default:
        sendError(session, "Invalid message with id " + jsonMessage.get("id").getAsString());
        break;
      }
    }

    private void stop(WebSocketSession session) {
      log.debug("WS disconnection");
      pipe.removeWebRtcEndpoint(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

      log.debug("WS disconnection");
      pipe.removeWebRtcEndpoint(session.getId());
    }

    private void start(WebSocketSession session, JsonObject jsonMessage) throws IOException {
      MediaPipeline pipeline = pipe.getMediaPipeline();
      WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
  
      webRtcEndpoint.addIceCandidateFoundListener(event -> {
        JsonObject response = new JsonObject();
        response.addProperty("id", "iceCandidate");
        response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
        try {
          //log.error(response.toString(), event);
          synchronized (session) {
            session.sendMessage(new TextMessage(response.toString()));
          }
        } catch (IOException e) {
          log.debug(e.getMessage());
        }
      });
  
      pipe.addWebRtcEndpoint(session.getId(), webRtcEndpoint);
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

      log.info("WebRTCEndpoint gathering...");
      
      webRtcEndpoint.gatherCandidates();
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
}
