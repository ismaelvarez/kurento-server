package org.gtc.kurentoserver.services.orion.publisher;

import org.gtc.kurentoserver.services.orion.entities.CarsDetectedEntity;
import org.kurento.module.cardetector.CarsDetectedEvent;
import org.kurento.orion.connector.OrionConnectorConfiguration;
import org.kurento.orion.publisher.DefaultOrionPublisher;

public class CarDetectionPublisher extends DefaultOrionPublisher<CarsDetectedEvent, CarsDetectedEntity> {

    public CarDetectionPublisher(OrionConnectorConfiguration config) {
        super(config, CarsDetectedEntity.class);
    }

    @Override
    public CarsDetectedEntity mapEntityToOrionEntity(CarsDetectedEvent event) {
        CarsDetectedEntity orion_entity = new CarsDetectedEntity(event.getCarsDetected(), event.getIdCam());
        return orion_entity;
    }
    
}
