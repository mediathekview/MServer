package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OrfLetterPageTask implements Callable<ConcurrentLinkedQueue<TopicUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(OrfLetterPageTask.class);
  
  private static final String SHOW_URL_SELECTOR = "ul.latest_episodes > li.latest_episode > a";
          
  @Override
  public ConcurrentLinkedQueue<TopicUrlDTO> call() throws Exception {
    final ConcurrentLinkedQueue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();
    
    // URLs für Seiten parsen
    final Document document = Jsoup.connect(OrfConstants.URL_SHOW_LETTER_PAGE_A).get();
    List<String> overviewLinks = OrfHelper.parseLetterLinks(document);
    
    // Sendungen für die einzelnen Seiten pro Buchstabe ermitteln
    overviewLinks.forEach(url -> {
      try {
        Document subpageDocument = Jsoup.connect(url).get();
        results.addAll(parseOverviewPage(subpageDocument));
      } catch (IOException ex) {
        LOG.fatal("OrfLetterPageTask: error parsing url " + url, ex);
      }
    });  
    
    return results;
  }  
  
  private ConcurrentLinkedQueue<TopicUrlDTO> parseOverviewPage(Document aDocument) {
    final ConcurrentLinkedQueue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();
    
    Elements links = aDocument.select(SHOW_URL_SELECTOR);
    links.forEach(element -> {
      if (element.hasAttr(Consts.ATTRIBUTE_HREF)) {
        String link = element.attr(Consts.ATTRIBUTE_HREF);
        String theme = OrfHelper.parseTheme(element);
        
        results.add(new TopicUrlDTO(theme, link));
      }
    });
      
    return results;
  }
}