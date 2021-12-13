package org.gtc.kurentoserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.gtc.kurentoserver.entities.Camera;
import org.gtc.kurentoserver.services.orion.parser.OrionCameraEntityParser;
import org.json.JSONException;
import org.junit.jupiter.api.Test;


public class CameraTest {

	
    @Test
	void contextLoads() throws JSONException, IOException {
        InputStream f = getClass().getResourceAsStream("Notification.json");
        
        try {
            String everything = readFromInputStream(f);
            OrionCameraEntityParser parser = new OrionCameraEntityParser();

            List<Camera> cameras = parser.getEntitiesFrom(everything);
    
            assertEquals(cameras.size(), 1);
    
            Camera c = cameras.get(0);

            assertEquals("interior", c.getId());
    
            assertEquals(c.getCameraType(), "stream");

        } catch (Exception e) {
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
