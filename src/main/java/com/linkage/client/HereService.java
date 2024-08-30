package com.linkage.client;

import com.linkage.LinkageConfiguration;
import com.linkage.api.ApiResponse;
import com.linkage.core.validations.HereSearchSchema;

public class HereService extends BaseServiceClient {
   public HereService(LinkageConfiguration configuration) {
      super(configuration);
   }

   // Does the existential search via the id
   public ApiResponse<Object> hereSearch(HereSearchSchema input) {

      String latitude = input.getLatitude();
      String longitude = input.getLongitude();
      String urlOne = "https://discover.search.hereapi.com/v1/browse?";
      String at = "at=" + latitude + "," + longitude;
      String apiKey = configuration.getHereApiKey();
      String urlTwo = "&apiKey=" + apiKey;
      String in = "&in=circle:" + latitude + "," + longitude + ";" + "r=100";
      String categories = "&categories=600-6400-0000,600-6400-0069,600-6400-0070,700-7200-0272,800-8000-0000,800-8000-0154,800-8000-0155,800-8000-0156,800-8000-0157,800-8000-0158,800-8000-0159,800-8000-0161,800-8000-0162,800-8000-0325,800-8000-0340,800-8000-0341,800-8000-0367,800-8000-0400,800-8000-0401";
      String limit = "&limit=10";
      String name = "&name=" + input.getKeyword();

      String appendedUrl = urlOne + at + urlTwo + in + categories + limit + name;

      return networkCallExternalService(appendedUrl, "get", null, null);
   }
}
