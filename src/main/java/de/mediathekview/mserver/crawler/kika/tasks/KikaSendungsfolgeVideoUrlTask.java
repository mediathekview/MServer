package de.mediathekview.mserver.crawler.kika.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class KikaSendungsfolgeVideoUrlTask
    extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final String ATTRIBUTE_ONCLICK = "onclick";
  private static final long serialVersionUID = -2633978090540666539L;
  private static final String VIDEO_DATA_ELEMENT_SELECTOR = ".av-playerContainer a[onclick]";
  private static final String VIDEO_URL_REGEX_PATTERN = "(?<=dataURL:')[^']*";

  public KikaSendungsfolgeVideoUrlTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected AbstractUrlTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new KikaSendungsfolgeVideoUrlTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    for (final Element videoDataElement : aDocument.select(VIDEO_DATA_ELEMENT_SELECTOR)) {
      if (videoDataElement.hasAttr(ATTRIBUTE_ONCLICK)) {
        final String rawVideoData = videoDataElement.attr(ATTRIBUTE_ONCLICK);
        final Matcher videoUrlMatcher =
            Pattern.compile(VIDEO_URL_REGEX_PATTERN).matcher(rawVideoData);
        if (videoUrlMatcher.find()) {
          taskResults.add(new CrawlerUrlDTO(videoUrlMatcher.group()));
        }
      }
    }
  }
}
