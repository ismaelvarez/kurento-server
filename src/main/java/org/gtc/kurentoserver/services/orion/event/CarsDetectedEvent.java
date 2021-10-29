package org.gtc.kurentoserver.services.orion.event;

import org.kurento.orion.connector.entities.event.MediaEvent;

public class CarsDetectedEvent extends MediaEvent {

    int numCars;
    String idCam;
    String id;

    public CarsDetectedEvent(int numCars, String idCam) {
        this.numCars = numCars;
        this.idCam = idCam;
        id = "CarDetection_"+idCam;
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
