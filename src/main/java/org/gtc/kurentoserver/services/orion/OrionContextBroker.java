package org.gtc.kurentoserver.services.orion;

import javax.annotation.PreDestroy;

import org.gtc.kurento.orion.subscription.OrionSubscriptionManager;
import org.gtc.kurento.orion.subscription.entities.EntityPattern;
import org.gtc.kurento.orion.subscription.entities.SubscriptionRequest;
import org.gtc.kurento.orion.subscription.entities.SubscriptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Orion Context Broker subscription component
 */
@Component
@Async
public class OrionContextBroker {
    private static final Logger log = LoggerFactory.getLogger(OrionContextBroker.class);
    
    @Value( "${orion.notification.url}" )
    private String notificationUrl;

    private String subscriptionId;

    @Autowired
    private OrionSubscriptionManager subscriptionManager;

    /**
     * Unsubscribe to the orion
     */
    @PreDestroy
    public void unsubscribe() {
        if (subscriptionManager.unsubscribe(subscriptionId)) {
            log.trace("OrionContextBroker::destroy()");
            log.info("Unsubscribed succesfully");
        } else {
            log.info("Unsubscribed unsuccesfully");
        }
    }
    
    /**
     * Subscribe to the orion
     */
    @EventListener(ApplicationReadyEvent.class)
    public void subscribe() {
        log.trace("OrionContextBroker::init()");
        SubscriptionRequest req = new SubscriptionRequest();
        req.setDescription("Application Server subscription");
        req.setExpiringDate(java.sql.Date.valueOf("2022-07-30"));
        req.setThrottling(5);
        req.setNotificationUrl(notificationUrl);
        EntityPattern a = new EntityPattern();
        a.setIdPattern("gtc*");
        a.setType("Camera");
        req.addEntity(a);

        SubscriptionResponse subscribe = subscriptionManager.subscribe(req);
        subscriptionId = subscribe.getSubscriptionId();
    }


}
