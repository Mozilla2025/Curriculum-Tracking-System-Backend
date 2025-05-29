package com.mozilla.curriculum_tracking_system.repository.user;

import com.mozilla.curriculum_tracking_system.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{

      Optional<User> findByUsername(String username);

      Optional<User> findByUsernameOrEmail(String username, String email);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRolesName(@Param("roleName") String roleName);
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countUsersWithRole(@Param("roleName") String roleName);
    
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isEnabled = true")
    long countEnabledUsersWithRole(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isEnabled = true")
    List<User> findEnabledUsersByRole(@Param("roleName") String roleName);
}
