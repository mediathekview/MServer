package de.mediathekview.mserver.crawler.dreisat.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

public class DreisatDayPageTask extends DreisatOverviewpageTask {

  private static final Pattern REGEX_MAX_SUB_PAGE_NUMBER = Pattern.compile("(?<=verpasst)\\d+");
  private static final String SUB_PAGE_LINK_EXTENSION = "&mode=verpasst";
  private static final String SUB_PAGE_EXIST_REGEX = ".*" + SUB_PAGE_LINK_EXTENSION + ".*";
  private static final String SUB_PAGE_LINK_EXTENSION_TEMPLATE = SUB_PAGE_LINK_EXTENSION + "%d";

  public DreisatDayPageTask(
      AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      boolean aCountMax) {
    // use MAX_VALUE as maximum sub page value because crawler should parse all sub pages of a day
    super(aCrawler, aUrlToCrawlDTOs, aCountMax, Integer.MAX_VALUE);
  }


  @Override
  protected AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new DreisatDayPageTask(crawler, aURLsToCrawl, countMax);
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
