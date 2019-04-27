package de.mediathekview.mserver.crawler.dreisat.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

public class DreisatTopicPageTask extends DreisatOverviewpageTask {

  private static final Pattern REGEX_MAX_SUB_PAGE_NUMBER = Pattern.compile("(?<=verpasst)\\d+");
  private static final String SUB_PAGE_LINK_EXTENSION = "&mode=verpasst";
  private static final String SUB_PAGE_EXIST_REGEX = ".*" + SUB_PAGE_LINK_EXTENSION + ".*";
  private static final String SUB_PAGE_LINK_EXTENSION_TEMPLATE = SUB_PAGE_LINK_EXTENSION + "%d";

  public DreisatTopicPageTask(
      AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      boolean aCountMax, int aMaxSubPages) {
    super(aCrawler, aUrlToCrawlDTOs, aCountMax, aMaxSubPages);
  }


  @Override
  protected AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new DreisatTopicPageTask(crawler, aURLsToCrawl, countMax, maxSubPages);
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
