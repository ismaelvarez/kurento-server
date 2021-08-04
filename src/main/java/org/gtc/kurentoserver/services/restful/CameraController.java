package org.gtc.kurentoserver.services.restful;

import java.util.List;

import org.gtc.kurentoserver.services.restful.entities.Camera;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest service to provide camera information
 */
@RestController
public class CameraController {
	@CrossOrigin(origins = "*")
    @GetMapping("/cameras")
    List<Camera> all() {
        return Camera.getCameras();
    }
}
