package com.github.leoyakubov.twofactorauth.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import com.github.leoyakubov.twofactorauth.model.User;

import java.util.Optional;


public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
