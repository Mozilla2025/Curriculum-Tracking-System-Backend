package com.mozilla.curriculum_tracking_system.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialsEmailData {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String roleName;
    private String loginUrl;
}
