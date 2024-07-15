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
        log.info("Handling session {}", session);
        URI uri = session.getHandshakeInfo().getUri();

        String token = UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst("token");

        if (token == null || tokenService.authenticate(token, vncId, WebSocketType.USERS) || token.isBlank()) {
            log.info("error {}", session);
            return session.close(CloseStatus.SERVER_ERROR);
        }

        String username = this.tokenService.getUsername(token);
        if (username == null || username.isBlank()) {
            log.info("error {}", session);
            return session.close(CloseStatus.NOT_ACCEPTABLE);
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
                        .doOnSubscribe(s -> this.vncService.addUser(vncId, username))
                        .doFinally(s -> this.vncService.removeUser(vncId, username))
        );
    }
}
