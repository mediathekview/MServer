package mServer.crawler.sender.srf;

import java.time.LocalDateTime;

public class SrfShowOverviewUrlBuilder {

  private final String monthYear;
  private final int filmsPerPage;

  public SrfShowOverviewUrlBuilder(final int filmsPerPage) {
    LocalDateTime today = LocalDateTime.now();
    monthYear = today.getMonthValue() + "-" + today.getYear();
    this.filmsPerPage = filmsPerPage;
  }

  public String buildUrl(String id) {
    return String.format(SrfConstants.SHOW_OVERVIEW_PAGE_URL, id, filmsPerPage, monthYear);
  }
}
