package org.gtc.kurentoserver.services.orion.publisher;

import org.gtc.kurentoserver.services.orion.entities.TrafficFlowObserved;
import org.kurento.module.cardetector.CarsDetectedEvent;
import org.kurento.orion.connector.OrionConnectorConfiguration;
import org.kurento.orion.publisher.DefaultOrionPublisher;

public class TrafficFlowObservedPublisher extends DefaultOrionPublisher<CarsDetectedEvent, TrafficFlowObserved> {

    public TrafficFlowObservedPublisher(OrionConnectorConfiguration config) {
        super(config, TrafficFlowObserved.class);
    }

    @Override
    public TrafficFlowObserved mapEntityToOrionEntity(CarsDetectedEvent event) {
        return new TrafficFlowObserved(event.getCarsDetected(), event.getIdCam(), event.getIdCam());
    }
    
}
