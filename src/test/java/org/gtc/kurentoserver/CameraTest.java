package org.gtc.kurentoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

import org.gtc.kurentoserver.model.Camera;
import org.gtc.kurentoserver.services.orion.parser.OrionCameraEntityParser;
import org.json.JSONException;
import org.junit.Test;

import static org.junit.Assert.*;


public class CameraTest {

	
    @Test
	public void contextLoads() throws JSONException, IOException {
        InputStream f = Objects.requireNonNull(this.getClass().getClassLoader().getResource("Notification.json")).openStream();
        
        /*try {
            String everything = readFromInputStream(f);
            OrionCameraEntityParser parser = new OrionCameraEntityParser();

            List<Camera> cameras = parser.getEntitiesFrom(everything);
    
            assertEquals(cameras.size(), 1);
    
            Camera c = cameras.get(0);

            assertEquals("interior", c.getId());
    
            assertEquals(c.getCameraType(), "stream");
            assertTrue(c.isRestrictive());

        } catch (Exception e) {
            e.printStackTrace();
        }*/
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
