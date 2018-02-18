package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WdrDayPageTask extends AbstractDocumentTask<TopicUrlDTO, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(WdrDayPageTask.class);  
  
  private static final String ENTRY_SELECTOR = "div.section > div.con > div.mod > div.boxCon > div.box";
  private static final String URL_SELECTOR = "div.hideTeasertext > a";
  private static final String TOPIC_SELECTOR = "h3.ressort > a";
  
  public WdrDayPageTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    Elements entryElements = aDocument.select(ENTRY_SELECTOR);
    entryElements.forEach(entry -> {
      Elements topicElements = entry.select(TOPIC_SELECTOR);
      Elements urlElements = entry.select(URL_SELECTOR);
      
      if (topicElements.size() == urlElements.size() && topicElements.size() == 1) {
        String topic = getTopic(topicElements.get(0), urlElements.get(0));
        String url = UrlUtils.addDomainIfMissing(urlElements.get(0).attr(Consts.ATTRIBUTE_HREF), WdrConstants.URL_BASE);
        
        if (isRelevantEntry(url)) {
          TopicUrlDTO dto = new TopicUrlDTO(topic, url);
          taskResults.add(dto);
        }
      }
    });
  }

  @Override
  protected AbstractUrlTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new WdrDayPageTask(crawler, aURLsToCrawl);
  }
  
  private static boolean isRelevantEntry(String aUrl) {
    // Hilfe-URLs ignorieren
    return !aUrl.contains("/hilfe/");
  }
  
  private static String getTopic(Element aTopicElement, Element aUrlElement) {
    String topic = aTopicElement.text();
        
    // Sonderbehandlung für Thema: bei bestimmten Wörtern das Thema aus Videotitel ermitteln
    if(topic.compareToIgnoreCase("Film") == 0) {
        // Aus Film -> Fernsehfilm machen, damit das Thema zu Sendung A-Z passt
        topic = "Fernsehfilm";
    } else if (topic.compareToIgnoreCase("Video") == 0) {
        String[] titleParts = aUrlElement.attr(Consts.ATTRIBUTE_TITLE).split("-");
        if(titleParts.length >= 1) {
            topic = titleParts[0].replace(", WDR", "").trim();
        }
    } 

    return topic;
  }
}
