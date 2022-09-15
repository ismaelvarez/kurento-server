package org.gtc.kurentoserver;

import org.gtc.kurento.orion.subscription.OrionSubscriptionManager;
import org.gtc.kurentoserver.api.SessionManager;
import org.gtc.kurentoserver.services.authentification.JWTAuthentication;
import org.kurento.client.KurentoClient;
import org.kurento.orion.connector.OrionConnectorConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@ServletComponentScan
@SpringBootApplication
@EnableWebSocket
public class KurentoApplicationServer {
	@Value("${kurento.ws.url}")
	private String kurentoAddress;
	@Value("${orion.host}")
	private String orionHost;

	@Bean
	public KurentoClient kurentoClient() {
		return KurentoClient.create(kurentoAddress);
	}

	@Bean
	public OrionConnectorConfiguration orionConnectorConfiguration() {
		OrionConnectorConfiguration orionConnectorConfiguration = new OrionConnectorConfiguration();
		orionConnectorConfiguration.setOrionHost(orionHost);
		return orionConnectorConfiguration;
	}

	@Bean
	public OrionSubscriptionManager orionSubscriptionManager() {
		return new OrionSubscriptionManager(orionConnectorConfiguration());
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry
						.addMapping("/**").allowedOrigins("*")
						.allowedHeaders("*", "token")
						.exposedHeaders("session-alive")
						.allowedMethods("POST", "PATCH","GET","DELETE");
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(KurentoApplicationServer.class, args);
	}
	
}
