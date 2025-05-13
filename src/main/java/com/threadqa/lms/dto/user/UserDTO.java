package com.threadqa.lms.dto.user;

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
public class UserDTO {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private String bio;
    private String phoneNumber;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastLoginAt;
    private Set<String> roles;
}
