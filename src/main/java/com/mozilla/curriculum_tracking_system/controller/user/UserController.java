package com.mozilla.curriculum_tracking_system.controller.user;

import com.mozilla.curriculum_tracking_system.dto.user.AssignRoleRequest;
import com.mozilla.curriculum_tracking_system.dto.user.CreateUserRequest;
import com.mozilla.curriculum_tracking_system.dto.user.UserResponse;
import com.mozilla.curriculum_tracking_system.response.ApiResponse;
import com.mozilla.curriculum_tracking_system.service.user.IUserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/user-management")
@RequiredArgsConstructor
public class UserController {

    private final IUserManagementService userManagementService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('QA')")
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userManagementService.createUser(request);
        return ResponseEntity.ok(new ApiResponse("Successfully created user", response));
    }

    @PostMapping("/assign-role")
    @PreAuthorize("hasRole('QA') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> assignRole(@Valid @RequestBody AssignRoleRequest request) {
        UserResponse response = userManagementService.assignRole(request);
        return ResponseEntity.ok(new ApiResponse("Successfully assigned role to user", response));
    }

    @DeleteMapping("/{userId}/roles/{roleName}/delete")
    @PreAuthorize("hasRole('QA') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> removeRole(
            @PathVariable Long userId,
            @PathVariable String roleName) {
        UserResponse response = userManagementService.removeRole(userId, roleName);
        return ResponseEntity.ok(new ApiResponse("Successfully removed user role", response));
    }

    @GetMapping("/get-all-users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('QA')")
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserResponse> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse("Successfully retrieved users", users));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('QA') or @userManagementService.isCurrentUser(#userId)")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId) {
        UserResponse user = userManagementService.getUserById(userId);
        return ResponseEntity.ok(new ApiResponse("Successfully retrieved user", user));
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('QA') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse("Successfully deleted user", null));
    }
}