package com.lms.mapper;

import com.lms.dto.user.UserDTO;
import com.lms.dto.user.UserProfileDTO;
import com.lms.model.Role;
import com.lms.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserDTO toUserDTO(User user);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserProfileDTO toUserProfileDTO(User user);

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}