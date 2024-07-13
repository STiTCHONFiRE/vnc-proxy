package ru.stitchonfire.vncproxy.model;

import lombok.Builder;

import java.time.Instant;

@Builder
public record TokenData(
        String username,
        Instant expiredAt
) {

}
