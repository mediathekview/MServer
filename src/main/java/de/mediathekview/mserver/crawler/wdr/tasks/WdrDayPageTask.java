package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.*;
import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentLinkedQueue;

public class WdrDayPageTask extends AbstractDocumentTask<TopicUrlDTO, CrawlerUrlDTO> {

  private static final String ENTRY_SELECTOR =
      "div.section > div.con > div.mod > div.boxCon > div.box";
  private static final String URL_SELECTOR = "div.hideTeasertext > a";
  private static final String TOPIC_SELECTOR = "h3.ressort > a";

  public WdrDayPageTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  private static boolean isRelevantEntry(final String aUrl) {
    // Hilfe-URLs ignorieren
    return !aUrl.contains("/hilfe/");
  }

  private static String getTopic(final Element aTopicElement, final Element aUrlElement) {
    String topic = aTopicElement.text();

    // Sonderbehandlung für Thema: bei bestimmten Wörtern das Thema aus Videotitel ermitteln
    if (topic.compareToIgnoreCase("Film") == 0) {
      // Aus Film -> Fernsehfilm machen, damit das Thema zu Sendung A-Z passt
      topic = "Fernsehfilm";
    } else if (topic.compareToIgnoreCase("Video") == 0) {
      final String[] titleParts = aUrlElement.attr(HtmlConsts.ATTRIBUTE_TITLE).split("-");
      if (titleParts.length >= 1) {
        topic = titleParts[0].replace(", WDR", "").trim();
      }
    }

    return topic;
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {
    final Elements entryElements = aDocument.select(ENTRY_SELECTOR);
    entryElements.forEach(
        entry -> {
          final Elements topicElements = entry.select(TOPIC_SELECTOR);
          final Elements urlElements = entry.select(URL_SELECTOR);

          if (topicElements.size() == urlElements.size() && topicElements.size() == 1) {
            final String topic = getTopic(topicElements.get(0), urlElements.get(0));
            final String url =
                UrlUtils.addDomainIfMissing(
                    urlElements.get(0).attr(HtmlConsts.ATTRIBUTE_HREF), WdrConstants.URL_BASE);

            if (isRelevantEntry(url)) {
              final TopicUrlDTO dto = new TopicUrlDTO(topic, url);
              taskResults.add(dto);
            }
          }
        });
  }

  @Override
  protected AbstractUrlTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlsToCrawl) {
    return new WdrDayPageTask(crawler, aUrlsToCrawl);
  }
}
