package com.video.infra.config;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;

@Dependent
public class S3Config {

  @Produces
  public S3Client s3Client() {
    return S3Client.builder().region(Region.AP_SOUTHEAST_1) // specify your desired region
        .build();
  }
}
