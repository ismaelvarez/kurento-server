package org.gtc.kurentoserver.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        return ocb.getCamera(cameraId);
    }

    public EntityResults<Camera> getAll(int limit, int offset) {
        return ocb.getAll(limit, offset);
    }

    public EntityResults<Camera> getBy(String idPattern, String location, boolean restrictive, int limit, int offset) {
        EntityResults<Camera> cameras = ocb.getCamerasBy(idPattern, location, restrictive, limit, offset);
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
