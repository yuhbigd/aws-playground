package com.video.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class VideoUploadRequestDto {
  @NotBlank
  @Pattern(regexp = "^[\\w\\-. ]+\\.(mp4|avi|mov|wmv|flv|mkv)$",
      message = "Video key must be a valid video filename with allowed extensions (mp4, avi, mov, wmv, flv, mkv)")
  private String videoKey;
}
