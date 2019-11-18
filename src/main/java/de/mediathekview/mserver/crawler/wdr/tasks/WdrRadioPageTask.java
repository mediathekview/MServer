package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDto;
import org.apache.commons.text.WordUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentLinkedQueue;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class WdrRadioPageTask extends AbstractDocumentTask<WdrTopicUrlDto, CrawlerUrlDTO> {

  private static final String SELECTOR_TOPIC = "h2 > a";

  public WdrRadioPageTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos, final
      JsoupConnection jsoupConnection) {
    super(aCrawler, aUrlToCrawlDtos, jsoupConnection);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {

    final Elements topicElements = aDocument.select(SELECTOR_TOPIC);
    topicElements.forEach(
        topicElement -> {
          String url = topicElement.attr(ATTRIBUTE_HREF);
          String topic = topicElement.text();

          url = UrlUtils.addDomainIfMissing(url, WdrConstants.URL_BASE);
          topic = WordUtils.capitalize(topic);

          taskResults.add(new WdrTopicUrlDto(topic, url, false));
        });
  }

  @Override
  protected AbstractRecrusivConverterTask<WdrTopicUrlDto, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new WdrRadioPageTask(crawler, aElementsToProcess, getJsoupConnection());
  }
}
