package com.video.infra.helper.s3;

import java.net.URL;
import com.google.common.collect.Multimap;

public final class S3PostSignResponse {

  private final URL mUrl;
  private final Multimap<String, String> mFields;

  S3PostSignResponse(URL url, Multimap<String, String> fields) {
    mUrl = url;
    mFields = fields;
  }

  public URL getUrl() {
    return mUrl;
  }

  public Multimap<String, String> getFields() {
    return mFields;
  }
}
