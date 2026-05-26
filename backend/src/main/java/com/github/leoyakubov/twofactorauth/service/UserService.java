package com.github.leoyakubov.twofactorauth.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.github.leoyakubov.twofactorauth.exception.BadRequestException;
import com.github.leoyakubov.twofactorauth.exception.EmailAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.exception.InternalServerException;
import com.github.leoyakubov.twofactorauth.exception.ResourceNotFoundException;
import com.github.leoyakubov.twofactorauth.exception.UsernameAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.model.AuthUserDetails;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import com.github.leoyakubov.twofactorauth.repository.UserRepository;
import com.github.leoyakubov.twofactorauth.payload.LoginResult;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;
    private final TotpManager totpManager;

    public UserService(@Lazy PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       @Lazy AuthenticationManager authenticationManager,
                       JwtTokenManager jwtTokenManager,
                       TotpManager totpManager) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenManager = jwtTokenManager;
        this.totpManager = totpManager;
    }

    public LoginResult loginUser(String username, String password) {
       Authentication authentication = authenticationManager
               .authenticate(new UsernamePasswordAuthenticationToken(username, password));

       AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();
       if(userDetails.isMfa()) {
           log.info("login accepted for {} and MFA verification is required", userDetails.getUsername());
           return LoginResult.requiresMfa();
       }

        Authentication canonicalAuthentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        log.info("login accepted for {} and issuing access token", userDetails.getUsername());
        return LoginResult.authenticated(jwtTokenManager.generateToken(canonicalAuthentication));
    }

    public String verify(String username, String code) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException( String.format("username %s", username)));

        if(!totpManager.verifyCode(code, user.getSecret())) {
            log.warn("MFA verification failed for {}", username);
            throw new BadRequestException("Code is incorrect");
        }

        log.info("MFA verification succeeded for {}", username);
        return Optional.of(user)
                .map(AuthUserDetails::new)
                .map(userDetails -> new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()))
                .map(jwtTokenManager::generateToken)
                .orElseThrow(() ->
                        new InternalServerException("unable to generate access token"));
    }

    public User registerUser(User user, Role role) {
        log.info("registering user {}", user.getUsername());

        if(userRepository.existsByUsername(user.getUsername())) {
            log.warn("username {} already exists.", user.getUsername());

            throw new UsernameAlreadyExistsException(
                    String.format("username %s already exists", user.getUsername()));
        }

        if(userRepository.existsByEmail(user.getEmail())) {
            log.warn("email {} already exists.", user.getEmail());

            throw new EmailAlreadyExistsException(
                    String.format("email %s already exists", user.getEmail()));
        }
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(Set.of(role)));

        if(user.isMfa()) {
            user.setSecret(totpManager.generateSecret());
        }

        log.info("saved user {} (mfa={})", user.getUsername(), user.isMfa());
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        log.info("retrieving user {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByUsernameOrEmail(String identifier) {
        log.info("retrieving user {}", identifier);
        return userRepository.findByUsername(identifier).or(() -> userRepository.findByEmail(identifier));
    }
}
