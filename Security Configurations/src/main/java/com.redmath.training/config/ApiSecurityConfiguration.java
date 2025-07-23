package com.redmath.training.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.redmath.training.user.UserService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;


@OpenAPIDefinition(info = @Info(title = "News API", version = "v1"), security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@EnableMethodSecurity
@Configuration

public class ApiSecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserService userService) throws Exception {
        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
        );

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.oauth2ResourceServer(config -> config.jwt(jwt -> jwt
                .jwtAuthenticationConverter(token -> {
                    UserDetails user = userService.loadUserByUsername(token.getSubject());
                    return new JwtAuthenticationToken(token, user.getAuthorities());
                })
        ));

        http.authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/login").permitAll()
                .requestMatchers("/error", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/news/**").permitAll()
                .anyRequest().authenticated()
        );

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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

