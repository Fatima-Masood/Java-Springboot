package com.expensetracker.user;

import com.expensetracker.dto.PasswordUpdateRequest;
import com.expensetracker.expenditure.ExpenditureRepository;
import com.expensetracker.limits.MonthlyLimitRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ExpenditureRepository expenditureRepository;
    @Autowired
    private MonthlyLimitRepository monthlyLimitRepository;
    @Autowired
    private JwtEncoder jwtEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.emptyList())
                .build();
    }

    public User authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
            throw new BadCredentialsException("Invalid credentials");
        return user;
    }

    public String OAuthSignUp(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("login");
        log.info("User email: " + email);
        if (email == null) {
            throw new RuntimeException("Email not found in OAuth2 user attributes");
        }

        userRepository.findByUsername(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(email);
                    newUser.setPassword(passwordEncoder.encode(email));
                    newUser.setRole("USER");
                    return userRepository.save(newUser);
                });

        Jwt jwt = createJwt(email);
        return jwt.getTokenValue();
    }

    public String register(String username, String password,
                           AuthenticationManager authenticationManager) {

        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("USER");
            userRepository.save(user);
        }
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        setAuthentication(auth);
        Jwt jwt = createJwt(username);
        return "{\"access_token\": \"" + jwt.getTokenValue() + "\"}";
    }

    public void setAuthentication(Authentication auth){
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public Jwt createJwt(String username) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(username)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @Transactional
    public int deleteUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty())
            return -1;

        User user = optionalUser.get();
        expenditureRepository.deleteByUser(username);
        monthlyLimitRepository.deleteByUsername(username);
        userRepository.delete(user);
        return 0;
    }

    public int updatePassword(PasswordUpdateRequest passwordUpdateRequest){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) return -1;

        User user = optionalUser.get();
        if (!passwordEncoder.matches(passwordUpdateRequest.getOldPassword(), user.getPassword())) return 1;

        user.setPassword(passwordEncoder.encode(passwordUpdateRequest.getNewPassword()));
        userRepository.save(user);
        return 0;
    }
}
