package org.gtc.kurentoserver.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Camera {
    private String url;
    private String name;
    @JsonDeserialize(as=ArrayList.class, contentAs=String.class)
    private List<String> group;
    private String description;
    private String id;
    private String user;
    @JsonIgnoreProperties(ignoreUnknown = true) 
    private String cameraType = "Stream";

    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;
    @JsonSerialize(keyAs = String.class, contentAs = Boolean.class)
    private Map<String, Boolean> kurentoConfig;

    public Camera(){}

    public Camera(String id, String name, String description, String url) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, Boolean> getKurentoConfig() {
        return kurentoConfig;
    }

    public void setKurentoConfig(Map<String, Boolean> kurentoConfig) {
        this.kurentoConfig = kurentoConfig;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty(access = Access.WRITE_ONLY)
    public String getUrlWithCredentials() {
        if (user.equals("")) {
            return url;
        }
        int index = url.indexOf("://") + 3;
        String credentials = user + ":" + password + "@";
        return url.substring(0, index) + credentials + url.substring(index);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<String> getGroup() {
		return group;
	}

	public void setGroup(List<String> group) {
		this.group = group;
	}


    public String getCameraType() {
        return cameraType;
    }

    public void setCameraType(String cameraType) {
        this.cameraType = cameraType;
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
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Camera [description=" + description + ", group=" + group + ", id=" + id + ", name=" + name + "]";
    }

    
    
}
