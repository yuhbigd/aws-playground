package com.video.core.port.driven.storage;

import com.video.core.domain.model.Video;
import com.video.core.domain.model.VideoPostRequestInstruction;

public interface StoragePort {
  VideoPostRequestInstruction generateVideoPostRequestInstruction(Video video);
}
