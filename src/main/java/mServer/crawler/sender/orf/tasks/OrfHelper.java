package mServer.crawler.sender.orf.tasks;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import mServer.crawler.sender.orf.OrfConstants;

/**
 * Helper methods for ORF tasks
 */
public class OrfHelper {

  private static final String LETTER_URL_SELECTOR = "li.letter-item > a";
  private static final String ATTRIBUTE_HREF = "href";
  private static final String ATTRIBUTE_TITLE = "title";

  private OrfHelper() {
  }

  public static String parseTheme(final Element aItem) {
    String theme = aItem.attr(ATTRIBUTE_TITLE);
    return parseTheme(theme);
  }

  public static String parseTheme(final String theme) {
    final String result = theme.replaceAll("[0-9]{1,2}:[0-9][0-9]$", "").trim();
    // Thema steht vor Doppelpunkt
    // Ausnahmen
    // - ZIB-Sendungen mit Uhrzeit
    // - DokEins-Sendungen
    // - Ungarisches Magazin
    int index = result.indexOf(':');
    if (index > 0
            && !result.startsWith("ZIB")
            && !result.startsWith("DOKeins")
            && !result.contains("Ungarisches Magazin")) {
      return result.substring(0, index).trim();
    }
    return result;
  }

  /**
   * determines the links to the letter pages
   *
   * @param aDocument the html document with letter links
   * @return list with urls
   */
  public static List<String> parseLetterLinks(Document aDocument) {
    final List<String> results = new ArrayList<>();

    Elements links = aDocument.select(LETTER_URL_SELECTOR);
    links.forEach(element -> {
      if (element.hasAttr(ATTRIBUTE_HREF)) {
        String subpage = element.attr(ATTRIBUTE_HREF);
        results.add(OrfConstants.URL_BASE + subpage);
      }
    });

    return results;
  }
}
