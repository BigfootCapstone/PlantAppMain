package com.codeup.plantapp.config;

import com.codeup.plantapp.services.UserServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;





@Configuration
@EnableWebSecurity
public class SecConfig {

    private UserServices userServices;

    public SecConfig(UserServices userServices) {
        this.userServices = userServices;
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) -> requests
                .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/",
                        "/users/create",
                        "/users/login",
                        "/login"
                ).permitAll()
                .requestMatchers(
//                        "/login",
//                        "/users/login",
                        "/users/{id}",
                        "/users/{id}",
                        "/users/{id}/edit",
                        "/users/profile",
                        "/users/{id}/delete",
                        "/friends/",
                        "/friends/{id}",
                        "/friends/accept/{id}",
                        "/friends/ignore/{id}",
                        "/plants/add",
                        "/plants/search",
                        "/plants/{id}",
                        "/plants/{id}/delete",
                        "/plants/plantEdit/{id}",
                        "/plants/garden/{id}",
                        "/plants/comments/{id}",
                        "/plants/comments/delete/{plant}.{id}",
                        "/posts/all",
                        "/posts/create",
                        "/posts/{id}",
                        "/posts/comments",
                        "/posts/{id}/edit",
                        "/posts/users/login"
                ).authenticated()


        );

        http.formLogin((form) -> form.loginPage("/login").defaultSuccessUrl("/users/profile"));
        http.logout((form) -> form.logoutSuccessUrl("/"));
        http.httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }
}