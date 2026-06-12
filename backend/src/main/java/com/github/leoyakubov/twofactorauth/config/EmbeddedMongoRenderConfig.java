package com.github.leoyakubov.twofactorauth.config;

import de.flapdoodle.embed.mongo.commands.ImmutableMongodArguments;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.process.io.ProcessOutput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("render")
public class EmbeddedMongoRenderConfig {

    @Bean
    public ProcessOutput processOutput() {
        return ProcessOutput.silent();
    }

    @Bean
    public MongodArguments mongodArguments() {
        return ImmutableMongodArguments.defaults().withIsQuiet(true);
    }
}
