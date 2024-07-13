package ru.stitchonfire.vncproxy.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.stitchonfire.vncproxy.dto.TokenDto;
import ru.stitchonfire.vncproxy.model.TokenData;
import ru.stitchonfire.vncproxy.type.WebSocketType;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenService {
    Map<UUID, TokenData> tokens = new HashMap<>();

    public Mono<TokenDto> generateToken(Principal principal) {
        UUID tokenValue = UUID.randomUUID();
        Instant expiredAt = Instant.now().plus(3, ChronoUnit.MINUTES);
        tokens.put(tokenValue, TokenData.builder()
                .username(principal.getName())
                .expiredAt(expiredAt)
                .build()
        );

        return Mono.just(
                TokenDto.builder()
                        .token(tokenValue)
                        .expiredAt(expiredAt)
                        .build()
        );
    }

    public String getUsername(String token) {
        return tokens.get(UUID.fromString(token)).username();
    }

    public boolean authenticate(String token, WebSocketType type) {
        UUID tokenValue = UUID.fromString(token);
        if (!tokens.containsKey(tokenValue)) {
            return false;
        }

        TokenData tokenData = tokens.get(tokenValue);

        if (type.equals(WebSocketType.VNC)) {
            tokens.remove(tokenValue);
        }

        return !Instant.now().isBefore(tokenData.expiredAt());
    }
}
