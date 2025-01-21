package com.video.infra.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.video.core.domain.model.VideoState;
import com.video.core.port.driven.persistence.PersistencePort;
import com.video.infra.exception.RedisRuntimeException;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

@ApplicationScoped
public class RedisPersistenceLayerImpl implements PersistencePort {

    private final Map<String, StatefulRedisPubSubConnection<String, String>> connections = new ConcurrentHashMap<>();

    @Inject
    private RedisClient redisClient;

    @Override
    public String storeVideoState(String taskId, VideoState state) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisReactiveCommands<String, String> reactiveCommands = connection.reactive();
            Mono<String> action = Mono.just(taskId).flatMap(val -> {
                String key = "task:" + val;
                return reactiveCommands.set(key, state.getValue()).flatMap(res -> {
                    return reactiveCommands.expire(key, 60 * 60 * 24).thenReturn(res);
                });
            }).onErrorMap(e -> new RedisRuntimeException(e));
            return action.block();
        }
    }

    @Override
    public void consumeProgressEvents(Consumer<String> consumer, String taskId) {
        StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
        RedisPubSubReactiveCommands<String, String> reactiveCommands = connection.reactive();
        connections.put(taskId, connection);
        reactiveCommands.subscribe("progress:" + taskId).subscribe();
        reactiveCommands.observeChannels()
                .doOnNext(patternMessage -> {
                    consumer.accept(patternMessage.getMessage());
                })
                .doFinally(signal -> {
                    if (connection.isOpen()) {
                        connection.close();
                    }
                })
                .subscribe(
                        x -> {
                        },
                        e -> System.err.print("onError: " + e));
    }

    @Override
    public VideoState getVideoState(String taskId) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisReactiveCommands<String, String> reactiveCommands = connection.reactive();
            Mono<String> action = Mono.just(taskId).flatMap(val -> {
                String key = "task:" + val;
                return reactiveCommands.get(key);
            }).onErrorMap(e -> new RedisRuntimeException(e));
            Optional<String> valueOpt = action.blockOptional();
            if (valueOpt.isEmpty()) {
                throw new RedisRuntimeException("No value found for key: " + taskId);
            }
            return VideoState.fromValue(valueOpt.get());
        }
    }

    @Override
    public void closeConnectionAssociateWithTaskId(String taskId) {
        StatefulRedisPubSubConnection<String, String> connection = connections.get(taskId);
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
        connections.remove(taskId);
    }

}
