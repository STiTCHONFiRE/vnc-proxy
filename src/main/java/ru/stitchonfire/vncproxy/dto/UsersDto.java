package ru.stitchonfire.vncproxy.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public class UsersDto {
    Set<String> usernames;
}
