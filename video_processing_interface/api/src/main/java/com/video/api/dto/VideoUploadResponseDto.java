package com.video.api.dto;

import java.util.Collection;
import java.util.Map;
import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class VideoUploadResponseDto {
  private String url;
  private Map<String, Collection<String>> formData;
}
