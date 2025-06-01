package com.mozilla.curriculum_tracking_system.repository.user;

import com.mozilla.curriculum_tracking_system.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph("User.withRoles")
    Optional<User> findByUsername(String username);

    @EntityGraph("User.withRoles")
    Optional<User> findByEmail(String email);

    @EntityGraph("User.withRoles")
    @Override
    Optional<User> findById(Long id);

    @EntityGraph("User.withRoles")
    @Override
    List<User> findAll();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph("User.withRoles")
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRolesName(@Param("roleName") String roleName);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    long countUsersWithRole(@Param("roleName") String roleName);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.isEnabled = true")
    long countEnabledUsersWithRole(@Param("roleName") String roleName);

    @EntityGraph("User.withRoles")
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isEnabled = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findUserByUsernameWithoutRoles(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findUserByEmailWithoutRoles(@Param("email") String email);
}