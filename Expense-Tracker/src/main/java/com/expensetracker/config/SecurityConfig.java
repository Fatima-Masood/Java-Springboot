package com.expensetracker.config;

import com.expensetracker.user.UserService;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.jsonwebtoken.io.IOException;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

@OpenAPIDefinition(info = @Info(title = "Expenditure API", version = "v1"), security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@EnableMethodSecurity
@Configuration
@Slf4j

public class SecurityConfig {

    record UserCredentials(String username, String password) {}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserService userService, JwtEncoder jwtEncoder) throws Exception {
        long expirySeconds = 3600;
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        // Disable default form login and OAuth2 auto-configuration
        http.formLogin(form -> form.disable());
        http.oauth2Login(oauth2 -> oauth2.disable());

        // Stateless session
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // Authorization rules
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/error", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/register", "/api/users/form-login", "/api/users/auth-login").permitAll()
                .anyRequest().authenticated()
        );

        // CSRF and CORS
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> cors.disable());

        return http.build();
    }


    @Bean
    public JwtEncoder jwtEncoder(@Value("${jwt.signing.key}") byte[] signingKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(signingKey, "HmacSHA256")));
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.signing.key}") byte[] signingKey) {
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(signingKey, "HmacSHA256")).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

}
