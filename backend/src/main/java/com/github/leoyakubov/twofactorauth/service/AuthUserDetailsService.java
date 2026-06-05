package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("loading user details for {}", username);
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .map(AuthUserDetails::new)
                .orElseThrow(() -> {
                    log.debug("no user found for {}", username);
                    return new UsernameNotFoundException("Username not found");
                });
    }
}
