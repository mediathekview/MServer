package de.mediathekview.mserver.crawler.dreisat.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

public class DreisatTopicsOverviewPageTask extends DreisatOverviewpageTask {

  private static final Pattern REGEX_MAX_SUB_PAGE_NUMBER = Pattern.compile("(?<=sendungenaz)\\d+");
  private static final String SUB_PAGE_EXIST_REGEX = ".*" + "sendungenaz" + "[0-9]+";
  private static final String SUB_PAGE_LINK_EXTENSION_TEMPLATE = "%d";

  public DreisatTopicsOverviewPageTask(
      AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      boolean aCountMax) {
    // use MAX_VALUE as maximum sub page value because crawler should parse all sub pages of topics overview
    super(aCrawler, aUrlToCrawlDTOs, aCountMax, Integer.MAX_VALUE);
  }


  @Override
  protected AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new DreisatTopicsOverviewPageTask(crawler, aURLsToCrawl, countMax);
  }

  @Override
  protected Pattern getReqExMaxSubPageNumber() {
    return REGEX_MAX_SUB_PAGE_NUMBER;
  }

  @Override
  protected String getSubPageLinkExtension() {
    return SUB_PAGE_LINK_EXTENSION_TEMPLATE;
  }

  @Override
  protected String getSubpageExistRegex() {
    return SUB_PAGE_EXIST_REGEX;
  }
}
