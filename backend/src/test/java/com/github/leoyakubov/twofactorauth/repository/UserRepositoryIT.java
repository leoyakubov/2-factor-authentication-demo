package com.github.leoyakubov.twofactorauth.repository;

import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryIT {

    private MongoServer mongoServer;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind();

        MongoTemplate mongoTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(mongoServer.getConnectionString() + "/testdb"));
        userRepository = new MongoRepositoryFactory(mongoTemplate).getRepository(UserRepository.class);
    }

    @AfterEach
    void tearDown() {
        if (mongoServer != null) {
            mongoServer.shutdown();
        }
    }

    @Test
    void shouldSaveAndFindUserByUsernameAndEmail() {
        User user = buildUser("demo", "demo@example.com");

        userRepository.save(user);

        assertTrue(userRepository.findByUsername("demo").isPresent());
        assertTrue(userRepository.findByEmail("demo@example.com").isPresent());
        assertTrue(userRepository.existsByUsername("demo"));
        assertTrue(userRepository.existsByEmail("demo@example.com"));
    }

    private static User buildUser(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .password("encoded")
                .active(true)
                .userProfile(Profile.builder()
                        .displayName("Demo User")
                        .profilePictureUrl("https://example.com/" + username + ".png")
                        .build())
                .roles(Set.of(Role.USER))
                .mfa(false)
                .build();
    }
}
