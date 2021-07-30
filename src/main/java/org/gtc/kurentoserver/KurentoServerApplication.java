package org.gtc.kurentoserver;

import org.gtc.kurentoserver.handler.PipelineHandler;
import org.gtc.kurentoserver.pipeline.MultiCameraWithFilterPipeline;
import org.gtc.kurentoserver.pipeline.PipelineOld;
import org.gtc.kurentoserver.pipeline.ViewerPipeline;
import org.gtc.kurentoserver.pipeline.AbstractPipeline;
import org.kurento.client.KurentoClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@SpringBootApplication
@EnableWebSocket
public class KurentoServerApplication implements WebSocketConfigurer {

	@Bean
	public WebSocketConfigurer getWebSocketConfigurer() {
		return this;
	}
	
	@Bean
		public KurentoClient kurentoClient() {
		return KurentoClient.create();
		}

	@Bean
	public PipelineOld pipeline() {
		return new PipelineOld();
	}

	@Bean
	public AbstractPipeline multiCameraWithFilterPipeline() {
		return new MultiCameraWithFilterPipeline();
	}

	public AbstractPipeline gtcViewer() {
		return new ViewerPipeline("");
	}

	public AbstractPipeline gtc2Viewer() {
		return new ViewerPipeline("");
	}

	@Bean
	public PipelineHandler handler(AbstractPipeline pipeline) {
		return new PipelineHandler(pipeline);
	}


	@Bean
	public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(32768);
		return container;
	}

	public static void main(String[] args) {
		SpringApplication.run(KurentoServerApplication.class, args);
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler(multiCameraWithFilterPipeline()), "/kurento");
		registry.addHandler(handler(gtcViewer()), "/gtcExt");
		registry.addHandler(handler(gtc2Viewer()), "/gtcExt2");
	}
	
	}
