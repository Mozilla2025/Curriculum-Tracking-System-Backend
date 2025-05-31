package com.mozilla.curriculum_tracking_system.config;

import com.mozilla.curriculum_tracking_system.constants.RoleConstants;
import com.mozilla.curriculum_tracking_system.model.roles.Role;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.roles.RoleRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${app.admin.username:admin}")
    private String defaultAdminUsername;
    
    @Value("${app.admin.email:admin@curriculum.system}")
    private String defaultAdminEmail;
    
    @Value("${app.admin.password:Admin@123}")
    private String defaultAdminPassword;
    
    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeDefaultAdmin();
    }
    
    private void initializeRoles() {
        log.info("Initializing system roles...");
        
        createRoleIfNotExists(RoleConstants.ADMIN, "System Administrator with full access");
        createRoleIfNotExists(RoleConstants.VICE_CHANCELLOR, "Vice Chancellor of the institution");
        createRoleIfNotExists(RoleConstants.DEAN, "Dean of a faculty or school");
        createRoleIfNotExists(RoleConstants.BOARD_MEMBER, "Member of the institutional board");
        createRoleIfNotExists(RoleConstants.DEPARTMENT_MEMBER, "Member of an academic department");
        createRoleIfNotExists(RoleConstants.FACULTY, "Faculty member or academic staff");
        createRoleIfNotExists(RoleConstants.STAFF, "General staff member");
        
        log.info("Role initialization completed");
    }
    
    private void createRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        } else {
            log.debug("Role already exists: {}", roleName);
        }
    }
    
    private void initializeDefaultAdmin() {
        log.info("Checking for default admin user...");
        
        long adminCount = userRepository.countUsersWithRole(RoleConstants.ADMIN);
        
        if (adminCount == 0) {
            log.info("No admin user found. Creating default admin user...");
            
            Role adminRole = roleRepository.findByName(RoleConstants.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
            User adminUser = User.builder()
                    .username(defaultAdminUsername)
                    .email(defaultAdminEmail)
                    .password(passwordEncoder.encode(defaultAdminPassword))
                    .firstName("System")
                    .lastName("Administrator")
                    .phoneNumber("")
                    .isEnabled(true)
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(adminUser);
            
            log.info("Default admin user created successfully");
            log.info("Username: {}", defaultAdminUsername);
            log.info("Email: {}", defaultAdminEmail);
            log.warn("Please change the default admin password after first login!");
        } else {
            log.info("Admin user(s) already exist. Skipping default admin creation.");
        }
    }
}
