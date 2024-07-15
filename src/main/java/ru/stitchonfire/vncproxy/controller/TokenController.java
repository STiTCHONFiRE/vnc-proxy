package ru.stitchonfire.vncproxy.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.stitchonfire.vncproxy.dto.TokenDto;
import ru.stitchonfire.vncproxy.service.TokenService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tokens")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenController {
    TokenService tokenService;

    @PostMapping("{id}")
    public Mono<TokenDto> getToken(@PathVariable String id, @AuthenticationPrincipal Principal principal) {
        return tokenService.generateToken(id, principal);
    }
}

