package ru.stitchonfire.vncproxy.dto;

import lombok.Builder;
import ru.stitchonfire.vncproxy.type.UserEventType;

@Builder
public record UserEventDto(
        String username,
        UserEventType eventType
) {

}
