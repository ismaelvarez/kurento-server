package org.gtc.kurentoserver;

import org.gtc.kurento.orion.subscription.OrionSubscriptionManager;
import org.kurento.client.KurentoClient;
import org.kurento.orion.connector.OrionConnectorConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
@ComponentScan
public class KurentoApplicationServer {
	@Value("${kurento.address}")
	private String kurentoAddress;

	@Bean
	public KurentoClient kurentoClient() {
		return KurentoClient.create();
	}

	@Bean
	public OrionSubscriptionManager orionSubscriptionManager() {
		return new OrionSubscriptionManager(new OrionConnectorConfiguration());
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/kurento").allowedOrigins("*");
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(KurentoApplicationServer.class, args);
	}
	
}
