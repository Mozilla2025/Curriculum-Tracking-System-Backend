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
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                        .cors(cors -> cors.configurationSource(configurationSource()))
                        .csrf(AbstractHttpConfigurer::disable)
                        .exceptionHandling(ex -> ex
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                        .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers(
                                        "/actuator/health",
                                        "/api/v1/users/**"
                                ).permitAll()

                                .requestMatchers(
                                        "/api/v1/admin/**"
                                ).hasAnyRole("ADMIN", "QA")

                                .requestMatchers(
                                        "/api/v1/tracking/curriculums/initiate",
                                        "/api/v1/tracking/curriculums/*/assign/**",
                                        "/api/v1/tracking/curriculums/*/notes",
                                        "/api/v1/tracking/curriculums/stats"
                                ).hasRole("QA")

                                .requestMatchers(
                                        "/api/v1/tracking/curriculums/stage/SCHOOL_BOARD/**"
                                ).hasAnyRole("QA", "SCHOOL_BOARD")

                                .requestMatchers(
                                        "/api/v1/tracking/curriculums/stage/DEAN_COMMITTEE/**"
                                ).hasAnyRole("QA", "DEAN")

                                .requestMatchers(
                                        "/api/v1/tracking/curriculums/stage/SENATE/**"
                                ).hasAnyRole("QA", "SENATE")

                                .requestMatchers(
                                        "/api/v1/tracking/curriculums/stage/QA_INTERNAL_REVIEW/**",
                                        "/api/v1/tracking/curriculums/stage/VICE_CHANCELLOR_REVIEW/**",
                                        "/api/v1/tracking/curriculums/stage/CUE_EXTERNAL_REVIEW/**"
                                ).hasRole("QA")

                                .requestMatchers("/api/v1/tracking/**").authenticated()


                                .anyRequest().authenticated())
                        .authenticationProvider(authenticationProvider())
                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource configurationSource() {
                CorsConfiguration corsConfiguration = new CorsConfiguration();

                corsConfiguration.setAllowedOriginPatterns(List.of(
                        "https://curiculum-tracking-system-frontend.vercel.app",
                        "https://1rrq4qld-5173.uks1.devtunnels.ms",
                        "http://localhost:5173",
                        "http://localhost:3000"
                ));

                corsConfiguration.setAllowedMethods(Arrays.asList(
                        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

                corsConfiguration.setAllowedHeaders(Arrays.asList(
                        "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));

                corsConfiguration.setAllowCredentials(true);
                corsConfiguration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", corsConfiguration);
                return source;
        }
}