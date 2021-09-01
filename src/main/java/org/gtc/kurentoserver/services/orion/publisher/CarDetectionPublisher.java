package org.gtc.kurentoserver.services.orion.publisher;

import org.gtc.kurentoserver.services.orion.entities.CarsDetected;
import org.kurento.module.cardetector.CarsDetectedEvent;
import org.kurento.orion.connector.OrionConnectorConfiguration;
import org.kurento.orion.connector.entities.event.MediaEvent;
import org.kurento.orion.connector.entities.event.MediaEventOrionPublisher;

public class CarDetectionPublisher extends MediaEventOrionPublisher<CarsDetectedEvent> {

    public CarDetectionPublisher(OrionConnectorConfiguration config) {
        super(config);
    }

    @Override
    public MediaEvent mapEntityToOrionEntity(CarsDetectedEvent event) {
        CarsDetected orion_entity = new CarsDetected(event.getCarsDetected(), event.getIdCam());
        return orion_entity;
    }
    
}
