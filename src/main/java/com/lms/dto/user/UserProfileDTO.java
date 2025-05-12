package com.lms.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String telegram;
    private String avatar;
    private boolean emailVerified;
    private boolean telegramConnected;
    private Set<String> roles;
    private ZonedDateTime createdAt;
}