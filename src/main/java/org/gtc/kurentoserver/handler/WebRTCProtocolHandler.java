package org.gtc.kurentoserver.handler;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.gtc.kurentoserver.pipeline.PipelineManager;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Handler for the WebRTC petitions to connect to the Kurento Pipelines 
 */
public class WebRTCProtocolHandler extends TextWebSocketHandler {
  private static final Logger log = LoggerFactory.getLogger(WebRTCProtocolHandler.class);

  @Autowired
  private PipelineManager pipelineManager;
  private static final Gson gson = new GsonBuilder().create();

  
  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    //Message sended by web client
    JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

    log.debug("Incoming message: {}", jsonMessage);
    
      
    switch (jsonMessage.get("id").getAsString()) {
      case "sdpOffer":
        try {
          if (jsonMessage.has("idCam") && !jsonMessage.get("idCam").isJsonNull() ) {
            createSDPAnswer(session, jsonMessage);
          } else {
            sendError(session, "idCam no specified in the message");
          }
          
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

        if (jsonMessage.has("idCam")) {
          String idCam = jsonMessage.get("idCam").getAsString();
          pipelineManager.get(idCam).addCandidate(cand, session.getId());
        }
        break;
      }

      default:
        sendError(session, "Invalid message with id " + jsonMessage.get("id").getAsString());
        break;
        
    } 
  }

  /**
   * Close all WebRTC connections of the session
   * @param session Websocket session
   */
  private void stop(WebSocketSession session) {
    log.info("WebRTC connection with session {} disconnected", session.getId());
    pipelineManager.releaseAllWebRTCOf(session.getId());
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    log.debug("WS disconnection");
    log.info("Connection closed by session {}", session.getId());
    pipelineManager.releaseAllWebRTCOf(session.getId());
  }

  /**
   * Creates a SDP Answer to the web client. Also it creates/connects the endpoint to the speficic pipeline
   * using the field idCam  
   * @param session Websocket session
   * @param jsonMessage Message in JSON sended by the web client
   * @throws IOException 
   */
  private void createSDPAnswer(WebSocketSession session, JsonObject jsonMessage) throws IOException {
    //Identifier of camera
    String idCam = jsonMessage.get("idCam").getAsString();

    //Create WebRTCEndpoint per session
    MediaPipeline pipeline = pipelineManager.get(idCam).getMediaPipeline();
    WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();

    webRtcEndpoint.addIceCandidateFoundListener(event -> {
      JsonObject response = new JsonObject();
      response.addProperty("id", "iceCandidate");
      response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
      response.add("idCam", JsonUtils.toJsonElement(idCam));
      try {
        synchronized (session) {
          session.sendMessage(new TextMessage(response.toString()));
        }
      } catch (IOException e) {
        log.debug(e.getMessage());
      }
    });

    //Add EndPoint to pipeline
    pipelineManager.get(idCam).addWebRtcEndpoint(session.getId(), webRtcEndpoint);
    
    // SDP negotiation (offer and answer)
    String sdpAnswer = webRtcEndpoint.processOffer(jsonMessage.get("sdpOffer").getAsString());
    sendSDPAnswer(sdpAnswer, session, jsonMessage);


    //Start WebRTC stream
    webRtcEndpoint.gatherCandidates();
  }

  /**
   * Error while creating the SDP Answer. Sends message to client
   * @param session Websocket session
   * @param message Message to send to the client
   */
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

  /**
   * Sends an SDPAnswer to the client
   * @param sdpAnswer SDPAnswer of the KMS
   * @param session Websocket session
   * @param jsonMessage Message sended by the client
   * @throws IOException
   */
  private void sendSDPAnswer(String sdpAnswer, WebSocketSession session, JsonObject jsonMessage) throws IOException {
    JsonObject response = new JsonObject();
    response.addProperty("id", "sdpAnswer");
    response.addProperty("sdpAnswer", sdpAnswer);
    response.addProperty("idCam", jsonMessage.get("idCam").getAsString());

    synchronized (session) {
      session.sendMessage(new TextMessage(response.toString()));
    }
  }
}
