package org.gtc.kurentoserver.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesLoader {
  private static final Logger log = LoggerFactory.getLogger(PropertiesLoader.class);
  public static Properties applicationProperties;

  public static Properties loadApplicationProperties() throws IOException {
    if (applicationProperties == null) {
      applicationProperties = loadProperties("application.properties");
      log.info("================= Loading Application Properties ======================");
      applicationProperties.forEach((key, value) -> {
        log.info("\t{}={}", key, value);
      });
    }
    return applicationProperties;
  }

  public static Properties loadProperties(String resourceFileName) throws IOException {
      Properties configuration = new Properties();
      InputStream inputStream = PropertiesLoader.class
        .getClassLoader()
        .getResourceAsStream(resourceFileName);
      configuration.load(inputStream);
      inputStream.close();
      return configuration;
  }
}
