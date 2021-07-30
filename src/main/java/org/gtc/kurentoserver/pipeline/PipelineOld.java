package org.gtc.kurentoserver.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.PlayerEndpoint;
import org.kurento.client.RecorderEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.module.crowddetector.CrowdDetectorFilter;
import org.springframework.beans.factory.annotation.Autowired;

public class PipelineOld {

    @Autowired
    private KurentoClient kurento;

    private MediaPipeline pipe;
    private final Map<String, WebRtcEndpoint> webRtcEndpoints = new ConcurrentHashMap<>();
    private PlayerEndpoint playerEndpoint;
    private List<CrowdDetectorFilter> filter = new ArrayList<>();
    private RecorderEndpoint recorderEndpoint;

    @PostConstruct
    public void init() {
        this.setPipe(this.kurento.createMediaPipeline());
    } 

    public RecorderEndpoint getRecorderEndpoint() {
        return recorderEndpoint;
    }

    public void setRecorderEndpoint(RecorderEndpoint recorderEndpoint) {
        this.recorderEndpoint = recorderEndpoint;
    }

    public MediaPipeline getPipe() {
        if (pipe == null)
            pipe = this.kurento.createMediaPipeline();
        return pipe;
    }

    private void setPipe(MediaPipeline pipe) {
        this.pipe = pipe;
    }

    public void setWebRtcEndpoint(String sessionId, WebRtcEndpoint webRtcEndpoint) {
        webRtcEndpoints.put(sessionId, webRtcEndpoint);
    }

    public void setCrowdDetectorFilter(CrowdDetectorFilter filter) {
        this.filter.add(filter);
    }

    public PlayerEndpoint getPlayerEndpoint() {
        return playerEndpoint;
    }

    public void addCandidate(IceCandidate candidate, String session) {
        WebRtcEndpoint endpoint = this.webRtcEndpoints.get(session);
        if (endpoint != null) {
          endpoint.addIceCandidate(candidate);
        }
    }

    public PlayerEndpoint constructPlayerEndpoint(String url) {

        return new PlayerEndpoint.Builder(pipe, url).build();
    }
    
}
