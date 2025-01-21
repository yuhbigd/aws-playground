package com.video.core.port.driver;

import java.util.function.Consumer;

import com.video.core.domain.model.VideoState;
import com.video.core.port.driver.dto.input.GenerateUploadUrlCommand;
import com.video.core.port.driver.dto.output.UploadUrlResult;


public interface IUploadVideoService {
  UploadUrlResult generate(GenerateUploadUrlCommand command);

  void consumeProgressEvents(Consumer<String> consumer, String taskId);

  boolean canOpenWebSocket(String taskId);

  VideoState getVideoState(String taskId);

  void onCloseWsConnection(String taskId);
}