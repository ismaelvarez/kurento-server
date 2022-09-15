package org.gtc.kurentoserver.services.orion.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.gtc.kurentoserver.model.Camera;
import org.gtc.kurentoserver.security.Encrytor;
import org.gtc.kurentoserver.services.orion.OrionEntitiesParser;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class OrionCameraEntityParser implements OrionEntitiesParser<Camera> {

    private final SecretKey key;
    private final IvParameterSpec ivParameterSpec;
    public OrionCameraEntityParser(SecretKey key, IvParameterSpec iv ) {
        this.key = key;
        ivParameterSpec = iv;
    }

    private final Map<String, String> SPECIAL_CHARACTERS = new HashMap<String, String>() {{
        put("<", "%3C");
        put(">", "%3E");
        put("\"", "%22");
        put("'", "%27");
        put("=", "%3D");
        put("(", "%28");
        put(")", "%29");
    }};

    @Override
    public Camera getEntityFrom(String entity) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaType type = objectMapper.getTypeFactory().
                constructType(Camera.class);

        JsonNode node = objectMapper.readTree(entity);

        ObjectNode camera = objectMapper.createObjectNode();

        if (!node.has("id") || !node.has("streamURL"))
            return null;

        camera.put("id", node.get("id").asText());

        Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> field = iterator.next();
            if (field.getValue() != null && field.getValue().get("value") != null && !field.getValue().get("value").isNull()) {
                if (field.getKey().equals("user") || field.getKey().equals("password")) {
                    String plainText = Encrytor.decrypt("AES/CBC/PKCS5Padding", fromURLEncoding(field.getValue().get("value").asText()), key, ivParameterSpec);
                    camera.put(field.getKey(), plainText);
                } else {
                    camera.set(field.getKey(), field.getValue().get("value"));
                }
            }
        }

        return objectMapper.readValue(fromURLEncoding(camera.toString()), type);
    }

    @Override
    public List<Camera> getEntitiesFrom(String entities) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode data = objectMapper.readTree(entities);

        List<Camera> listCamera = new ArrayList<>();

        for (JsonNode node : data) {
            listCamera.add(getEntityFrom(node.toString()));
        }
        return listCamera;
    }

    @Override
    public String getOrionEntityFrom(Camera entity) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try {

            return "{\n" +
                    "\"id\": \""+toURLEncoding(entity.getId())+"\",\n" +
                    "\"type\": \"Camera\",\n" +
                    "\"name\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getName()+"\"\n" +
                    "},\n" +
                    "\"cameraName\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getCameraName()+"\"\n" +
                    "},\n" +
                    "\"cameraType\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getCameraType()+"\"\n" +
                    "},\n" +
                    "\"cameraUsage\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getCameraUsage()+"\"\n" +
                    "},\n" +
                    "\"cameraMode\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getCameraMode()+"\"\n" +
                    "},\n" +
                    "\"panoramic\":{\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getPanoramic()+"\"\n" +
                    "},\n" +
                    "\"restrictive\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": "+entity.isRestrictive()+"\n" +
                    "},\n" +
                    "\"location\":{\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+entity.getLocation()+"\"\n" +
                    "},\n" +
                    "\"streamURL\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+toURLEncoding(entity.getStreamURL())+"\"\n" +
                    "},\n" +
                    "\"user\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\": \""+toURLEncoding(Encrytor.encrypt("AES/CBC/PKCS5Padding", entity.getUser(), key, ivParameterSpec))+"\"\n" +
                    "},\n" +
                    "\"password\": {\n" +
                    "\"type\": \"String\",\n" +
                    "\"value\" : \""+toURLEncoding(Encrytor.encrypt("AES/CBC/PKCS5Padding", entity.getPassword(), key, ivParameterSpec))+"\"\n" +
                    "},\n" +
                    "\"kurentoModules\": {\n" +
                    "\"type\": \"Map\",\n" +
                    "\"value\": "+objectMapper.writeValueAsString(entity.getKurentoModules())+"\n" +
                    "}\n" +
                    "}";
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String fromURLEncoding(String toEncode) {
        String encoded = toEncode;
        for (Map.Entry<String, String> entry : SPECIAL_CHARACTERS.entrySet()) {
            encoded = encoded.replace(entry.getValue(), entry.getKey());
        }
        return encoded;
    }

    private String toURLEncoding(String toEncode) {
        String encoded = toEncode;
        for (Map.Entry<String, String> entry : SPECIAL_CHARACTERS.entrySet()) {
            encoded = encoded.replace(entry.getKey(), entry.getValue());
        }
        return encoded;
    }

}
