package org.gtc.kurentoserver.services.orion;

import java.util.List;

public interface OrionNotificationParser<T> {

    List<T> getEntitiesFrom(String notification) throws Exception;
    
}
