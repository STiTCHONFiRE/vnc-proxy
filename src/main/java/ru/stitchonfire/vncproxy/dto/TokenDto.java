package ru.stitchonfire.vncproxy.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record TokenDto(
        UUID token,
        Instant expiredAt
) {
}
