package org.gtc.kurentoserver.services.restful;

import org.gtc.kurentoserver.api.SessionManager;
import org.gtc.kurentoserver.services.exceptions.NotAuthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController("ProcessingController")
public class ProcessingController {

    private static final Logger log = LoggerFactory.getLogger(ProcessingController.class);
    @Autowired
    private SessionManager sessions;

    @GetMapping("/processing/modules")
    @ResponseBody
    List<String> getModules(@RequestHeader(value="x-access-token") String sessionId) {
        log.trace("ProcessingController::getModules()");
        if (!sessions.sessionAlive(sessionId))
            throw new NotAuthenticatedException();

        return Arrays.asList("vehicleObjectDetection", "imageRecorder");
    }
}
