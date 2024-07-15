package ru.stitchonfire.vncproxy.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenData {
    String username;
    UUID vncId;
    boolean vncIsAuth;
    boolean usersIsAuth;
    Instant expiredAt;

}
