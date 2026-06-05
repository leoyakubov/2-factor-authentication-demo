package com.github.leoyakubov.twofactorauth;

import com.github.leoyakubov.twofactorauth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration,de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration",
		"security.jwt.secret=01234567890123456789012345678901"
})
class TwoFactorAuthApplicationIT {

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void shouldLoadApplicationContext() {
	}

}
