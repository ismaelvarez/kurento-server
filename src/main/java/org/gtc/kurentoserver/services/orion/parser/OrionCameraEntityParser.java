package org.gtc.kurentoserver.services.orion.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.gtc.kurentoserver.entities.Camera;
import org.gtc.kurentoserver.services.orion.OrionEntitiesParser;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OrionCameraEntityParser implements OrionEntitiesParser<Camera> {
    @Override
    public List<Camera> getEntitiesFrom(String entities) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaType type = objectMapper.getTypeFactory().
                constructCollectionType(List.class, Camera.class);
        JsonNode data = objectMapper.readTree(entities);

        ArrayNode list = objectMapper.createArrayNode();

        for (JsonNode node : data) {
            if (!node.has("id") || !node.has("url"))
                continue;

            ObjectNode camera = objectMapper.createObjectNode();

            camera.put("id", node.get("id").asText());

            Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> field = iterator.next();
                if (field.getValue() != null && field.getValue().get("value") != null && !field.getValue().get("value").isNull()) {
                    camera.set(field.getKey(), field.getValue().get("value"));
                }
            }
            list.add(camera);
        }
        return objectMapper.readValue(list.toString(), type);
    }

    @Override
    public String getOrionEntityFrom(Camera entity) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        try {

            return "{\n" +
                    "\"id\": \""+entity.getId()+"\",\n" +
                    "\"type\": \"Camera\",\n" +
                    "\"cameraType\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getCameraType()+"\"\n" +
                    "},\n" +
                    "\"order\":{\n" +
                    "\"type\": \"Number\",\n" +
                    "\"value\": "+entity.getOrder()+"\n" +
                    "},\n" +
                    "\"panoramic\":{\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getPanoramic()+"\"\n" +
                    "},\n" +
                    "\"name\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getName()+"\"\n" +
                    "},\n" +
                    "\"group\":{\n" +
                    "\"type\": \"ArrayList\",\n" +
                    "\"value\": "+objectMapper.writeValueAsString(entity.getGroup())+"\n" +
                    "},\n" +
                    "\"url\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getUrl()+"\"\n" +
                    "},\n" +
                    "\"user\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getUser()+"\"\n" +
                    "},\n" +
                    "\"password\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\" : \""+entity.getPassword()+"\"\n" +
                    "},\n" +
                    "\"kurentoConfig\": {\n" +
                    "\"type\": \"Map\",\n" +
                    "\"value\": "+objectMapper.writeValueAsString(entity.getKurentoConfig())+"\n" +
                    "},\n" +
                    "\"description\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getDescription()+"\"\n" +
                    "}\n" +
                    "}";
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
