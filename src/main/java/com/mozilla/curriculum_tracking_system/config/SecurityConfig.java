package com.mozilla.curriculum_tracking_system.config;

import com.mozilla.curriculum_tracking_system.security.JwtAuthenticationEntryPoint;
import com.mozilla.curriculum_tracking_system.security.JwtAuthenticationFilter;
import com.mozilla.curriculum_tracking_system.service.user.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors(cors -> cors.configurationSource(configurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling -> 
                    exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                // Public endpoints - only login is public
                                .requestMatchers("/api/v1/auth/login").permitAll()
                                .requestMatchers("/actuator/health").permitAll()
                                
                                // Admin-only endpoints - user management
                                .requestMatchers("/api/v1/auth/register").hasRole("ADMIN")
                                .requestMatchers("/api/v1/users/create").hasRole("ADMIN")
                                .requestMatchers("/api/v1/users/assign-role").hasRole("ADMIN")
                                .requestMatchers("/api/v1/users/admin/**").hasRole("ADMIN")
                                
                                // Role-specific endpoints
                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/v1/vice-chancellor/**").hasAnyRole("ADMIN", "VICE_CHANCELLOR")
                                .requestMatchers("/api/v1/dean/**").hasAnyRole("ADMIN", "VICE_CHANCELLOR", "DEAN")
                                .requestMatchers("/api/v1/board/**").hasAnyRole("ADMIN", "VICE_CHANCELLOR", "BOARD_MEMBER")
                                .requestMatchers("/api/v1/department/**").hasAnyRole("ADMIN", "DEAN", "DEPARTMENT_MEMBER")
                                
                                // General authenticated endpoints
                                .requestMatchers("/api/v1/auth/refresh").authenticated()
                                .requestMatchers("/api/v1/auth/logout").authenticated()
                                .requestMatchers("/api/v1/profile/**").authenticated()
                                
                                // All other requests require authentication
                                .anyRequest().authenticated()
                );
        
        httpSecurity.authenticationProvider(authenticationProvider());
        httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource configurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        corsConfiguration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",
            "http://localhost:4200",
            "https://must.ac.ke"  
        ));
        
        corsConfiguration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        corsConfiguration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"
        ));
        
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}