package org.gtc.kurentoserver.services.orion;

import org.gtc.kurento.orion.notification.OrionNotification;

public interface OrionNotificationParser<T> {

    OrionNotification<T> getEntitiesFrom(String notification) throws Exception;
    
}
