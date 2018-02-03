package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import de.mediathekview.mserver.crawler.orf.OrfTopicUrlDTO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OrfArchiveLetterPageTask implements Callable<ConcurrentLinkedQueue<OrfTopicUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(OrfArchiveLetterPageTask.class);
  
  private static final String ITEM_SELECTOR = "article.item > a";
          
  @Override
  public ConcurrentLinkedQueue<OrfTopicUrlDTO> call() throws Exception {
    final ConcurrentLinkedQueue<OrfTopicUrlDTO> results = new ConcurrentLinkedQueue<>();
    
    // URLs für Seiten parsen
    final Document document = Jsoup.connect(OrfConstants.URL_ARCHIVE).get();
    List<String> overviewLinks = OrfHelper.parseLetterLinks(document);
    
    // Sendungen für die einzelnen Seiten pro Buchstabe ermitteln
    overviewLinks.forEach(url -> {
      try {
        Document subpageDocument = Jsoup.connect(url).get();
        results.addAll(parseOverviewPage(subpageDocument));
      } catch (IOException ex) {
        LOG.fatal("OrfArchiveLetterPageTask: error parsing url " + url, ex);
      }
    });  
    
    return results;
  }  
  
  private ConcurrentLinkedQueue<OrfTopicUrlDTO> parseOverviewPage(Document aDocument) {
    final ConcurrentLinkedQueue<OrfTopicUrlDTO> results = new ConcurrentLinkedQueue<>();
    
    Elements elements = aDocument.select(ITEM_SELECTOR);
    elements.forEach(item -> {
      String theme = OrfHelper.parseTheme(item);
      String url = item.attr(Consts.ATTRIBUTE_HREF);
      
      OrfTopicUrlDTO dto = new OrfTopicUrlDTO(theme, url);
      results.add(dto);
    });
      
    return results;
  }
}