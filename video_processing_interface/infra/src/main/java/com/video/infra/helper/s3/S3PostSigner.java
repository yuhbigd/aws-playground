package com.video.infra.helper.s3;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.video.infra.helper.s3.S3PostSignRequest.ConditionValue;
import com.video.infra.helper.s3.S3PostSignRequest.MatchCondition;
import com.video.infra.helper.s3.S3PostSignRequest.PairCondition;
import com.video.infra.helper.s3.S3PostSignRequest.TripleCondition;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;


public final class S3PostSigner {

  private static final DateTimeFormatter AMZ_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
  private static final DateTimeFormatter AMZ_EXPIRATION_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private ObjectMapper objectMapper = new ObjectMapper();

  @Getter
  @Setter
  private static final class Policy {

    public String expiration;

    public List<String[]> conditions;
  }

  private final AwsCredentialsProvider mCredentialsProvider;

  public S3PostSigner(AwsCredentialsProvider credentialsProvider) {
    mCredentialsProvider = credentialsProvider;
  }

  public S3PostSignResponse presignPost(S3PostSignRequest req) throws JsonProcessingException {
    AwsCredentials credentials = mCredentialsProvider.resolveCredentials();
    ZonedDateTime date = ZonedDateTime.now(ZoneOffset.UTC);
    String credentialsField = AwsSigner.buildCredentialField(credentials, Region.AP_SOUTHEAST_1);
    final Policy policy = new Policy();
    policy.expiration = AMZ_EXPIRATION_DATE_FORMATTER.format(date.plus(req.getExpireIn()));
    List<String[]> result = new ArrayList<>();
    result.add(new String[] {"eq", "$x-amz-algorithm", "AWS4-HMAC-SHA256"});
    result.add(new String[] {"eq", "$x-amz-date", AMZ_DATE_FORMATTER.format(date)});
    result.add(new String[] {"eq", "$x-amz-credential", credentialsField});
    result.add(new String[] {"eq", "$key", req.getVideoKey()});
    result.add(new String[] {"eq", "$bucket", req.getBucket()});
    result.addAll(buildConditionList(req.getConditions()));
    policy.conditions = result;
    final String policyJson = objectMapper.writeValueAsString(policy);
    final String policyB64 =
        Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
    Multimap<String, String> fields = ArrayListMultimap.create();
    fields.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
    fields.put("x-amz-credential", credentialsField);
    fields.put("x-amz-date", AMZ_DATE_FORMATTER.format(date));
    fields.put("x-amz-signature", AwsSigner.hexDump(AwsSigner.signMac(
        AwsSigner.generateSigningKey(credentials.secretAccessKey(), Region.AP_SOUTHEAST_1, "s3"),
        policyB64.getBytes(StandardCharsets.UTF_8))));
    fields.put("policy", policyB64);
    fields.put("key", req.getVideoKey());
    addToMap(fields, req.getConditions());
    String url = String.format("https://%s.s3.amazonaws.com/", req.getBucket());
    try {
      return new S3PostSignResponse(URL.of(URI.create(url), null), fields);
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

  private List<String[]> buildConditionList(Map<MatchCondition, ConditionValue> conditions) {
    List<String[]> conditionList = new ArrayList<>();
    for (var condition : conditions.entrySet()) {
      var conditionType = condition.getKey();
      var conditionValue = condition.getValue();
      if (conditionValue instanceof PairCondition pairCondition) {
        conditionList.add(new String[] {conditionType.condition(), "$" + pairCondition.getKey(),
            pairCondition.getValue()});
      } else if (conditionValue instanceof TripleCondition tripleCondition) {
        if (conditionType != MatchCondition.CONTENT_RANGE) {
          throw new RuntimeException("Triple condition is not content range");
        }
        conditionList.add(new String[] {tripleCondition.getKey(), tripleCondition.getValue1(),
            tripleCondition.getValue2()});
      }
    }
    return conditionList;
  }

  private void addToMap(Multimap<String, String> fields,
      Map<MatchCondition, ConditionValue> conditions) {
    for (var condition : conditions.entrySet()) {
      var conditionType = condition.getKey();
      var conditionValue = condition.getValue();
      if (conditionValue instanceof PairCondition pairCondition) {
        fields.put(pairCondition.getKey(), pairCondition.getValue());
      } else if (conditionValue instanceof TripleCondition tripleCondition) {
        if (conditionType != MatchCondition.CONTENT_RANGE) {
          throw new RuntimeException("Triple condition is not content range");
        }
        fields.put(tripleCondition.getKey(), tripleCondition.getValue1());
        fields.put(tripleCondition.getKey(), tripleCondition.getValue2());
      }
    }
  }
}
