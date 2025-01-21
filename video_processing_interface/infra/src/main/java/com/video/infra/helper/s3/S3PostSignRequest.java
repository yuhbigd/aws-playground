package com.video.infra.helper.s3;

import java.time.Duration;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class S3PostSignRequest {
  public static enum MatchCondition {
    EQUAL("eq"), STARTS_WITH("starts_with"), CONTENT_RANGE("content_range");

    private String condition;

    MatchCondition(String condition) {
      this.condition = condition;
    }

    public String condition() {
      return condition;
    }
  }
  public interface ConditionValue {
  }

  @Value
  @Builder
  public static class PairCondition implements ConditionValue {
    private String key;
    private String value;
  }

  @Value
  @Builder
  public static class TripleCondition implements ConditionValue {
    private String key;
    private String value1;
    private String value2;
  }

  private String bucket;
  private String videoKey;
  private Map<MatchCondition, ConditionValue> conditions;
  private Duration expireIn;
}
