package com.video.api.rest;

import com.video.api.dto.VideoUploadRequestDto;
import com.video.api.dto.VideoUploadResponseDto;
import com.video.core.port.driver.IUploadVideoService;
import com.video.core.port.driver.dto.input.GenerateUploadUrlCommand;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("api")
public class VideoController {
  @Inject
  public IUploadVideoService uploadVideService;

  @Path("video")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Uni<VideoUploadResponseDto> generatePostPolicy(@Valid VideoUploadRequestDto request) {
    return Uni.createFrom()
        .item(uploadVideService
            .generate(GenerateUploadUrlCommand.builder().videoKey(request.getVideoKey()).build()))
        .onItem().transform(item -> VideoUploadResponseDto.builder().formData(item.getFormData())
            .url(item.getUrl()).build());
  }

  @Path("video/{taskId}/state")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<String> getVideoState(@PathParam("taskId") String taskId) {
    return Uni.createFrom().item(uploadVideService.getVideoState(taskId).name());
  }
}
