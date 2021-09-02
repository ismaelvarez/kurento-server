package org.gtc.kurentoserver.services.orion.entities;


import org.kurento.orion.connector.entities.OrionEntity;

public class CarsDetectedEntity implements OrionEntity {

    int numCars;
    String idCam;
    String id;
    String type;

    public CarsDetectedEntity(int numCars, String idCam) {
        this.numCars = numCars;
        this.idCam = idCam;
        id = "CarDetection_"+idCam;
        type = "CarDetection";
    }

    public int getNumCars() {
        return numCars;
    }

    public String getIdCam() {
        return idCam;
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
