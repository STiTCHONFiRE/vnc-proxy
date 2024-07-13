package ru.stitchonfire.vncproxy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import ru.stitchonfire.vncproxy.handler.UserWebSocketHandler;
import ru.stitchonfire.vncproxy.handler.VncWebSocketHandler;
import ru.stitchonfire.vncproxy.service.TokenService;
import ru.stitchonfire.vncproxy.service.VncService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableWebFlux
public class WebSocketConfig implements WebFluxConfigurer {

    @Bean
    public SimpleUrlHandlerMapping handlerMapping(
            @Value("${vnc.servers}") List<String> ips,
            TokenService tokenService,
            VncService vncService,
            ObjectMapper objectMapper
    ) {
        Map<String, WebSocketHandler> map = new HashMap<>();

        ips.forEach(ip -> {
            String[] split = ip.split(":");
            UUID uuid = UUID.randomUUID();
            map.put("/ws/" + uuid, new VncWebSocketHandler(uuid, split[0], Integer.parseInt(split[1]), tokenService, vncService));
            map.put("/ws/users/" + uuid, new UserWebSocketHandler(uuid, tokenService, vncService, objectMapper));
        });

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(10);
        handlerMapping.setUrlMap(map);
        return handlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
