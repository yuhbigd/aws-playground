package com.video.infra.exception;

public class RedisRuntimeException extends RuntimeException {
    public RedisRuntimeException(Throwable e) {
        super(e);
    }

    public RedisRuntimeException(String message) {
        super(message);
    }
}
