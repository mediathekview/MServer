package mServer.crawler.sender.zdf;

import java.net.URLEncoder;
import java.nio.charset.Charset;

public class ZdfUrlBuilder {
  private ZdfUrlBuilder() {}

  public static String buildLetterPageUrl(String cursor, int index) {
    cursor = addQuotationMarkIfNecessary(cursor);
    return String.format(
        ZdfConstants.URL_LETTER_PAGE,
        URLEncoder.encode(
            String.format(ZdfConstants.URL_LETTER_PAGE_VARIABLES, cursor, index), Charset.defaultCharset()),
        URLEncoder.encode(ZdfConstants.URL_LETTER_PAGE_EXTENSIONS, Charset.defaultCharset()));
  }

  private static String addQuotationMarkIfNecessary(String cursor) {
    if (!ZdfConstants.NO_CURSOR.equals(cursor)) {
      return String.format("\"%s\"", cursor);
    }
    return cursor;
  }

  public static String buildTopicSeasonUrl(int seasonNumber, int pageSize, String canonical) {
    return String.format(
            ZdfConstants.URL_TOPIC_PAGE,
            URLEncoder.encode(String.format(ZdfConstants.URL_TOPIC_PAGE_VARIABLES, seasonNumber, pageSize, canonical), Charset.defaultCharset()),
            URLEncoder.encode(ZdfConstants.URL_TOPIC_PAGE_EXTENSIONS, Charset.defaultCharset()));
  }

  public static String buildTopicSeasonUrl(int seasonNumber, int pageSize, String canonical, String cursor) {
    return String.format(
            ZdfConstants.URL_TOPIC_PAGE,
            URLEncoder.encode(String.format(ZdfConstants.URL_TOPIC_PAGE_VARIABLES_WITH_CURSOR, seasonNumber, pageSize, canonical, cursor), Charset.defaultCharset()),
            URLEncoder.encode(ZdfConstants.URL_TOPIC_PAGE_EXTENSIONS, Charset.defaultCharset()));
  }

  public static String buildTopicNoSeasonUrl(int pageSize, String id, String cursor) {
    return String.format(
            ZdfConstants.URL_TOPIC_PAGE_NO_SEASON,
            URLEncoder.encode(String.format(ZdfConstants.URL_TOPIC_PAGE_NO_SEASON_VARIABLES, id, pageSize, addQuotationMarkIfNecessary(cursor)), Charset.defaultCharset()),
            URLEncoder.encode(ZdfConstants.URL_TOPIC_PAGE_NO_SEASON_EXTENSIONS, Charset.defaultCharset()));
  }
}
