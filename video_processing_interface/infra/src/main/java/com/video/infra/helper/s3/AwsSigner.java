package com.video.infra.helper.s3;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class AwsSigner {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH).withZone(ZoneOffset.UTC);

  /*
   * The hex characters MUST be lower case because AWS only accepts lower case.
   */
  private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

  private AwsSigner() {}

  public static byte[] signMac(byte[] key, byte[] data) {
    try {
      final Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(key, "HmacSHA256"));
      return mac.doFinal(data);
    } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static byte[] generateSigningKey(String secretKey, Region region, String service) {
    final byte[] dateKey = signMac(("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8),
        DATE_FORMATTER.format(ZonedDateTime.now()).getBytes(StandardCharsets.UTF_8));
    final byte[] dateRegionKey = signMac(dateKey, region.id().getBytes(StandardCharsets.UTF_8));
    final byte[] dateRegionServiceKey =
        signMac(dateRegionKey, service.getBytes(StandardCharsets.UTF_8));
    return signMac(dateRegionServiceKey, "aws4_request".getBytes(StandardCharsets.UTF_8));
  }

  public static String buildCredentialField(AwsCredentials credentials, Region region) {
    return credentials.accessKeyId() + "/" + DATE_FORMATTER.format(ZonedDateTime.now()) + "/"
        + region.id() + "/" + "s3/aws4_request";
  }

  public static String hexDump(byte[] data) {
    final StringBuilder sb = new StringBuilder();
    for (byte _byte : data) {
      sb.append(HEX_CHARS[(_byte >> 4) & 0xf]);
      sb.append(HEX_CHARS[_byte & 0xf]);
    }
    return sb.toString();
  }
}
