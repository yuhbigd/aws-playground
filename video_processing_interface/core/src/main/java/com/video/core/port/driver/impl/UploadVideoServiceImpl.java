package com.video.core.port.driver.impl;

import java.util.UUID;
import java.util.function.Consumer;

import com.video.core.domain.model.Video;
import com.video.core.domain.model.VideoPostRequestInstruction;
import com.video.core.domain.model.VideoState;
import com.video.core.port.driven.persistence.PersistencePort;
import com.video.core.port.driven.storage.StoragePort;
import com.video.core.port.driver.IUploadVideoService;
import com.video.core.port.driver.dto.input.GenerateUploadUrlCommand;
import com.video.core.port.driver.dto.output.UploadUrlResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UploadVideoServiceImpl implements IUploadVideoService {

  @Inject
  public StoragePort storagePort;

  @Inject
  PersistencePort persistencePort;

  @Override
  public UploadUrlResult generate(GenerateUploadUrlCommand command) {
    String prefix = "video/input/";
    String videoKey = prefix + command.getVideoKey();
    String taskid = UUID.randomUUID().toString();
    Video video = Video.builder().taskId(taskid).videoKey(videoKey).build();
    VideoPostRequestInstruction postReq = storagePort.generateVideoPostRequestInstruction(video);
    persistencePort.storeVideoState(taskid, VideoState.PENDING);
    return UploadUrlResult.builder().url(postReq.getUrl()).formData(postReq.getFormData().asMap())
        .build();
  }

  @Override
  public void consumeProgressEvents(Consumer<String> consumer, String taskId) {
    persistencePort.consumeProgressEvents(consumer, taskId);
  }

  @Override
  public boolean canOpenWebSocket(String taskId) {
    return persistencePort.getVideoState(taskId) == VideoState.PROCESSING;
  }

  @Override
  public VideoState getVideoState(String taskId) {
    return persistencePort.getVideoState(taskId);
  }

  @Override
  public void onCloseWsConnection(String taskId) {
    persistencePort.closeConnectionAssociateWithTaskId(taskId);
  }

}
