package com.video.core.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Video {
  private String videoKey;
  private String taskId;
}
