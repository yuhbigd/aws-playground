package com.video.core.port.driver.dto.output;

import java.util.Collection;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadUrlResult {
  private String url;
  private Map<String, Collection<String>> formData;
}
