package ru.stitchonfire.vncproxy.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import reactor.core.publisher.Mono;
import ru.stitchonfire.vncproxy.dto.VncDto;
import ru.stitchonfire.vncproxy.handler.VncWebSocketHandler;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor()
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VncService {
    SimpleUrlHandlerMapping urlHandlerMapping;

    public Mono<List<VncDto>> getVncData() {
        return Mono.delay(Duration.ofSeconds(1)).then(Mono.just(urlHandlerMapping.getUrlMap().entrySet()
                .stream()
                .map(e -> {
                    VncWebSocketHandler handler = (VncWebSocketHandler) e.getValue();
                    return VncDto.builder()
                            .id(e.getKey().substring(4))
                            .ipAddressAndPort(handler.TCP_SERVER_HOST() + ":" + handler.TCP_SERVER_PORT())
                            .build();
                })
                .toList()
        ));
    }
}
