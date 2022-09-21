package org.gtc.kurentoserver.services.orion;

import org.apache.commons.io.IOUtils;
import org.gtc.kurentoserver.model.Camera;
import org.gtc.kurentoserver.security.Encrytor;
import org.gtc.kurentoserver.services.orion.entities.EntityResults;
import org.gtc.kurentoserver.services.orion.parser.OrionCameraEntityParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Orion Context Broker subscription component
 */
@Component
public class OrionContextBroker {
    @Value("${orion.host}")
    private String orionHost;

    @Value("${fiware.service}")
    private String orionService;

    @Value("${fiware.service.path}")
    private String orionServicePath;

    @Value("${cypher.secret.key}")
    private String secretKey;

    @Value("${cypher.iv}")
    private String iv;
    private static final Logger log = LoggerFactory.getLogger(OrionContextBroker.class);
    OrionCameraEntityParser orionCameraEntityParser;

    @PostConstruct
    public void initParser() {
        orionCameraEntityParser = new OrionCameraEntityParser(Encrytor.getSecretKey(secretKey), Encrytor.getIV(iv));
    }

    /**
     * Search cameras
     */

    public EntityResults<Camera> getCamerasBy(String idPattern, String location, boolean restrictive, int limit, int offset) {
        log.trace("OrionContextBroker::getCamerasBy()");
        URL url = null;
        try {
            StringBuilder query = new StringBuilder();

            query.append("http://").append(orionHost).append(":1026/v2/entities?type=Camera");

            if (limit > 0) {
                query.append("&limit=").append(limit);
            }

            if (offset > 0) {
                query.append("&offset=").append(limit);
            }

            if (!location.equals(""))
                query.append("&q=location~=").append(location).append(";");

            if (!restrictive) {
                if (query.toString().contains("&q="))
                    query.append("restrictive==").append(false).append(";");
                else
                    query.append("&q=restrictive==").append(false).append(";");
            }

            if (!idPattern.equals("*"))
                query.append("&idPattern=").append(idPattern);

            query.append("&options=count&orderBy=dateCreated");

            url = new URL(query.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            addFiwareHeaders(connection);

            List<Camera> entitiesFrom = orionCameraEntityParser.getEntitiesFrom(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8));

            return new EntityResults<>(entitiesFrom, Integer.parseInt(connection.getHeaderField("Fiware-Total-Count")));

        } catch (Exception e) {
            return null;
        }
    }

    public EntityResults<Camera> getAll(int limit, int offset) {
        log.trace("OrionContextBroker::getAll()");
        URL url = null;
        try {
            StringBuilder query = new StringBuilder();
            query.append("http://").append(orionHost).append(":1026/v2/entities?type=Camera");
            if (limit > 0) {
                query.append("&limit=").append(limit);
            }

            if (offset > 0) {
                query.append("&offset=").append(limit);
            }

            query.append("&options=count");

            url = new URL(query.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            addFiwareHeaders(connection);


            List<Camera> entitiesFrom = orionCameraEntityParser.getEntitiesFrom(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8));

            return new EntityResults<>(entitiesFrom, Integer.parseInt(connection.getHeaderField("Fiware-Total-Count")));

        } catch (Exception e) {
            return null;
        }
    }

    public Camera getCamera(String cameraId) {
        log.trace("OrionContextBroker::getCamera()");
        URL url = null;
        try {
            url = new URL("http://" + orionHost + ":1026/v2/entities/" + cameraId + "?type=Camera");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("accept", "application/json");
            addFiwareHeaders(connection);
            return orionCameraEntityParser.getEntityFrom(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8));

        } catch (Exception e) {
            return null;
        }
    }

    public boolean createCamera(Camera camera) throws Exception {
        URL url = null;
        try {
            url = new URL("http://"+orionHost+":1026/v2/entities");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");
            addFiwareHeaders(http);
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
            url = new URL("http://"+orionHost+":1026/v2/entities/"+camera.getId()+"/attrs");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Accept", "application/json");
            http.setRequestProperty("Content-Type", "application/json");
            addFiwareHeaders(http);

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
            url = new URL("http://"+orionHost+":1026/v2/entities/"+id);

            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("DELETE");
            addFiwareHeaders(http);

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

    private void addFiwareHeaders(HttpURLConnection connection) {
        connection.setRequestProperty("fiware-service", orionService);
        connection.setRequestProperty("fiware-servicePath", orionServicePath);
    }
}
