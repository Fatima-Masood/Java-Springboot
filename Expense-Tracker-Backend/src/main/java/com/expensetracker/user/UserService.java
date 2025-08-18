package com.expensetracker.user;

import com.expensetracker.dto.PasswordUpdateRequest;
import com.expensetracker.expenditure.ExpenditureRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ExpenditureRepository expenditureRepository;

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

    public User OAuthSignUp(String username, AuthenticationManager authenticationManager) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(username));
        user.setRole("USER");
        if (!userRepository.existsByUsername(username))
            user = userRepository.save(user);
        return user;
    }

    public String register(String username, String password,
                           AuthenticationManager authenticationManager,
                           JwtEncoder jwtEncoder) {

        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("USER");
            userRepository.save(user);
        }
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(auth);
        Jwt jwt = createJwt(username, jwtEncoder);
        return "{\"access_token\": \"" + jwt.getTokenValue() + "\"}";
    }

    public void setAuthentication(Authentication auth){
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public Jwt createJwt(String username, JwtEncoder jwtEncoder) {
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
        cookie.setDomain("/");
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
