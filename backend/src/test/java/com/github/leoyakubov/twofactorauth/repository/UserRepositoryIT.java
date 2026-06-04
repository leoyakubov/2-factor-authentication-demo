package com.github.leoyakubov.twofactorauth.repository;

import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.data.domain.Sort.Direction.ASC;

class UserRepositoryIT {

    private MongoServer mongoServer;
    private SimpleMongoClientDatabaseFactory databaseFactory;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind();

        databaseFactory = new SimpleMongoClientDatabaseFactory(mongoServer.getConnectionString() + "/testdb");
        MongoTemplate mongoTemplate = new MongoTemplate(databaseFactory);
        mongoTemplate.indexOps(User.class).createIndex(new Index().on("username", ASC).unique());
        mongoTemplate.indexOps(User.class).createIndex(new Index().on("email", ASC).unique());
        userRepository = new MongoRepositoryFactory(mongoTemplate).getRepository(UserRepository.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (databaseFactory != null) {
            databaseFactory.destroy();
        }
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

    @Test
    void shouldRejectDuplicateUsername() {
        userRepository.save(buildUser("demo", "first@example.com"));

        assertThrows(DuplicateKeyException.class,
                () -> userRepository.save(buildUser("demo", "second@example.com")));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        userRepository.save(buildUser("first", "demo@example.com"));

        assertThrows(DuplicateKeyException.class,
                () -> userRepository.save(buildUser("second", "demo@example.com")));
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
