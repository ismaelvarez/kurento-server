package org.gtc.kurentoserver.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.gtc.kurentoserver.model.Camera;
import org.gtc.kurentoserver.services.orion.entities.EntityResults;
import org.gtc.kurentoserver.services.orion.OrionContextBroker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Camera Repository 
 */
@Component
public class CameraDAO {

    @Value( "${camera.restricted}" )
    private String restrictedCamerasPath;

    @Autowired
    private OrionContextBroker ocb;

    @PostConstruct
    private void getRestrictedCameras() {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(restrictedCamerasPath);
            restrictedCameras = Arrays.asList(IOUtils.toString(fis, StandardCharsets.UTF_8).split("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> restrictedCameras = new ArrayList<>();

    public boolean add(Camera camera) throws Exception {
        if (restrictedCameras.contains(camera.getStreamURL())) {
            camera.setRestrictive(true);
        }

        return ocb.createCamera(camera);
    }

    public void delete(Camera camera) throws Exception {
        ocb.deleteCamera(camera.getId());
    }

    public boolean delete(String cameraId) throws Exception {
        return ocb.deleteCamera(cameraId);
    }

    public Camera getCamera(String cameraId) {
        Camera camera = ocb.getCamera(cameraId);
        if (camera == null) return null;

        for (String restrictiveCamera : restrictedCameras)
            if (camera.getStreamURL().equals(restrictiveCamera))
                camera.setRestrictive(true);

        return camera;
    }

    public EntityResults<Camera> getAll(int limit, int offset) {
        return ocb.getAll(limit, offset);
    }

    public EntityResults<Camera> getBy(String idPattern, String cameraName, String cameraType, String cameraUsage,
                                       String cameraMode, String location, Boolean restrictive, Boolean panoramic,
                                       String streamURL, int limit, int offset) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(cameraName, cameraName);
        attributes.put("cameraUsage", cameraUsage);
        attributes.put("cameraType", cameraType);
        attributes.put("cameraMode", cameraMode);
        attributes.put("location", location);
        if (restrictive == null) attributes.put("restrictive", null);
        else attributes.put("restrictive", Boolean.toString(restrictive));

        if (panoramic == null) attributes.put("panoramic", null);
        else attributes.put("panoramic", Boolean.toString(panoramic));

        attributes.put("streamURL", streamURL);

        EntityResults<Camera> cameras = ocb.getCamerasBy(idPattern, attributes, limit, offset);
        if (cameras == null) {
            return new EntityResults<>(new ArrayList<>(), 0);
        }
        List<Camera> toRemove = new ArrayList<>();
        for (String restrictiveCamera : restrictedCameras) {
            for (Camera c : cameras.getResults()) {
                if (c.getStreamURL().equals(restrictiveCamera) && !restrictive)
                    toRemove.add(c);
            }
        }
        List<Camera> results = cameras.getResults();
        for (Camera c : toRemove) {
            results.remove(c);
        }

        return cameras;

    }

    public boolean contains(String cameraId) {
        return ocb.getCamera(cameraId) != null;
    }

    public boolean update(Camera camera) throws Exception {
        return ocb.updateCamera(camera);
    }

    
}
