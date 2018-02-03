package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import org.jsoup.nodes.Element;

/**
 * Helper methods for ORF tasks
 */
public class OrfHelper {
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
}
