package org.gtc.kurentoserver.services.restful;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gtc.kurentoserver.api.SessionManager;
import org.gtc.kurentoserver.api.UserDAO;
import org.gtc.kurentoserver.dao.UserLocalSessionDAO;
import org.gtc.kurentoserver.dao.UserMongoDBDAO;
import org.gtc.kurentoserver.services.exceptions.UserNotFoundException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController("rest2")
@Order(10)
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserMongoDBDAO userDAO;

    @Autowired
    private SessionManager sessions;

    /**
     * Log the session with admin user
     * @param credentials Username and password
     */
    @PostMapping("/users/login")
    Map<String, String> login(@RequestBody @Validated String credentials) throws Exception {
        log.trace("UserController::login()");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            JsonNode jsonNode = objectMapper.readTree(credentials);
            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();

            if (userDAO.getUser(username, password) != null) {
                Map<String, String> map = new HashMap<>();
                map.put("accessToken", sessions.createSession(username));
                return map;
            }

            throw new UserNotFoundException(username);
        } catch (JsonMappingException e) {
            log.error("Error parsing credentials {}.", e.getMessage());
            throw new Exception("Error in the POST body");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new Exception("Error in the POST body");
        }
    }

    /**
     * Check if a token stills valid
     * @param sessionId JWT token
     */
    @GetMapping("/users/isLogged")
    boolean isLogged(@RequestHeader(value="x-access-token", required = false) String sessionId) {
        log.trace("UserController::isLogged()");

        if (sessionId == null)
            return false;

        return sessions.sessionAlive(sessionId);
    }


    /**
     * Log out session
     * @param sessionId JWT token
     */
    @PostMapping("/users/logout")
    boolean logOut(@RequestHeader(value="x-access-token") String sessionId) {
        log.trace("UserController::logout()");
        return true;
    }

}
