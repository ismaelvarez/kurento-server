package org.gtc.kurentoserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.gtc.kurentoserver.services.restful.entities.Camera;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;


public class CameraTest {

	static String json = "{\"data\" : [{\"id\":\"gtcInt\",\"type\":\"Camera\",\"url\":{\"type\":\"String\",\"value\":\"ggggg\",\"metadata\":{}}}, {\"id\":\"gtcInt\",\"type\":\"Camera\",\"url\":{\"type\":\"String\",\"value\":\"ggggg\",\"metadata\":{}}}]}";

    @Test
	void contextLoads() throws JsonMappingException, JsonProcessingException, JSONException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode data = objectMapper.readTree(json).get("data");
        JavaType type = objectMapper.getTypeFactory().
            constructCollectionType(List.class, Camera.class);
        String a = normalice(data).get("data").toString();
    	List<Camera> cameras = objectMapper.readValue(a, type);

		assertEquals(2, cameras.size());
    }

    private static JSONObject normalice(JsonNode data) throws JSONException {
        List<Map<String, String>> list = new ArrayList<>();
        for (JsonNode node : data) {
            Map<String, String> map = new HashMap<>();
            map.put("id", node.get("id").asText());
            map.put("url", node.get("url").get("value").asText());
            map.put("description", "des");
            map.put("name", "name");
            list.add(map);
        }
        JSONObject jsonArray = new JSONObject();
        jsonArray.put("data", list);
        return jsonArray;
    }
    
}
