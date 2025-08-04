package com.mozilla.curriculum_tracking_system.service.user;

import com.mozilla.curriculum_tracking_system.dto.user.AssignRoleRequest;
import com.mozilla.curriculum_tracking_system.dto.user.CreateUserRequest;
import com.mozilla.curriculum_tracking_system.dto.user.UserResponse;

import java.util.List;

/**
 * Interface for user management operations
 */
public interface IUserManagementService {

    /**
     * Creates a new user
     *
     * @param request the user creation request
     * @return the created user response
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Assigns a role to a user
     *
     * @param request the role assignment request
     * @return the updated user response
     */
    UserResponse assignRole(AssignRoleRequest request);

    /**
     * Removes a role from a user
     *
     * @param userId   the user ID
     * @param roleName the role name to remove
     * @return the updated user response
     */
    UserResponse removeRole(Long userId, String roleName);

    /**
     * Retrieves all users
     *
     * @return list of all users
     */
    List<UserResponse> getAllUsers();

    /**
     * Retrieves a user by ID
     *
     * @param userId the user ID
     * @return the user response
     */
    UserResponse getUserById(Long userId);

    /**
     * Retrieves users by role
     *
     * @param roleName the role name
     * @return list of users with the specified role
     */
    List<UserResponse> getUsersByRole(String roleName);

    /**
     * Retrieves the dean's email by schoolId
     * @param schoolId
     * @return a String containing the dean's email
     */
    public String getDeanEmailBySchool(Long schoolId);

    /**
     * Updates user status (enabled/disabled)
     *
     * @param userId  the user ID
     * @param enabled the new status
     * @return the updated user response
     */
    UserResponse updateUserStatus(Long userId, boolean enabled);

    /**
     * Deletes a user
     *
     * @param userId the user ID to delete
     */
    void deleteUser(Long userId);

    public boolean isCurrentUser(Long userId);

}