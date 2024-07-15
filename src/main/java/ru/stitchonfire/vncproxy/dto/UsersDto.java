package ru.stitchonfire.vncproxy.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record UsersDto(Set<String> usernames) {

}
