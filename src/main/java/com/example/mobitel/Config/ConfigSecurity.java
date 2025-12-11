


package com.example.mobitel.Config;

import com.example.mobitel.Config.Utility.AuthTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class ConfigSecurity {

    private final AuthTokenFilter authTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // CORS handled in SecurityConfig
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints (login, register)
                        .requestMatchers(
                                "/user/login"          // login endpoint
                        ).permitAll()

                        // Manager-only
                        .requestMatchers(
                                "/project-management-system/user/adduser",
                                "/project-management-system/task/create",
                                "/project-management-system/api/create/project",
                                "/project-management-system/user/view",
                                "/project-management-system/task/complete/**",
                                "/project-management-system/user/active-staff-tasks",
                                "/project-management-system/task/activeTasks/count",
                                "/project-management-system/task/completedTasks/count",
                                "/project-management-system/user/count",
                                "/project-management-system/api/pending",
                                "/project-management-system/task/assignedOngoing/count",
                                "/project-management-system/task/task/view"

                        ).hasRole("Manager")

                        // Manager + Employee
                        .requestMatchers(
                                "/project-management-system/task/swap/**",
                                "/project-management-system/task/unassigned/**",
                                "/project-management-system/task/assignedOngoing",
                                "/project-management-system/task/update/**",
                                "/project-management-system/task/assign",
                                "/project-management-system/task/onHold",
                                "/project-management-system/api/view/projects"
                        ).hasAnyRole("Manager", "Employee")

                        // Employee-only
                        .requestMatchers(
                                "/project-management-system/task/user-intern-tasks/count",
                                "/project-management-system/user/active-interns",
                                  "/project-management-system/task/intern-task-projects",
                                "/project-management-system/task/assignedOngoing/LoggedUserIntern/count"
                        ).hasRole("Employee")

                        // Intern-only
                        .requestMatchers(
                                "/project-management-system/task/logged-user-tasks",
                                "/project-management-system/task/activeTasks/interns",
                                "/project-management-system/task/completedTasksAssignedToInterns",
                                "/project-management-system/task/logged-user-tasks/active/count",
                                "/project-management-system/task/logged-user-tasks/count",
                                "/project-management-system/task/logged-user-tasks/completed/count"
                        ).hasRole("Intern")

                        .requestMatchers(
                                "/project-management-system/task/completed-projects"
                        ).hasAnyRole("Manager","Employee","Intern")

                        .anyRequest().authenticated()
                );

        // Add JWT Filter
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
