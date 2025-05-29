package com.mozilla.curriculum_tracking_system.service.user;

import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEMail) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(usernameOrEMail, usernameOrEMail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEMail));
    }

    @Transactional
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }
}
