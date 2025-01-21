package com.video.core.domain.model;

import com.google.common.collect.Multimap;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VideoPostRequestInstruction {
  private String url;
  private Multimap<String, String> formData;
}
