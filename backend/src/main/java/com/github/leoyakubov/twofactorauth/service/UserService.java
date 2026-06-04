package com.github.leoyakubov.twofactorauth.service;

import com.github.leoyakubov.twofactorauth.exception.ResourceNotFoundException;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByUsername(String username) {
        log.info("retrieving user {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        log.info("retrieving user {}", identifier);
        return userRepository.findByUsername(identifier).or(() -> userRepository.findByEmail(identifier));
    }

    public User getByUsername(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("username %s", username)));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
