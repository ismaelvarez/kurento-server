package org.gtc.kurentoserver.services.restful.entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.core.io.ClassPathResource;

public class Camera {

    public static List<Camera> getCameras() {
        if (cameras != null) {
            return cameras;
        }
        List<Camera> cameras = new ArrayList<>();
        try {
            ClassPathResource classPathResource = new ClassPathResource("urls.txt", Camera.class.getClassLoader());
            
            Files.lines(Paths.get(classPathResource.getURI())).forEachOrdered(line->{
                String[] parameters = line.split("=");
                if (parameters.length == 4 && !parameters[0].startsWith("#")) {
                    cameras.add(new Camera(parameters[0], parameters[1], parameters[2], parameters[3]));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } 

        return cameras;
    }

    private static List<Camera> cameras = null;
        
    @JsonIgnore
    private String url;
    private Object descripion;
    private String name;
    private String id;

    public Camera(String id, String name, String description, String url) {
        this.id = id;
        this.name = name;
        this.descripion = description;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getDescripion() {
        return descripion;
    }

    public void setDescripion(Object descripion) {
        this.descripion = descripion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
}
