package org.gtc.kurentoserver.services.orion;

import org.apache.commons.io.IOUtils;
import org.gtc.kurentoserver.entities.Camera;
import org.gtc.kurentoserver.services.orion.parser.OrionCameraEntityParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orion Context Broker subscription component
 */
@Component
public class OrionContextBroker {
    private static final Logger log = LoggerFactory.getLogger(OrionContextBroker.class);
    OrionCameraEntityParser orionCameraEntityParser = new OrionCameraEntityParser();

    /**
     * Subscribe to the orion
     */
    public List<Camera> getCameras() {
        log.trace("OrionContextBroker::getCameras()");
        URL url = null;
        try {
            url = new URL("http://localhost:1026/v2/entities?type=Camera");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");


            return orionCameraEntityParser.getEntitiesFrom(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public boolean createCamera(Camera camera) throws Exception {
        URL url = null;
        try {
            url = new URL("http://localhost:1026/v2/entities");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");


            byte[] out = orionCameraEntityParser.getOrionEntityFrom(camera).getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);
            if (http.getResponseCode() == 201) {
                http.disconnect();
                return true;
            }
            throw new Exception("Could not create entity in OCB. " + http.getResponseCode() + " " + http.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCamera(Camera camera) throws Exception {
        URL url = null;
        try {
            url = new URL("http://localhost:1026/v2/entities/"+camera.getId()+"/attrs");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Accept", "application/json");
            http.setRequestProperty("Content-Type", "application/json");

            String orionEntityFrom = orionCameraEntityParser.getOrionEntityFrom(camera);
            String tmp = " { \"" + orionEntityFrom.substring(orionEntityFrom.indexOf("cameraType"));
            byte[] out = tmp.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);
            if (HttpStatus.NO_CONTENT.value() == http.getResponseCode()) {
                http.disconnect();
                return true;
            }
            throw new Exception("Could not create entity in OCB. " + http.getResponseCode() + " " + http.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean deleteCamera(String id) throws Exception {
        URL url = null;
        try {
            url = new URL("http://localhost:1026/v2/entities/"+id);

            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("DELETE");

            if (HttpStatus.NO_CONTENT.value() == http.getResponseCode()) {
                http.disconnect();
                return true;
            }
            throw new Exception("Could not delete entity in OCB. " + http.getResponseCode() + " " + http.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
