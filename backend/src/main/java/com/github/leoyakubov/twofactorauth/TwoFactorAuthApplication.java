package com.github.leoyakubov.twofactorauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TwoFactorAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(TwoFactorAuthApplication.class, args);
	}

}
