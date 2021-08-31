package org.gtc.kurentoserver.services.orion.entities;

import org.kurento.orion.connector.entities.event.MediaEvent;

public class CarsDetected extends MediaEvent {

    int numCars;
    String idCam;
    String id;

    public CarsDetected(int numCars, String idCam) {
        this.numCars = numCars;
        this.idCam = idCam;
        id = "CarDetected_"+idCam;
        this.setType("CarDetection");
        this.setId(id);
    }

    public int getNumCars() {
        return numCars;
    }

    public String getIdCam() {
        return idCam;
    }
}
