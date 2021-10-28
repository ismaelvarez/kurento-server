package org.gtc.kurentoserver.services.restful.repository;

import java.util.ArrayList;
import java.util.List;

import org.gtc.kurentoserver.services.restful.entities.Camera;
import org.springframework.stereotype.Component;

/**
 * Camera Repository 
 */
@Component
public class CameraRepository {
    private List<Camera> cameras = new ArrayList<>();

    public void add(Camera camera) {
        cameras.add(camera);
    }

    public void delete(Camera camera) {
        cameras.remove(camera);
    }

    public void delete(String cameraId) {
        cameras.removeIf(cm ->  ( cm.getId().equals(cameraId)));
    }

    public Camera getCamera(String cameraId) {
        return cameras.stream().findFirst().filter(camera -> camera.getId().equals(cameraId)).get();
    }

    public List<Camera> getAll() {
        return cameras;
    }

    public boolean contains(String cameraId) {
        return cameras.stream().filter(camera -> camera.getId().equals(cameraId)).count() > 0;
    }

    
}
