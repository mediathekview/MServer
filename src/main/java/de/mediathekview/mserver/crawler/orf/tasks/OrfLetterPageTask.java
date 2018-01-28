package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import de.mediathekview.mserver.crawler.orf.OrfTopicUrlDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OrfLetterPageTask implements Callable<ConcurrentLinkedQueue<OrfTopicUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(OrfLetterPageTask.class);
  
  private static final String LETTER_URL_SELECTOR = "div.mod_name_list > ul.js_extra_content > li.base_list_item > a.base_list_item_inner";
  private static final String SHOW_URL_SELECTOR = "ul.latest_episodes > li.latest_episode > a";
          
  @Override
  public ConcurrentLinkedQueue<OrfTopicUrlDTO> call() throws Exception {
    final ConcurrentLinkedQueue<OrfTopicUrlDTO> results = new ConcurrentLinkedQueue<>();
    
    // URLs für Seiten parsen
    final Document document = Jsoup.connect(OrfConstants.URL_SHOW_LETTER_PAGE).get();
    List<String> overviewLinks = parseOverviewLinks(document);
    
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
  
 /**
   * Ermittelt aus der HTML-Seite die Links für die weiteren Übersichtsseiten
   * @param aDocument das Dokument der ersten Übersichtsseite
   * @return Liste der URLs der weiteren Übersichtsseiten
   */
  private List<String> parseOverviewLinks(Document aDocument) {
    final List<String> results = new ArrayList<>();
    
    Elements links = aDocument.select(LETTER_URL_SELECTOR);
    links.forEach(element -> {
      if (element.hasAttr(Consts.ATTRIBUTE_HREF)) {
        String subpage = element.attr(Consts.ATTRIBUTE_HREF);
        results.add(OrfConstants.URL_BASE + subpage);
      }
    });
      
    return results;
  }
  
  private ConcurrentLinkedQueue<OrfTopicUrlDTO> parseOverviewPage(Document aDocument) {
    final ConcurrentLinkedQueue<OrfTopicUrlDTO> results = new ConcurrentLinkedQueue<>();
    
    Elements links = aDocument.select(SHOW_URL_SELECTOR);
    links.forEach(element -> {
      if (element.hasAttr(Consts.ATTRIBUTE_HREF)) {
        String link = element.attr(Consts.ATTRIBUTE_HREF);
        String theme = element.attr(Consts.ATTRIBUTE_TITLE);
        
        results.add(new OrfTopicUrlDTO(theme, link));
      }
    });
      
    return results;
  }  
}