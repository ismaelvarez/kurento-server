package org.gtc.kurentoserver.services.restful;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.gtc.kurentoserver.KurentoServerHelper;
import org.gtc.kurentoserver.services.orion.CameraOrionNotificationParser;
import org.gtc.kurentoserver.services.restful.entities.Camera;
import org.gtc.kurentoserver.services.restful.repository.CameraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest service to provide camera information
 */
@RestController("rest")
@Order(0)
public class CameraController {
    private static final Logger log = LoggerFactory.getLogger(CameraController.class);

    @Autowired
    private KurentoServerHelper kurentoServerHelper;
    private CameraOrionNotificationParser notificationParser = new CameraOrionNotificationParser();
    
    @Autowired
    private CameraRepository repository;

    private Long lastNotification = 0L;


    @PostConstruct
    public void init() {
        log.trace("CameraController::init");
    }

    /**
     * List all cameras
     * @param groups If provided, list all cameras with that group
     * @return list of cameras
     */
	@CrossOrigin(origins = "*")
    @GetMapping("/cameras")
    @ResponseBody
    List<Camera> all(@RequestParam Optional<String> groups) {
        log.trace("CameraController::all()");
        if (groups.isPresent()) {
            List<String> list = Arrays.asList(groups.get().split(","));
            return repository.getAll().stream().filter(c-> !Collections.disjoint(c.getGroup(), list)).collect(Collectors.toList());
        }
        return repository.getAll();
    }

    /**
     * Get camera by id
     * @param id Id of camera
     * @return camera
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/camera/{id}")
    Camera getCamera(@PathVariable("id") String id) {
        log.trace("CameraController::all()");
        return repository.getCamera(id);
    }

    /**
     * Orion notification path. Creates or updates cameras
     * @param payload Orion notification
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @CrossOrigin(origins = "0.0.0.0:1026")
    @PostMapping("/cameras")
    void notification(@RequestBody @Validated String payload) throws JsonMappingException, JsonProcessingException {
        log.trace("CameraController::notification({})", payload);
        log.info("New notification from Orion: {}", payload);

        if (System.currentTimeMillis() - lastNotification < 3000) return;

        lastNotification = System.currentTimeMillis();

    	List<Camera> cameras = notificationParser.getEntitiesFrom(payload);
        for (Camera camera : cameras) {
            if (!kurentoServerHelper.contains(camera.getId())) {
                log.info("Created {}", camera.toString());
                repository.add(camera);
                kurentoServerHelper.createPipelineWithCamera(camera);
            } else {
                log.info("Updated {}", camera.toString());
                repository.delete(camera);
                repository.add(camera);
                kurentoServerHelper.reloadPipelineOfCamera(camera);
            }
        }
    }

    /**
     * Delete camera
     * @param id id of camera
     */
	@CrossOrigin(origins = "*")
    @DeleteMapping("/camera/{id}")
    void deleteCamera(@PathVariable("id") String id) {
        log.trace("CameraController::deleteCamera({})", id);
        repository.delete(id);
        kurentoServerHelper.deletePipelineOfCamera(id);
    }
    

    
}
