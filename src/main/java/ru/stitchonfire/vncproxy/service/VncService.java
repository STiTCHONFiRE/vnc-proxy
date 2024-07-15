package ru.stitchonfire.vncproxy.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import ru.stitchonfire.vncproxy.dto.UserEventDto;
import ru.stitchonfire.vncproxy.dto.UsersDto;
import ru.stitchonfire.vncproxy.dto.VncDto;
import ru.stitchonfire.vncproxy.handler.VncWebSocketHandler;
import ru.stitchonfire.vncproxy.type.UserEventType;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VncService {
    List<WebSocketHandler> webSocketHandlers = new ArrayList<>();
    Map<UUID, Set<String>> connectedUsers = new HashMap<>();
    Map<UUID, Sinks.Many<UserEventDto>> sinks = new HashMap<>();

    public Mono<List<VncDto>> getVncData() {
        return Mono.delay(Duration.ofSeconds(1)).then(Mono.just(webSocketHandlers
                .stream()
                .filter(v -> v instanceof VncWebSocketHandler)
                .map(v -> {
                    VncWebSocketHandler handler = (VncWebSocketHandler) v;
                    return VncDto.builder()
                            .id(handler.vncId().toString())
                            .ipAddressAndPort(handler.tcpServerHost() + ":" + handler.tcpServerPort())
                            .build();
                })
                .toList()
        ));
    }

    public Mono<UsersDto> getUsers(String id) {
        UUID uuid = UUID.fromString(id);
        return Mono.just(
                UsersDto.builder()
                        .usernames(connectedUsers.get(uuid))
                        .build()
        );
    }

    public Sinks.Many<UserEventDto> getSink(UUID id) {
        sinks.putIfAbsent(id, Sinks.many().replay().latest());
        return sinks.get(id);
    }

    public void addUser(UUID id, String username) {
        connectedUsers.putIfAbsent(id, new HashSet<>());
        connectedUsers.get(id).add(username);

        this.getSink(id).tryEmitNext(
                UserEventDto.builder()
                        .username(username)
                        .eventType(UserEventType.CONNECT)
                        .build()
        );
    }

    public void removeUser(UUID id, String username) {
        connectedUsers.get(id).remove(username);

        this.getSink(id).tryEmitNext(
                UserEventDto.builder()
                        .username(username)
                        .eventType(UserEventType.DISCONNECT)
                        .build()
        );
    }

    public void addWebSocketHandler(WebSocketHandler webSocketHandler) { webSocketHandlers.add(webSocketHandler); }
}
