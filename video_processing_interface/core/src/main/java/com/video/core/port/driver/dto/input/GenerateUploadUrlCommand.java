package com.video.core.port.driver.dto.input;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GenerateUploadUrlCommand {
  private String videoKey;
}