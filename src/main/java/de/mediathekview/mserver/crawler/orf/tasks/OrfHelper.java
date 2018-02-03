package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Helper methods for ORF tasks
 */
public class OrfHelper {
  
  private static final String LETTER_URL_SELECTOR = "div.mod_name_list > ul.js_extra_content > li:not(.inactive) > a.base_list_item_inner";
  
  public static String parseTheme(final Element aItem) {
    String theme = aItem.attr(Consts.ATTRIBUTE_TITLE);
    
    // Thema steht vor Doppelpunkt
    // Ausnahmen 
    // - ZIB-Sendungen mit Uhrzeit
    // - DokEins-Sendungen
    // - Ungarisches Magazin
    int index = theme.indexOf(":");
    if (index > 0 
      && !theme.startsWith("ZIB")
      && !theme.startsWith("DOKeins")
      && !theme.contains("Ungarisches Magazin")) {
      return theme.substring(0, index);
    }
    
    return theme;
  }  
  
  /**
   * determines the links to the letter pages
   * @param aDocument the html document with letter links
   * @return list with urls
   */
  public static List<String> parseLetterLinks(Document aDocument) {
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
}
