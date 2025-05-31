package com.mozilla.curriculum_tracking_system.service.user;

import com.mozilla.curriculum_tracking_system.constants.RoleConstants;
import com.mozilla.curriculum_tracking_system.dto.user.CreateUserRequest;
import com.mozilla.curriculum_tracking_system.dto.email.UserCredentialsEmailData;
import com.mozilla.curriculum_tracking_system.dto.user.AssignRoleRequest;
import com.mozilla.curriculum_tracking_system.dto.user.UserResponse;
import com.mozilla.curriculum_tracking_system.exception.BadRequestException;
import com.mozilla.curriculum_tracking_system.exception.ResourceNotFoundException;
import com.mozilla.curriculum_tracking_system.mapper.UserMapper;
import com.mozilla.curriculum_tracking_system.model.roles.Role;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.roles.RoleRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import com.mozilla.curriculum_tracking_system.service.email.IEmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService implements IUserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final IEmailService emailService;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        validateCreateUserRequest(request);

        validateUserUniqueness(request);

        try {
            User user = userMapper.toEntity(request, passwordEncoder.encode(request.getPassword()));

            if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
                Role role = findRoleByName(request.getRoleName());
                user.addRole(role);
            }

            User savedUser = userRepository.save(user);
            sendCredentialsEmail(savedUser, request.getPassword());
            return userMapper.toResponse(savedUser);
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to create user due to an unexpected error");
        }
    }

    @Override
    @Transactional
    public UserResponse assignRole(AssignRoleRequest request) {
        validateAssignRoleRequest(request);

        try {
            User user = findUserById(request.getUserId());
            Role role = findRoleByName(request.getRoleName());
            boolean userHasRole = user.getRoles().stream()
                    .anyMatch(existingRole -> existingRole.getName().equals(request.getRoleName()));

            if (userHasRole) {
                throw new BadRequestException("User already has the role: " + request.getRoleName());
            }

            user.addRole(role);
            User savedUser = userRepository.saveAndFlush(user);

            return userMapper.toResponse(savedUser);
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to assign role due to an unexpected error");
        }
    }

    @Override
    @Transactional
    public UserResponse removeRole(Long userId, String roleName) {
        if (userId == null) {
            throw new BadRequestException("User ID is required");
        }
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new BadRequestException("Role name is required");
        }

        try {
            User user = findUserById(userId);

            Role roleToRemove = user.getRoles().stream()
                    .filter(role -> role.getName().equals(roleName))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("User does not have the role: " + roleName));

            validateAdminRoleRemoval(roleName, user);

            user.removeRole(roleToRemove);

            User savedUser = userRepository.save(user);
            return userMapper.toResponse(savedUser);

        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to remove role due to an unexpected error");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return userMapper.toResponseList(users);
        } catch (Exception e) {
            throw new BadRequestException("Failed to retrieve users");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User ID is required");
        }

        User user = findUserById(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new BadRequestException("Role name is required");
        }
        findRoleByName(roleName);

        try {
            List<User> users = userRepository.findByRolesName(roleName);
            return userMapper.toResponseList(users);
        } catch (Exception e) {
            throw new BadRequestException("Failed to retrieve users by role");
        }
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean enabled) {
        if (userId == null) {
            throw new BadRequestException("User ID is required");
        }

        try {
            User user = findUserById(userId);

            validateAdminStatusUpdate(user, enabled);

            user.setEnabled(enabled);
            User savedUser = userRepository.save(user);

            return userMapper.toResponse(savedUser);
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to update user status due to an unexpected error");
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User ID is required");
        }

        try {
            User user = findUserById(userId);

            validateAdminDeletion(user);
            user.clearRoles();
            userRepository.flush();

            userRepository.delete(user);
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to delete user due to an unexpected error");
        }
    }

    private void validateCreateUserRequest(CreateUserRequest request) {
        if (request == null) {
            throw new BadRequestException("User creation request is required");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
        if (request.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters long");
        }
    }

    private void validateAssignRoleRequest(AssignRoleRequest request) {
        if (request == null) {
            throw new BadRequestException("Role assignment request is required");
        }
        if (request.getUserId() == null) {
            throw new BadRequestException("User ID is required");
        }
        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            throw new BadRequestException("Role name is required");
        }
    }

    private void validateUserUniqueness(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });
    }

    private Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Role not found: " + roleName);
                });
    }

    private void validateAdminRoleRemoval(String roleName, User user) {
        if (RoleConstants.ADMIN.equals(roleName)) {
            try {
                long adminCount = userRepository.countUsersWithRole(RoleConstants.ADMIN);
                boolean userIsAdmin = user.getRoles().stream()
                        .anyMatch(role -> RoleConstants.ADMIN.equals(role.getName()));

                if (adminCount <= 1 && userIsAdmin) {
                    throw new BadRequestException("Cannot remove admin role from the last admin user");
                }
            } catch (BadRequestException e) {
                throw e;
            } catch (Exception e) {
                throw new BadRequestException("Failed to validate admin role removal");
            }
        }
    }

    private void validateAdminStatusUpdate(User user, boolean enabled) {
        if (!enabled && isUserAdmin(user)) {
            try {
                long enabledAdminCount = userRepository.countEnabledUsersWithRole(RoleConstants.ADMIN);
                if (enabledAdminCount <= 1) {
                    throw new BadRequestException("Cannot disable the last admin user");
                }
            } catch (BadRequestException e) {
                throw e;
            } catch (Exception e) {
                throw new BadRequestException("Failed to validate admin status update");
            }
        }
    }

    private void validateAdminDeletion(User user) {
        if (isUserAdmin(user)) {
            try {
                long adminCount = userRepository.countUsersWithRole(RoleConstants.ADMIN);
                if (adminCount <= 1) {
                    throw new BadRequestException("Cannot delete the last admin user");
                }
            } catch (BadRequestException e) {
                throw e;
            } catch (Exception e) {
                throw new BadRequestException("Failed to validate admin deletion");
            }
        }
    }

    private boolean isUserAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> RoleConstants.ADMIN.equals(role.getName()));
    }

    @Override
    public boolean isCurrentUser(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            String username = userDetails.getUsername();

            User currentUser = userRepository.findActiveUserByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
            return currentUser.getId().equals(userId);
        }
        return false;
    }

    private void sendCredentialsEmail(User user, String plainPassword) {
        try {
            String roleName = user.getRoles().isEmpty() ? "User" : user.getRoles().iterator().next().getName();

            UserCredentialsEmailData credentialsEmailData = UserCredentialsEmailData.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .password(plainPassword)
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roleName(roleName)
                    .loginUrl(null)
                    .build();

            emailService.sendUserCredentialsEmail(credentialsEmailData);
        } catch (Exception e) {
        }
    }

}