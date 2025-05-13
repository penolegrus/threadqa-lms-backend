package com.threadqa.lms.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String profilePicture;
    private String bio;
    private String phoneNumber;
    private Set<String> roles;
}