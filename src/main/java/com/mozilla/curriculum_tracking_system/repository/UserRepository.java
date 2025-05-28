package com.mozilla.curriculum_tracking_system.repository;

import com.mozilla.curriculum_tracking_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByUsername(String username);
}
