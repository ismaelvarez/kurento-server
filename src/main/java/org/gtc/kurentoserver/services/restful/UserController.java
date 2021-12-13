package org.gtc.kurentoserver.services.restful;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gtc.kurentoserver.dao.UserDAO;
import org.gtc.kurentoserver.entities.Camera;
import org.gtc.kurentoserver.services.authentification.SessionAuthentication;
import org.gtc.kurentoserver.services.exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@RestController("rest2")
@Order(10)
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private SessionAuthentication sessions;

    /**
     * Log the session with admin user
     * @param credentials User credentials
     * @throws JsonMappingException
     * @throws JsonProcessingException
     */
    @PostMapping("/users/login")
    boolean login(@RequestHeader(value="session-id", required = false) String sessionId,
                  @RequestBody @Validated String credentials) throws Exception {
        log.trace("UserController::login()");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            JsonNode jsonNode = objectMapper.readTree(credentials);
            String username = jsonNode.get("username").asText();
            String password = jsonNode.get("password").asText();

            if (userDAO.getUser(username, password) != null) {
                sessions.logIn(sessionId);
                return true;
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
     * Log the session with admin user
     * @param sessionId session-id
     */
    @PostMapping("/users/logout")
    boolean logOut(@RequestHeader(value="session-id", required = false) String sessionId) {
        log.trace("UserController::logout()");
        if (!sessions.isLogged(sessionId))
            sessions.logIn(sessionId);

        return true;
    }

}
