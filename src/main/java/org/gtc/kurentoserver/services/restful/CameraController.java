package org.gtc.kurentoserver.services.restful;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import org.gtc.kurentoserver.KurentoServerHelper;
import org.gtc.kurentoserver.api.SessionManager;
import org.gtc.kurentoserver.dao.CameraDAO;
import org.gtc.kurentoserver.model.Camera;
import org.gtc.kurentoserver.services.orion.entities.EntityResults;
import org.gtc.kurentoserver.services.exceptions.NotAuthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Rest service to provide camera information
 */
@RestController("rest")
@Order(0)
public class CameraController {
    private static final String STREAM = "STREAM";

    private static final Logger log = LoggerFactory.getLogger(CameraController.class);

    @Autowired
    private KurentoServerHelper kurentoServerHelper;
    
    @Autowired
    private CameraDAO repository;

    @Autowired
    private SessionManager sessions;

    @PostConstruct
    public void init() {
        log.trace("CameraController::init");
    }



    /**
     * List all cameras
     * @param limit Number of cameras to be returned
     * @param offset Number of page
     * @return list of cameras
     */
    @GetMapping("/cameras")
    @ResponseBody
    EntityResults<Camera> getAll(@RequestParam Optional<Integer> limit, @RequestParam Optional<Integer> offset,
                     @RequestHeader(value="x-access-token", required = false) String sessionId) {

        log.trace("CameraController::getAll()");
        boolean isLogged = sessionId != null && sessions.sessionAlive(sessionId);

        return repository.getBy("*", new ArrayList<>(), isLogged, limit.orElse(0), offset.orElse(0));

     }

    /**
     * Get cameras by filters
     * @param limit Number of cameras to be returned
     * @param offset Number of page
     * @param name If provided, list all cameras with that id like idPattern
     * @param locations If provided, list all cameras with that location
     * @return list of cameras
     */
    @GetMapping("/cameras/search")
    @ResponseBody
    EntityResults<Camera> getBy(@RequestParam String name, @RequestParam Optional<String> locations, @RequestParam Optional<Integer> limit, @RequestParam Optional<Integer> offset,
                              @RequestHeader(value="x-access-token", required = false) String sessionId) {

        log.trace("CameraController::all()");
        boolean isLogged = sessionId != null && sessions.sessionAlive(sessionId);
        List<String> list = new ArrayList<>();

        if (locations.isPresent())
            list = Arrays.asList(locations.orElse("").split(","));

        return repository.getBy(name, list, isLogged, limit.orElse(0), offset.orElse(0));
    }

    /**
     * Get camera by id
     * @param id Identification of camera
     * @return camera
     */
    @GetMapping("/cameras/{id}")
    Camera getCamera(@PathVariable("id") String id,
                     @RequestHeader(value="x-access-token") String sessionId) {
        log.trace("CameraController::all()");

        if (!sessions.sessionAlive(sessionId))
            throw new NotAuthenticatedException();

        Camera camera = repository.getCamera(id);
        if (camera == null)
            throw new EntityNotFoundException();
        return camera;
    }

    /**
     * Delete camera from the OCB and remove the pipeline
     * @param id id of camera
     */
    @DeleteMapping("/cameras/{id}")
    @CrossOrigin("*")
    void deleteCamera(@RequestHeader(value="x-access-token") String sessionId, @PathVariable("id") String id) {
        log.trace("CameraController::deleteCamera({})", id);

        if (!sessions.sessionAlive(sessionId))
            throw new NotAuthenticatedException();

        if (!repository.contains(id)) {
            throw new EntityNotFoundException();
        }

        try {
            if (repository.delete(id)) {
                kurentoServerHelper.deletePipelineOfCamera(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create camera from the OCB and create the pipeline
     * @param camera Camara information to be inserted in OCB
     */
    @PostMapping("/cameras")
    @ResponseBody
    @CrossOrigin("*")
    void create(@RequestHeader(value="x-access-token") String sessionId, @RequestBody @Validated Camera camera) {
        log.trace("CameraController::create()");

        if (!sessions.sessionAlive(sessionId))
            throw new NotAuthenticatedException();

        if (repository.contains(camera.getId())) {
            throw new EntityExistsException();
        }

        try {
            if (repository.add(camera)) {
                if (camera.getCameraMode().equalsIgnoreCase(STREAM)) {
                    kurentoServerHelper.createPipelineWithCamera(camera);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update camera from the OCB and update the pipeline
     * @param camera Camara information to be updated in OCB
     */
    @PatchMapping("/cameras")
    @CrossOrigin("*")
    void update(@RequestHeader(value="x-access-token") String sessionId, @RequestBody @Validated Camera camera) {
        log.trace("CameraController::update()");

        if (!sessions.sessionAlive(sessionId))
            throw new NotAuthenticatedException();

        if (!repository.contains(camera.getId())) {
            throw new EntityNotFoundException();
        }

        Camera camera1 = repository.getCamera(camera.getId());

        if (camera.getPassword() == null)
            camera.setPassword(camera1.getPassword());

        //camera.setOrder(camera1.getOrder());

        try {
            if (repository.update(camera)) {
                if (kurentoServerHelper.contains(camera.getId())) {
                    kurentoServerHelper.deletePipelineOfCamera(camera);
                }
                if (camera.getCameraMode().equalsIgnoreCase(STREAM)) {
                    if (!kurentoServerHelper.contains(camera.getId()))
                        kurentoServerHelper.createPipelineWithCamera(camera);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
