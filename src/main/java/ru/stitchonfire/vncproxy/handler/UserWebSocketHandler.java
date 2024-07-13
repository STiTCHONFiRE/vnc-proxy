package ru.stitchonfire.vncproxy.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.stitchonfire.vncproxy.service.TokenService;
import ru.stitchonfire.vncproxy.service.VncService;
import ru.stitchonfire.vncproxy.type.WebSocketType;

import java.net.URI;
import java.util.UUID;

@Slf4j
public record UserWebSocketHandler(
        UUID vncId,
        TokenService tokenService,
        VncService vncService,
        ObjectMapper objectMapper
) implements WebSocketHandler {

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        URI uri = session.getHandshakeInfo().getUri();

        String token = UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst("token");

        if (token == null || tokenService.authenticate(token, WebSocketType.USERS) || token.isBlank()) {
            return session.close(CloseStatus.SERVER_ERROR);
        }

        return session.send(
                vncService.getSink(vncId).asFlux().mapNotNull(userEventDto -> {
                    try {
                        return session.textMessage(objectMapper.writeValueAsString(userEventDto));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }

                    return null;
                })
        );
    }
}
