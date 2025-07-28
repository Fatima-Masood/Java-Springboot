package com.expensetracker.config;

import com.expensetracker.user.UserService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.Filter;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

@OpenAPIDefinition(info = @Info(title = "Expenditure API", version = "v1"), security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@EnableMethodSecurity
@Configuration
@Slf4j

public class SecurityConfig {

    private Jwt jwt;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserService userService,
                                                   JwtEncoder jwtEncoder,
                                                   JwtAuthFilter jwtAuthFilter,
                                                   AuthenticationManager authenticationManager)
            throws Exception {
        long expirySeconds = 3600;
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        http.formLogin(config -> config
                .successHandler((request, response, auth) -> {
                    JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                            .subject(auth.getName())
                            .expiresAt(Instant.now().plusSeconds(expirySeconds))
                            .build();
                    jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet));
                    String tokenResponse = "{\"token_type\":\"Bearer\",\"access_token\":\"" + jwt.getTokenValue()
                            + "\",\"expires_in\":" + expirySeconds + "}";

                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpStatus.OK.value());
                    response.getWriter().write(tokenResponse);
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.getWriter().write("{\"error\":\"Authentication failed\"}");
                })

        );

        http.oauth2Login(config -> config
                .successHandler((request, response, auth) -> {
                    OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) auth;
                    OAuth2User oauth2User = oauth2Token.getPrincipal();

                    String email = oauth2User.getAttribute("login");
                    log.info(email);

                    userService.registerOAuthUserIfNeeded(email);

                    JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                            .subject(email)
                            .expiresAt(Instant.now().plusSeconds(expirySeconds))
                            .build();
                    jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet));

                    auth = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(email, email));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    String tokenResponse = "{\"token_type\":\"Bearer\",\"access_token\":\"" + jwt.getTokenValue()
                            + "\",\"expires_in\":" + expirySeconds + "}";
                    response.setContentType("application/json");
                    response.getWriter().print(tokenResponse);
                })
                .failureHandler((request, response, exception) -> {
                    exception.printStackTrace();
                    response.sendRedirect("/login?error=" + exception.getMessage());
                    response.getWriter().print("Error" + exception.getMessage());
                })


        );

        http.oauth2ResourceServer(config -> config.jwt(jwtConfig -> jwtConfig.jwtAuthenticationConverter(jwt -> {
            UserDetails user = userService.loadUserByUsername(jwt.getSubject());
            return new JwtAuthenticationToken(jwt, user.getAuthorities());
        })));

        http.sessionManagement(config ->
                config.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(config -> config
                .requestMatchers(
                        "/error",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/favicon.ico",
                        "/oauth2/**",
                        "/target/**",
                        "/api/users/register",
                        "/api/users/login").permitAll()
                .anyRequest().authenticated()
        );

        http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));

        http.httpBasic(AbstractHttpConfigurer::disable);

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
