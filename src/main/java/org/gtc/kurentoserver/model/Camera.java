package org.gtc.kurentoserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Camera {
    private String id;
    private String streamURL;
    private String name;
    private String cameraName;
    private String cameraType;
    private String cameraUsage;
    private String location;
    private String user;
    private String cameraMode = "Stream";
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;
    @JsonDeserialize(as=ArrayList.class, contentAs=String.class)
    private List<String> kurentoModules;
    private boolean restrictive;
    private String panoramic;

    public Camera(){}

    public Camera(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStreamURL() {
        return streamURL;
    }

    public void setStreamURL(String streamURL) {
        this.streamURL = streamURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getCameraType() {
        return cameraType;
    }

    public void setCameraType(String cameraType) {
        this.cameraType = cameraType;
    }

    public String getCameraUsage() {
        return cameraUsage;
    }

    public void setCameraUsage(String cameraUsage) {
        this.cameraUsage = cameraUsage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCameraMode() {
        return cameraMode;
    }

    public void setCameraMode(String cameraMode) {
        this.cameraMode = cameraMode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getKurentoModules() {
        return kurentoModules;
    }

    public void setKurentoModules(List<String> kurentoModules) {
        this.kurentoModules = kurentoModules;
    }

    public boolean isRestrictive() {
        return restrictive;
    }

    public void setRestrictive(boolean restrictive) {
        this.restrictive = restrictive;
    }

    public String getPanoramic() {
        return panoramic;
    }

    public void setPanoramic(String panoramic) {
        this.panoramic = panoramic;
    }

    @JsonProperty(access = Access.WRITE_ONLY)
    public String getUrlWithCredentials() {
        if (user.equals("")) {
            return streamURL;
        }
        int index = streamURL.indexOf("://") + 3;
        String credentials = user + ":" + password + "@";
        return streamURL.substring(0, index) + credentials + streamURL.substring(index);
    }

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Camera other = (Camera) obj;
        if (id == null) {
            return other.id == null;
        } else return id.equals(other.id);
    }

    @Override
    public String toString() {
        return "Camera [name=" + name + ", location=" + location + ", id=" + id + ", cameraName=" + cameraName + "]";
    }

}
