package org.gtc.kurentoserver.pipeline;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.kurento.client.DispatcherOneToMany;
import org.kurento.client.HubPort;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaElement;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPipeline {

    @Autowired
    protected KurentoClient kurento;

    protected MediaPipeline pipe;
    protected final Map<String, WebRtcEndpoint> webRtcEndpoints = new ConcurrentHashMap<>();
    protected DispatcherOneToMany endHub;

    @PostConstruct
    public void init() {
        pipe = this.kurento.createMediaPipeline();
        endHub = new DispatcherOneToMany.Builder(pipe).build();
        construct();
    } 

    protected void setEndHubSource(MediaElement element) {
        HubPort port = new HubPort.Builder(endHub).build();
        element.connect(port);
        endHub.setSource(port);
    }

    public MediaPipeline getMediaPipeline() {
        return pipe;
    }

    public void addWebRtcEndpoint(String sessionId, WebRtcEndpoint webRtcEndpoint) {
        webRtcEndpoints.put(sessionId, webRtcEndpoint);
        HubPort port = new HubPort.Builder(endHub).build();
        port.connect(webRtcEndpoint);
    }

    public void removeWebRtcEndpoint(String sessionId) {
        WebRtcEndpoint endpoint = this.webRtcEndpoints.get(sessionId);
        if (endpoint != null) {
            webRtcEndpoints.remove(sessionId);
            endpoint.release();
        }
    }

    public void addCandidate(IceCandidate candidate, String sessionId) {
        WebRtcEndpoint endpoint = this.webRtcEndpoints.get(sessionId);
        if (endpoint != null) {
          endpoint.addIceCandidate(candidate);
        }
    }

    public abstract void construct();
}
