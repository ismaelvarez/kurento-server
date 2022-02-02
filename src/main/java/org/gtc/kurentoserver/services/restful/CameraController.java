package org.gtc.kurentoserver.services.restful;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.gtc.kurentoserver.KurentoServerHelper;
import org.gtc.kurentoserver.dao.CameraDAO;
import org.gtc.kurentoserver.entities.Camera;
import org.gtc.kurentoserver.services.authentification.SessionAuthentication;
import org.gtc.kurentoserver.services.exceptions.NotAuthenticatedException;
import org.gtc.kurentoserver.services.orion.OrionContextBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Rest service to provide camera information
 */
@RestController("rest")
@Order(0)
public class CameraController {
    private static final String STREAM = "stream";

    private static final Logger log = LoggerFactory.getLogger(CameraController.class);

    @Autowired
    private KurentoServerHelper kurentoServerHelper;

    @Autowired
    private OrionContextBroker ocb;
    
    @Autowired
    private CameraDAO repository;

    @Autowired
    private SessionAuthentication sessions;

    @PostConstruct
    public void init() {
        log.trace("CameraController::init");
    }

    /**
     * List all cameras
     * @param groups If provided, list all cameras with that group
     * @return list of cameras
     */
    @GetMapping("/cameras")
    @ResponseBody
    List<Camera> all(@RequestParam Optional<String> groups,
                     @RequestHeader(value="session-id", required = false) String sessionId) {

        boolean isLogged = sessions.isLogged(sessionId);
        log.trace("CameraController::all()");
        if (groups.isPresent()) {
            List<String> list = Arrays.asList(groups.get().split(","));
            return repository.getAll().stream().filter(c-> !Collections.disjoint(c.getGroup(), list)).filter(c-> isLogged || c.isRestrictive()).collect(Collectors.toList());
        }

        return repository.getAll().stream().filter(c-> isLogged || c.isRestrictive()).collect(Collectors.toList());
    }

    /**
     * Get camera by id
     * @param id Id of camera
     * @return camera
     */
    @GetMapping("/cameras/{id}")
    Camera getCamera(@PathVariable("id") String id) {
        log.trace("CameraController::all()");
        if (!repository.contains(id))
            throw new EntityNotFoundException();
        return repository.getCamera(id);
    }

    /**
     * Delete camera
     * @param id id of camera
     */
    @DeleteMapping("/cameras/{id}")
    @CrossOrigin("*")
    void deleteCamera(@RequestHeader(value="session-id", required = false) String sessionId, @PathVariable("id") String id) {
        log.trace("CameraController::deleteCamera({})", id);

        if (!sessions.isLogged(sessionId))
            throw new NotAuthenticatedException();

        if (!repository.contains(id)) {
            throw new EntityNotFoundException();
        }

        try {
            if (ocb.deleteCamera(id)) {
                repository.delete(id);
                kurentoServerHelper.deletePipelineOfCamera(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/cameras")
    @ResponseBody
    @CrossOrigin("*")
    void create(@RequestHeader(value="session-id", required = false) String sessionId, @RequestBody @Validated Camera camera) {
        log.trace("CameraController::create()");
        log.info("Create camera {}; Session={}", camera.getId(), sessionId);
        if (!sessions.isLogged(sessionId))
            throw new NotAuthenticatedException();

        if (repository.contains(camera.getId())) {
            throw new EntityExistsException();
        }

        try {
            if (ocb.createCamera(camera)) {
                repository.add(camera);
                if (camera.getCameraType().equalsIgnoreCase(STREAM)) {
                    kurentoServerHelper.createPipelineWithCamera(camera);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @PatchMapping("/cameras")
    @CrossOrigin("*")
    void update(@RequestHeader(value="session-id", required = false) String sessionId, @RequestBody @Validated Camera camera) {
        log.trace("CameraController::update()");

        if (!sessions.isLogged(sessionId))
            throw new NotAuthenticatedException();

        if (!repository.contains(camera.getId())) {
            throw new EntityExistsException();
        }

        Camera camera1 = repository.getCamera(camera.getId());

        if (camera.getPassword().equals(""))
            camera.setPassword(camera1.getPassword());

        camera.setOrder(camera1.getOrder());

        try {
            if (ocb.updateCamera(camera)) {
                repository.update(camera);
                if (kurentoServerHelper.contains(camera.getId())) {
                    kurentoServerHelper.deletePipelineOfCamera(camera);
                }
                if (camera.getCameraType().equalsIgnoreCase(STREAM)) {
                    if (kurentoServerHelper.contains(camera.getId()))
                        kurentoServerHelper.createPipelineWithCamera(camera);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
