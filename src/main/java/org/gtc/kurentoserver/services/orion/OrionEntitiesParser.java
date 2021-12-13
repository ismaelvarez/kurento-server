package org.gtc.kurentoserver.services.orion;

import org.gtc.kurento.orion.notification.OrionNotification;

import java.util.List;

public interface OrionEntitiesParser<T> {

    List<T> getEntitiesFrom(String notification) throws Exception;

    String getOrionEntityFrom(T entity) throws Exception;
    
}
