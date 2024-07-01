package ru.stitchonfire.vncproxy.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

@Slf4j
public record VncWebSocketHandler(String TCP_SERVER_HOST, int TCP_SERVER_PORT) implements WebSocketHandler {
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return TcpClient.create().host(TCP_SERVER_HOST).port(TCP_SERVER_PORT).connect()
                .doOnSubscribe(subscription -> log.info("Connecting to TCP server {}:{}", TCP_SERVER_HOST, TCP_SERVER_PORT))
                .doOnSuccess(connection -> log.info("Connected to TCP server {}:{}", TCP_SERVER_HOST, TCP_SERVER_PORT))
                .doOnError(error -> log.error("Error connecting to TCP server: {}", error.getMessage()))
                .flatMap(connection -> {
                    // WebSocket => TCP
                    Mono<Void> inbound = session.receive()
                            .doOnSubscribe(subscription -> log.info("Receiving messages from WebSocket session: {}", session.getId()))
                            .doOnComplete(() -> {
                                log.info("WebSocket session completed: {}", session.getId());
                                log.info("TCP connection {}:{} is disposing", TCP_SERVER_HOST, TCP_SERVER_PORT);
                                connection.dispose();
                            })
                            .doOnError(error -> {
                                log.error("Error in WebSocket session {}: {}", session.getId(), error.getMessage());
                                connection.dispose();
                            })
                            .map(WebSocketMessage::getPayload)
                            .flatMap(buffer -> {
                                byte[] bytes = new byte[buffer.capacity()];
                                buffer.read(bytes);
                                return connection.outbound()
                                        .sendByteArray(Mono.just(bytes))
                                        .then();
                            })
                            .then();

                    // TCP => WebSocket
                    Mono<Void> outbound = connection.inbound().receive()
                            .doOnSubscribe(subscription -> log.info("Receiving messages from TCP connection"))
                            .doOnComplete(() -> log.info("TCP connection {}:{} completed", TCP_SERVER_HOST, TCP_SERVER_PORT))
                            .doOnError(error -> log.error("Error in TCP connection: {}", error.getMessage()))
                            .flatMap(buffer -> {
                                byte[] bytes = new byte[buffer.readableBytes()];
                                buffer.readBytes(bytes);
                                return session.send(Mono.just(
                                        session.binaryMessage(
                                                dataBufferFactory ->
                                                        dataBufferFactory.wrap(bytes)
                                        )
                                ));
                            })
                            .onErrorResume(error -> session.close(CloseStatus.SERVER_ERROR))
                            .then();

                    return Mono.when(inbound, outbound)
                            .doFinally(signal -> {
                                log.info("WebSocket session closed: {}", session.getId());
                                log.info("TCP is disposed: {}", connection.isDisposed());
                            });
                }).onErrorResume(error -> {
                    log.error("Closing WebSocket session with id {} due to error: {}", session.getId(), error.getMessage());
                    return session.close(CloseStatus.SERVER_ERROR);
                });
    }
}