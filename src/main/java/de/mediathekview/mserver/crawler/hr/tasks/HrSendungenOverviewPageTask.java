package de.mediathekview.mserver.crawler.hr.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.hr.HrConstants;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HrSendungenOverviewPageTask implements Callable<Set<CrawlerUrlDTO>> {

  private static final String SENDUNGSFOLEN_OVERVIEW_URL_REPLACEMENT = "sendungen/index.html";
  private static final String INDEX_PAGE_NAME = "index.html";
  private static final Logger LOG = LogManager.getLogger(HrSendungenOverviewPageTask.class);
  private static final String HR_SENDUNGEN_URL = HrConstants.BASE_URL + "sendungen-a-z/index.html";
  private static final String SENDUNG_URL_SELECTOR = ".c-teaser__headlineLink.link";

  @Override
  public Set<CrawlerUrlDTO> call() {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    try {
      final Document document = Jsoup.connect(HR_SENDUNGEN_URL).get();
      for (final Element filmUrlElement : document.select(SENDUNG_URL_SELECTOR)) {
        if (filmUrlElement.hasAttr(Consts.ATTRIBUTE_HREF)) {
          results.add(new CrawlerUrlDTO(filmUrlElement.absUrl(Consts.ATTRIBUTE_HREF)
              .replace(INDEX_PAGE_NAME, SENDUNGSFOLEN_OVERVIEW_URL_REPLACEMENT)));
        }
      }
    } catch (final IOException ioException) {
      LOG.fatal(
          "Something wen't terrible wrong on gathering the HR Sendung URLs from Sendungen overview page.",
          ioException);
    }

    return results;
  }

}
