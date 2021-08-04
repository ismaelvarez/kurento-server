package org.gtc.kurentoserver.pipeline;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Manager of all the pipelines active in the server
 */
@Component
public class PipelineManager {
    private static final Logger log = LoggerFactory.getLogger(PipelineManager.class);

    private ConcurrentMap<String, KurentoPipeline> pipelines;

    public PipelineManager() {
        pipelines = new ConcurrentHashMap<>();
    }

    /**
     * Add new pipeline with an identifier
     * @param id Identifier of the pipeline
     * @param pipeline Pipeline
     */
    public void add(String id, KurentoPipeline pipeline) {
        if (!pipelines.containsKey(id)) {
            pipelines.put(id, pipeline);
            pipeline.construct();
        }
    }

    public KurentoPipeline get(String id) {
        return pipelines.getOrDefault(id, null);
    }

    /**
     * Remove and release the WebRTC of an pipeline
     * @param session WebSocket session
     * @param id Pipeline identifier
     */
    public void releaseWebRTCOfSession(String session, String id){
        pipelines.get(id).removeWebRtcEndpoint(session);
    }

    /**
     * Remove and release all WebRTC associated with a WebSocket session 
     * @param session
     */
    public void releaseAllWebRTCOf(String session){
        pipelines.forEach((k, v) -> {
            v.removeWebRtcEndpoint(session);
        });
    }

    /**
     * Releases all pipelines when closing manager
     */
    @PreDestroy
    public void disconnectPipelines() {
        log.info("Releasing all pipelines");
        for (Entry<String, KurentoPipeline> entry : pipelines.entrySet()) {
            entry.getValue().release();
        }
    }
    
    
}
