package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDto;
import de.mediathekview.mserver.crawler.wdr.parser.WdrLetterPageDeserializer;
import de.mediathekview.mserver.crawler.wdr.parser.WdrLetterPageUrlDeserializer;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WdrLetterPageTask implements Callable<Queue<WdrTopicUrlDto>>  {

  private static final Logger LOG = LogManager.getLogger(WdrLetterPageTask.class);
  
  private final WdrLetterPageDeserializer deserializer = new WdrLetterPageDeserializer();
  
  @Override
  public Queue<WdrTopicUrlDto> call() {
    final ConcurrentLinkedQueue<WdrTopicUrlDto> results = new ConcurrentLinkedQueue<>();
    
    WdrLetterPageUrlDeserializer urlDeserializer = new WdrLetterPageUrlDeserializer();
        
    // URLs für Seiten parsen
    final Document document;
    try {
      document = Jsoup.connect(WdrConstants.URL_LETTER_PAGE).get();
    } catch (IOException ex) {
      LOG.fatal("WdrLetterPageTask: error loading url " + WdrConstants.URL_LETTER_PAGE, ex);
      return results;
    }

    List<String> overviewLinks = urlDeserializer.deserialize(document);

    // Sendungen für Startseite ermitteln
    parseSubPage(results, document);
    
    // Sendungen für die einzelnen Seiten pro Buchstabe ermitteln
    overviewLinks.forEach(url -> {
      try {
        Document subpageDocument = Jsoup.connect(url).get();
        parseSubPage(results, subpageDocument);
      } catch (IOException ex) {
        LOG.fatal("WdrLetterPageTask: error parsing url " + url, ex);
      }
    });
    
    return results;
  }  
  
  private void parseSubPage(final ConcurrentLinkedQueue<WdrTopicUrlDto> aResults, final Document aDocument) {
    List<WdrTopicUrlDto> topics = deserializer.deserialize(aDocument);
    aResults.addAll(topics);
  }
}
