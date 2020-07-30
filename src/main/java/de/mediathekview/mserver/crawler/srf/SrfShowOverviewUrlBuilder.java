package de.mediathekview.mserver.crawler.srf;

import java.time.LocalDateTime;

public class SrfShowOverviewUrlBuilder {

  private static final int FILMS_PER_PAGE = 100;

  private final String monthYear;

  public SrfShowOverviewUrlBuilder() {
    LocalDateTime today = LocalDateTime.now();
    monthYear = today.getMonthValue() + "-" + today.getYear();
  }

  public String buildUrl(String baseUrl, String id) {
    return String.format(SrfConstants.SHOW_OVERVIEW_PAGE_URL, baseUrl, id, FILMS_PER_PAGE, monthYear);
  }
}
