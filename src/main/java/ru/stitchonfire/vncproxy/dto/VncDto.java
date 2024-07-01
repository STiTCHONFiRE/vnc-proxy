package ru.stitchonfire.vncproxy.dto;

import lombok.Builder;

@Builder
public record VncDto(
        String id,
        String ipAddressAndPort
) {
}
