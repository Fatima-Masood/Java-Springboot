package com.redmath.training.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.SecurityFilterChain ;

@EnableWebSecurity
public class SecurityConfig extends SecurityFilterChain  {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .defaultSuccessUrl("/index.html", true)  // Redirect here after login
                .and()
                .logout()
                .logoutSuccessUrl("/login");
    }
}

