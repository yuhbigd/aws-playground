package com.video.core.port.driven.persistence;

import java.util.function.Consumer;

import com.video.core.domain.model.VideoState;

public interface PersistencePort {
    String storeVideoState(String taskId, VideoState state);

    void consumeProgressEvents(Consumer<String> consumer, String taskId);

    VideoState getVideoState(String taskId);

    void closeConnectionAssociateWithTaskId(String taskId);
}
