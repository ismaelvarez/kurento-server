package org.gtc.kurentoserver.services.pipeline;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Manager of all the pipelines active in the server
 */
@Component
@EnableScheduling
public class PipelineManager {
    private static final Logger log = LoggerFactory.getLogger(PipelineManager.class);

    private ConcurrentMap<String, WebRtcPipeline> pipelines;

    public PipelineManager() {
        pipelines = new ConcurrentHashMap<>();
    }

    /**
     * Add new pipeline with an identifier
     * @param id Identifier of the pipeline
     * @param pipeline Pipeline
     */
    public void add(String id, WebRtcPipeline pipeline) {
        if (!pipelines.containsKey(id)) {
            try {
                pipelines.put(id, pipeline);
                pipeline.construct();
            } catch (Exception e) {
                pipelines.remove(id);
            }
        }
    }

    public WebRtcPipeline get(String id) {
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

    public void disconnectPipeline(String pipelineId) {
        if (!pipelines.containsKey(pipelineId)) return;
        
        pipelines.get(pipelineId).release();
        pipelines.remove(pipelineId);
		log.info("Pipeline with id {} disconnected...", pipelineId);
    }

    /**
     * Releases all pipelines when closing manager
     */
    @PreDestroy
    public void disconnectPipelines() {
        log.info("Releasing all pipelines");
        for (Entry<String, WebRtcPipeline> entry : pipelines.entrySet()) {
            entry.getValue().release();
        }
        pipelines.clear();
    }

    @Scheduled(fixedRate = 5000)
    public void restorePipelines() {
        for (Entry<String, WebRtcPipeline> entry : pipelines.entrySet()) {
            if (!entry.getValue().isPlaying()) {
                log.info("Pipeline with id {} is currently offline. Trying to reconnect...", entry.getKey());
                entry.getValue().release();
                try {
                    entry.getValue().construct();
                } catch (Exception e) {
                    log.error("Pipeline with id {} could not be created", entry.getKey());
                }
            }
        }
    }
    
    
}
