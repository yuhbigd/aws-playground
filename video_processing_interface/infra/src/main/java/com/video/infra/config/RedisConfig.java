package com.video.infra.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class RedisConfig {

    @ConfigProperty(name = "REDIS_HOST", defaultValue = "127.0.0.1")
    private String redisHost;

    @ConfigProperty(name = "REDIS_PORT", defaultValue = "6379")
    private int redisPort;

    @ConfigProperty(name = "REDIS_USER", defaultValue = "user")
    private String redisUser;

    @ConfigProperty(name = "REDIS_PASSWORD", defaultValue = "password")
    private String redisPassword;

    @ConfigProperty(name = "REDIS_TLS", defaultValue = "false")
    private Boolean isTlsEnabled;

    private RedisClient redisClient;

    @Produces
    public RedisClient redisClient() {
        RedisURI uri = RedisURI.Builder
                .redis(redisHost, redisPort)
                .withAuthentication(redisUser, redisPassword)
                .withSsl(isTlsEnabled)
                .withVerifyPeer(false)
                .build();

        redisClient = RedisClient.create(uri);
        return redisClient;
    }

    @PreDestroy
    public void close() {
        redisClient.shutdown();
    }
}
