package com.todolist.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    // private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // ✅ Injected

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            log.warn("Invalid user: {}", username.replace('\n', ' '));
            throw new UsernameNotFoundException("User or password incorrect.");
        }

        User u = user.get();
        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPassword(),
                AuthorityUtils.createAuthorityList("ROLE_USER")
        );
    }


}