package com.expensetracker.config;

import com.expensetracker.user.UserService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.List;

import static io.opentelemetry.semconv.SemanticAttributes.HttpRequestMethodValues.POST;

@OpenAPIDefinition(info = @Info(title = "Expenditure API", version = "v1"), security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@EnableMethodSecurity
@Configuration
@Slf4j
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)

public class SecurityConfig {

    private Jwt jwt;
    private long expirySeconds = 3600;
    private JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserService userService,
                                                   JwtEncoder jwtEncoder,
                                                   JwtAuthFilter jwtAuthFilter,
                                                   AuthenticationManager authenticationManager) throws Exception {

        http
                .formLogin(config -> config
                .successHandler((request, response, auth) -> {
                    jwt = generatejwt(auth, jwtEncoder);
                    response = generateResponse(response);
                    response.getWriter().write(getTokenResponse());
                }))

                .oauth2Login(config -> config
                .successHandler((request, response, auth) -> {
                    OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) auth;

                    String username = oauth2Token.getPrincipal().getAttribute("login");
                    userService.registerOAuthUserIfNeeded(username, authenticationManager);

                    jwt = generatejwt(auth, jwtEncoder);
                    response = generateResponse(response);
                    response.getWriter().print(getTokenResponse());
                }))

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )

                .sessionManagement(config ->
                config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(config -> config
                .requestMatchers(
                        "/error",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/favicon.ico",
                        "/oauth2/**",
                        "/target/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users/login", "api/users/register").permitAll()
                .anyRequest().authenticated())

                .cors(withDefaults -> {})
                .csrf(csrf -> csrf.disable());

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:8000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private Jwt generatejwt(Authentication auth, JwtEncoder jwtEncoder){
        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .subject(auth.getName())
                .expiresAt(Instant.now().plusSeconds(expirySeconds))
                .build();
        jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet));

        return jwt;
    }
    public HttpServletResponse generateResponse(HttpServletResponse response){
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.OK.value());

        response.setContentType("application/json");

        Cookie cookie = new Cookie("access_token", jwt.getTokenValue());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) expirySeconds);
        response.addCookie(cookie);

        return response;
    }

    public String getTokenResponse() {
        return "{\"token_type\":\"Bearer\",\"access_token\":\"" + jwt.getTokenValue()
                + "\",\"expires_in\":" + expirySeconds + "}";
    }

}
