package de.mediathekview.mserver.crawler.zdf;

import java.net.URLEncoder;
import java.nio.charset.Charset;

public class ZdfUrlBuilder {
  private ZdfUrlBuilder() {}

  public static String buildLetterPageUrl(int index) {
    return String.format(
        ZdfConstants.URL_LETTER_PAGE,
        URLEncoder.encode(
            String.format(ZdfConstants.URL_LETTER_PAGE_VARIABLES, index), Charset.defaultCharset()),
        URLEncoder.encode(ZdfConstants.URL_LETTER_PAGE_EXTENSIONS, Charset.defaultCharset()));
  }

  public static String buildTopicSeasonUrl(int seasonNumber, int pageSize, String canonical) {
    return String.format(
            ZdfConstants.URL_TOPIC_PAGE,
            URLEncoder.encode(String.format(ZdfConstants.URL_TOPIC_PAGE_VARIABLES, seasonNumber, pageSize, canonical), Charset.defaultCharset()),
            URLEncoder.encode(ZdfConstants.URL_TOPIC_PAGE_EXTENSIONS, Charset.defaultCharset()));

  }
}
