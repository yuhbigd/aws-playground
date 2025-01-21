package com.video.infra.storage;

import java.time.Duration;
import java.util.HashMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.video.core.domain.model.Video;
import com.video.core.domain.model.VideoPostRequestInstruction;
import com.video.core.port.driven.storage.StoragePort;
import com.video.infra.exception.GeneratePostPolicyException;
import com.video.infra.helper.s3.S3PostSignRequest;
import com.video.infra.helper.s3.S3PostSignResponse;
import com.video.infra.helper.s3.S3PostSigner;
import com.video.infra.helper.s3.S3PostSignRequest.MatchCondition;
import com.video.infra.helper.s3.S3PostSignRequest.PairCondition;
import com.video.infra.helper.s3.S3PostSignRequest.TripleCondition;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

@ApplicationScoped
@RequiredArgsConstructor
public class S3StorageImpl implements StoragePort {

  @ConfigProperty(name = "AWS_STORAGE_BUCKET_NAME", defaultValue = "churchmilky")
  private String bucketName;

  @Override
  public VideoPostRequestInstruction generateVideoPostRequestInstruction(Video video) {
    AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
    S3PostSigner s3PostSigner = new S3PostSigner(credentialsProvider);
    S3PostSignRequest req =
        S3PostSignRequest.builder().bucket(bucketName).videoKey(video.getVideoKey())
            .conditions(new HashMap<>()).expireIn(Duration.ofSeconds(3600 * 24)).build();
    req.getConditions().put(MatchCondition.EQUAL,
        PairCondition.builder().key("x-amz-meta-task-id").value(video.getTaskId()).build());
    req.getConditions().put(MatchCondition.CONTENT_RANGE, TripleCondition.builder()
        .key("content-length-range").value1("1048576").value2("104857600").build()); // 1mb to 100mb
    try {
      S3PostSignResponse s3PostSignResponse = s3PostSigner.presignPost(req);
      return VideoPostRequestInstruction.builder().url(s3PostSignResponse.getUrl().toExternalForm())
          .formData(s3PostSignResponse.getFields()).build();
    } catch (Exception e) {
      throw new GeneratePostPolicyException(e);
    }
  }
}
