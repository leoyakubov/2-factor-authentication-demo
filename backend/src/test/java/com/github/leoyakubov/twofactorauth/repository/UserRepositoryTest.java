package com.github.leoyakubov.twofactorauth.repository;

import com.github.leoyakubov.twofactorauth.model.Profile;
import com.github.leoyakubov.twofactorauth.model.Role;
import com.github.leoyakubov.twofactorauth.model.User;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest(excludeAutoConfiguration = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration.class
})
@Import(UserRepositoryTest.MongoTestConfig.class)
@TestPropertySource(properties = "spring.data.mongodb.database=testdb")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByUsernameAndEmail() {
        User user = buildUser("demo", "demo@example.com");

        userRepository.save(user);

        assertTrue(userRepository.findByUsername("demo").isPresent());
        assertTrue(userRepository.findByEmail("demo@example.com").isPresent());
        assertTrue(userRepository.existsByUsername("demo"));
        assertTrue(userRepository.existsByEmail("demo@example.com"));
    }

    @TestConfiguration
    static class MongoTestConfig {

        @Bean(destroyMethod = "shutdown")
        MongoServer mongoServer() {
            MongoServer server = new MongoServer(new MemoryBackend());
            server.bind();
            return server;
        }

        @Bean
        MongoDatabaseFactory mongoDatabaseFactory(MongoServer mongoServer) {
            return new SimpleMongoClientDatabaseFactory(mongoServer.getConnectionString() + "/testdb");
        }

        @Bean
        MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
            return new MongoTemplate(mongoDatabaseFactory);
        }
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
