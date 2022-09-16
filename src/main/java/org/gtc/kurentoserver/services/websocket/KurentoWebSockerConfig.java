package org.gtc.kurentoserver.services.websocket;

import org.gtc.kurentoserver.services.websocket.handler.WebRTCProtocolHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.Map;

@Configuration
@EnableWebSocket
public class KurentoWebSockerConfig implements WebSocketConfigurer {
	
	@Bean
	public WebRTCProtocolHandler handler() {
		return new WebRTCProtocolHandler();
	}

	@Bean
	public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(32768);
		return container;
	}

	@Bean
	public HandshakeInterceptor auctionInterceptor() {
		return new HandshakeInterceptor() {
			@Override
			public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
										   org.springframework.http.server.ServerHttpResponse response,
										   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
				// Get the URI segment corresponding to the auction id during handshake
				String path = request.getURI().getPath();
				String auctionId = path.substring(path.lastIndexOf('/') + 1);

				// This will be added to the websocket session
				attributes.put("token", auctionId);
				return true;
			}

			@Override
			public void afterHandshake(org.springframework.http.server.ServerHttpRequest request, org.springframework.http.server.ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

			}
		};
	}


	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler(), "/kurento/{token}").setAllowedOrigins("*")
				.addInterceptors(auctionInterceptor());
	}

	
    
}
