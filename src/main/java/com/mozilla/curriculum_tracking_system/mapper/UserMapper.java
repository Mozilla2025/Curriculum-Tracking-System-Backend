package com.mozilla.curriculum_tracking_system.mapper;

import com.mozilla.curriculum_tracking_system.dto.user.CreateUserRequest;
import com.mozilla.curriculum_tracking_system.dto.user.UserResponse;
import com.mozilla.curriculum_tracking_system.model.roles.Role;
import com.mozilla.curriculum_tracking_system.model.user.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request, String encodedPassword) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encodedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
    }

    /**
     * Maps UserResponse DTO to User entity
     * Note: This creates a User without roles populated and without password
     * You may need to fetch roles separately if needed
     *
     * @param userResponse the UserResponse DTO
     * @return User entity
     */
    public User toEntity(UserResponse userResponse) {
        return User.builder()
                .id(userResponse.getId())
                .username(userResponse.getUsername())
                .email(userResponse.getEmail())
                .firstName(userResponse.getFirstName())
                .lastName(userResponse.getLastName())
                .phoneNumber(userResponse.getPhoneNumber())
                .isEnabled(userResponse.isEnabled())
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .createdAt(userResponse.getCreatedAt())
                .updatedAt(userResponse.getUpdatedAt())
                // Note: password is not included in UserResponse for security reasons
                // roles are not mapped here as UserResponse contains role names, not Role entities
                .build();
    }
    /**
     * Maps User entity to UserResponse DTO
     *
     * @param user the user entity
     * @return UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .enabled(user.isEnabled())
                .roles(mapRolesToNames(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Maps list of User entities to list of UserResponse DTOs
     *
     * @param users list of user entities
     * @return list of UserResponse DTOs
     */
    public List<UserResponse> toResponseList(List<User> users) {
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps Set of Roles to Set of role names
     *
     * @param roles set of role entities
     * @return set of role names
     */
    private Set<String> mapRolesToNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
