package org.gtc.kurentoserver.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.gtc.kurentoserver.entities.Camera;
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

    @PostConstruct
    private void getRestrictedCameras() {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(restrictedCamerasPath);
            restrictedCameras = Arrays.asList(IOUtils.toString(fis, StandardCharsets.UTF_8).split("\n"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> restrictedCameras = new ArrayList<>();

    private List<Camera> cameras = new ArrayList<>();

    public void add(Camera camera) {
        camera.setRestrictive(false);
        if (restrictedCameras.contains(camera.getUrl())) {
            camera.setRestrictive(true);
        }
        cameras.add(camera);
    }

    public void delete(Camera camera) {
        cameras.remove(camera);
    }

    public void delete(String cameraId) {
        cameras.removeIf(cm ->  ( cm.getId().equals(cameraId)));
    }

    public Camera getCamera(String cameraId) {
        return cameras.stream().filter(camera -> camera.getId().equals(cameraId)).findFirst().get();
    }

    public List<Camera> getAll() {
        return cameras;
    }

    public boolean contains(String cameraId) {
        return cameras.stream().filter(camera -> camera.getId().equals(cameraId)).count() > 0;
    }

    public void update(Camera camera) {
        cameras.remove(camera);
        add(camera);
    }

    
}
