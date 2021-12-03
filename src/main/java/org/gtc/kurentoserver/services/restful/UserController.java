package org.gtc.kurentoserver.services.restful;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gtc.kurentoserver.dao.UserDAO;
import org.gtc.kurentoserver.entities.Camera;
import org.gtc.kurentoserver.services.authentification.SessionAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController("rest2")
@Order(10)
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserDAO userDAO;


    /**
     * Log the session with admin user
     * @param credentials User credentials
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @PostMapping("/users/login")
    boolean login(HttpSession session, @RequestBody @Validated String credentials) {
        log.trace("UserController::login()");

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            JsonNode jsonNode = objectMapper.readTree(credentials);
            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();
            /*if (userDAO.getUser(username, password) != null) {
                if (!sessions.isLogged(session.getId()))
                    sessions.logIn(session.getId());

                return true;
            }*/

            return false;
        } catch (JsonMappingException e) {
            log.error("Error parsing credentials {}.", e.getMessage());
            return false;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }
/*
    /**
     * Log the session with admin user
     * @param credentials User credentials
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    /*@PostMapping("/users/logout")
    boolean logOut(HttpSession session) {
        log.trace("UserController::logout()");
        if (!sessions.isLogged(session.getId()))
            sessions.logIn(session.getId());

        return true;
    }*/

}
