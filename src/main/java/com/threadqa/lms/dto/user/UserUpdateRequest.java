package com.threadqa.lms.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String firstName;
    private String lastName;
    private String bio;
    private String phoneNumber;
    private String profilePicture;
    private String password;
}