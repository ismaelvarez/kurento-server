package org.gtc.kurentoserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.gtc.kurentoserver.services.orion.entities.OrionNotification;
import org.gtc.kurentoserver.services.orion.notification.CameraOrionNotificationParser;
import org.gtc.kurentoserver.services.restful.entities.Camera;
import org.json.JSONException;
import org.junit.jupiter.api.Test;


public class CameraTest {

	
    @Test
	void contextLoads() throws JSONException, IOException {
        InputStream f = getClass().getResourceAsStream("Notification.json");
        
        try {

            String everything = readFromInputStream(f);
            CameraOrionNotificationParser parser = new CameraOrionNotificationParser();

            OrionNotification<Camera> notification = parser.getEntitiesFrom(everything);
    
            assertEquals(notification.getId(), "57458eb60962ef754e7c0998");
    
            assertEquals(1, notification.getEntities().size());
    
            Camera c = notification.getEntities().get(0);
    
            assertEquals(c.getCameraType(), "Static");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
        = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
    
}
