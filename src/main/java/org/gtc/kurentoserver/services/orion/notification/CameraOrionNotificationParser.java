package org.gtc.kurentoserver.services.orion.notification;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.gtc.kurento.orion.notification.OrionNotification;
import org.gtc.kurentoserver.entities.Camera;
import org.gtc.kurentoserver.services.orion.OrionNotificationParser;

public class CameraOrionNotificationParser implements OrionNotificationParser<Camera> {

    /**
     * Parse Orion notification JSON to Camera Objects
     */
    @Override
    public OrionNotification<Camera> getEntitiesFrom(String notification) throws JsonMappingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        String id = objectMapper.readTree(notification).get("subscriptionId").asText();
        JsonNode data = objectMapper.readTree(notification).get("data");

        JavaType type = objectMapper.getTypeFactory().
            constructCollectionType(List.class, Camera.class);


        ArrayNode cameras = normalice(objectMapper, data);

    	return new OrionNotification<Camera>(id, objectMapper.readValue(cameras.toString(), type));
    }
    
    private ArrayNode normalice(ObjectMapper mapper, JsonNode data) {
        ArrayNode list = mapper.createArrayNode();

        for (JsonNode node : data) {
            if (!node.has("id") || !node.has("url"))
                continue;

            ObjectNode camera = mapper.createObjectNode();

            camera.put("id", node.get("id").asText());

            Iterator<Entry<String, JsonNode>> iterator = node.fields();
            while (iterator.hasNext()) {
                Entry<String, JsonNode> field = iterator.next();
                if (field.getValue() != null && field.getValue().get("value") != null && !field.getValue().get("value").isNull()) {
                    camera.set(field.getKey(), field.getValue().get("value"));
                }
            }
            list.add(camera);
        }

        return list;
    }
    
}
