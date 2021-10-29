package org.gtc.kurentoserver.services.pipeline;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.gtc.kurentoserver.services.PropertiesLoader;
import org.kurento.client.DispatcherOneToMany;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaElement;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;


/**
 * Abstract pipeline with the logic to connect/disconnec/addIceCandidates to a pipeline
 * Important: In the implementation of the construct method must specific the end media
 * element of the pipeline, using setEndHubSource
 */
public abstract class WebRtcPipeline implements Pipeline {
    protected KurentoClient kurento;

    protected MediaPipeline pipe;
    protected final Map<String, WebRtcEndpoint> webRtcEndpoints = new ConcurrentHashMap<>();
    protected DispatcherOneToMany endHub;
    protected Properties configuration = new Properties();

    private MediaElement lastElement;

    public WebRtcPipeline(KurentoClient kurentoClient) {
        this.kurento = kurentoClient;
        pipe = this.kurento.createMediaPipeline();
        endHub = new DispatcherOneToMany.Builder(pipe).build();
        try {
            configuration = PropertiesLoader.loadApplicationProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the last media element of the pipeline.ls
     * excd 
     * @param element Last media element of the pipeline
     */
    protected void setEndHubSource(MediaElement element) {
        //HubPort port = new HubPort.Builder(endHub).build();
        //element.connect(port);
        //endHub.setSource(port);
        lastElement = element;
    }

    /**
     * Return the MediaPipeline
     * @return MediaPipeline
     */
    public MediaPipeline getMediaPipeline() {
        return pipe;
    }

    /**exit
     * Add new WeRTCEndpoint 
     * @param sessionId Session id of the Websocket session
     * @param webRtcEndpoint WebRTCEndpoint 
     */
    public void addWebRtcEndpoint(String sessionId, WebRtcEndpoint webRtcEndpoint) {
        webRtcEndpoints.put(sessionId, webRtcEndpoint);
        //HubPort port = new HubPort.Builder(endHub).build();
        lastElement.connect(webRtcEndpoint);
    }

    /**
     * Remove the WebRTCEndpoint associated to a WebSocket session id
     * @param sessionId
     */
    public void removeWebRtcEndpoint(String sessionId) {
        WebRtcEndpoint endpoint = this.webRtcEndpoints.get(sessionId);
        if (endpoint != null) {
            webRtcEndpoints.remove(sessionId);
            endpoint.release();
        }
    }

    /**
     * Add a IceCandidate to a WebRTC of a WebSocket session
     * @param candidate IceCandidate
     * @param sessionId WebSocket session id
     */
    public void addCandidate(IceCandidate candidate, String sessionId) {
        WebRtcEndpoint endpoint = this.webRtcEndpoints.get(sessionId);
        if (endpoint != null) {
          endpoint.addIceCandidate(candidate);
        }
    }

    /**
     * Release all WebRTCEndpoints
     */
    @Override
    public void release() {
        for (WebRtcEndpoint webRtcEndpoint : webRtcEndpoints.values()) {
            webRtcEndpoint.release();
        }
        webRtcEndpoints.clear();
    }
}
