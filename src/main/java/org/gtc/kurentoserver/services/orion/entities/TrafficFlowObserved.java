package org.gtc.kurentoserver.services.orion.entities;


import org.kurento.orion.connector.entities.OrionEntity;

import java.util.Date;

public class TrafficFlowObserved implements OrionEntity {

    String id;
    String type;

    private final int intensity;

    private final Date dateObserved;

    private final String name;

    private final String owner;
    int numCars;
    String idCam;

    public TrafficFlowObserved(int intensity, String name, String owner) {
        this.intensity = intensity;
        this.owner = owner;
        this.name = name;
        this.dateObserved = new Date();
        id = owner+"_TrafficFlowObserved";
        type = "TrafficFlowObserved";
    }

    public int getIntensity() {
        return intensity;
    }

    public Date getDateObserved() {
        return dateObserved;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
