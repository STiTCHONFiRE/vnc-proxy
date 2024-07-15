package ru.stitchonfire.vncproxy.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.stitchonfire.vncproxy.dto.TokenDto;
import ru.stitchonfire.vncproxy.model.TokenData;
import ru.stitchonfire.vncproxy.type.WebSocketType;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenService {
    Map<UUID, TokenData> tokens = new HashMap<>();

    public Mono<TokenDto> generateToken(String id, Principal principal) {
        UUID tokenValue = UUID.randomUUID();
        UUID vncId = UUID.fromString(id);
        Instant expiredAt = Instant.now().plus(3, ChronoUnit.MINUTES);
        tokens.put(
                tokenValue,
                TokenData.builder()
                        .username(principal.getName())
                        .vncId(vncId)
                        .usersIsAuth(false)
                        .vncIsAuth(false)
                        .expiredAt(expiredAt)
                        .build()
        );

        return Mono.delay(Duration.ofSeconds(3)).then(Mono.just(
                TokenDto.builder()
                        .token(tokenValue)
                        .queryParameterName("token")
                        .expiredAt(expiredAt)
                        .build()
        ));
    }

    public String getUsername(String token) {
        return tokens.get(UUID.fromString(token)).getUsername();
    }

    public boolean authenticate(String token, UUID vncId, WebSocketType type) {
        UUID tokenValue = UUID.fromString(token);
        if (!tokens.containsKey(tokenValue)) {
            log.info("unknown token: {}", token);
            return false;
        }

        TokenData tokenData = tokens.get(tokenValue);

        if (Instant.now().isAfter(tokenData.getExpiredAt())) {
            log.info("expired token: {}", token);
            tokens.remove(tokenValue);
            return false;
        }

        if (!tokenData.getVncId().equals(vncId)) {
            return false;
        }

        if (type.equals(WebSocketType.VNC)) {
            if (tokenData.isVncIsAuth()) {
                return false;
            }

            tokenData.setVncIsAuth(true);
        }

        if (type.equals(WebSocketType.USERS)) {
            if (tokenData.isVncIsAuth()) {
                return false;
            }

            tokenData.setUsersIsAuth(true);
        }

        if (tokenData.isVncIsAuth() && tokenData.isUsersIsAuth()) {
            tokens.remove(tokenValue);
        }

        return true;
    }
}
