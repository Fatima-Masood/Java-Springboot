package com.expensetracker.config;

import com.expensetracker.user.User;
import com.expensetracker.user.UserService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;


@EnableMethodSecurity
@Configuration
@Slf4j
public class SecurityConfig {
    private AuthenticationManager authenticationManager;

    @Bean
    @Profile("!test")
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserService userService,
                                                   JwtEncoder jwtEncoder) throws Exception {

        http
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            String username = authentication.getName();
                            Jwt jwt = userService.createJwt(username, jwtEncoder);
                            userService.setAuthentication(authentication);
                            response.setStatus(200);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"access_token\": \"" + jwt.getTokenValue() + "\"}");
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Invalid username or password\"}");
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/github")
                .successHandler((request, response, authentication) -> {
                    OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
                    String username = token.getPrincipal().getAttribute("login");
                    User user = userService.OAuthSignUp(username, authenticationManager);
                    userService.setAuthentication(authentication);
                    Jwt jwt = userService.createJwt(user.getUsername(), jwtEncoder);
                    response.setStatus(200);
                    response.setContentType("application/json");

                    response.getWriter().write("{\"access_token\": \"" + jwt.getTokenValue() + "\"}");
                }))

            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(Customizer.withDefaults()))

            .sessionManagement(config -> config
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(config -> config
            .requestMatchers(
                    "/error",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/favicon.ico",
                    "/oauth2/**",
                    "/target/**",
                    "/api/users/login",
                    "/api/users/register").permitAll()
            .anyRequest().authenticated())
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));

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
