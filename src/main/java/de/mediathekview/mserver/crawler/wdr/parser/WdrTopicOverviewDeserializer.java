package de.mediathekview.mserver.crawler.wdr.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDTO;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WdrTopicOverviewDeserializer extends WdrLetterPageDeserializerBase {
  private static final Logger LOG = LogManager.getLogger(WdrTopicOverviewDeserializer.class);  
  
  private static final String SELECTOR_URL = "div.teaser > a";    
  private static final String SELECTOR_URL_ROCKPALAST_YEARS = "div.entries > div";
  private static final String SELECTOR_URL_ROCKPALAST_YEARS_ENTRIES = "div.entry > a";
  
  public List<WdrTopicUrlDTO> deserialize(final String aTopic, final Document aDocument) {
    List<WdrTopicUrlDTO> results = new ArrayList<>();
    
    addUrls(results, aTopic, aDocument, SELECTOR_URL, true);
    if (isRockpalastOverviewPage(aDocument)) {
      addUrls(results, aTopic, aDocument, SELECTOR_URL_ROCKPALAST_YEARS_ENTRIES, false);
    }
    
    return results;
  }
  
  private void addUrls(final List<WdrTopicUrlDTO> aResults, 
    final String aTopic,
    final Document aDocument, 
    final String aSelector,
    boolean defaultIsFileUrl)
  {
    Elements urlElements = aDocument.select(aSelector);
    urlElements.forEach(urlElement -> {
      String url = urlElement.attr(ATTRIBUTE_HREF);
      if(url != null && !url.isEmpty() && isUrlRelevant(url)) {
        url = UrlUtils.addDomainIfMissing(url, WdrConstants.URL_BASE);
        boolean isFileUrl = isFileUrl(urlElement, defaultIsFileUrl);
        
        aResults.add(new WdrTopicUrlDTO(aTopic, url, isFileUrl));
      }
    });
  }

  /***
   * Filtert URLs heraus, die nicht durchsucht werden sollen
   * Hintergrund: diese URLs verweisen auf andere und führen bei der Suche
   * im Rahmen der Rekursion zu endlosen Suchen
   * @param url zu prüfende URL
   * @return true, wenn die URL verarbeitet werden soll, sonst false
   */
  private boolean isUrlRelevant(String aUrl) {
      // die Indexseite der Lokalzeit herausfiltern, da alle Beiträge
      // um die Lokalzeitenseiten der entsprechenden Regionen gefunden werden
      if(aUrl.endsWith("lokalzeit/index.html")) {
          return false;
      } else if(aUrl.contains("wdr.de/hilfe")) {
          return false;
      }

      return true;
  }  
  
  private boolean isRockpalastOverviewPage(final Document aDocument) {
    // ermitteln, ob es sich um die erste Rockpalastübersichtsseite handelt
    // dazu muss das erste Element in der Jahresauswahl aktiv sein
    Elements yearElements = aDocument.select(SELECTOR_URL_ROCKPALAST_YEARS);

    if(yearElements != null) {
        Element firstYearElement = yearElements.first();

        if(firstYearElement != null) {
            return firstYearElement.classNames().contains("active");
        }
    }
    return false;
  }
}
