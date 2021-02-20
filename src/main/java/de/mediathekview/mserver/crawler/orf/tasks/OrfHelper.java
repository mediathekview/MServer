package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/** Helper methods for ORF tasks. */
class OrfHelper {

  private static final String LETTER_URL_SELECTOR = "li.letter-item > a";

  private OrfHelper() {}

  static String parseTheme(final Element aItem) {
    final String theme = aItem.attr(HtmlConsts.ATTRIBUTE_TITLE);
    return parseTheme(theme);
  }

  static String parseTheme(final String theme) {
    final String result = theme.replaceAll("[0-9][0-9]:[0-9][0-9]$", "").trim();
    // Thema steht vor Doppelpunkt
    // Ausnahmen
    // - ZIB-Sendungen mit Uhrzeit
    // - DokEins-Sendungen
    // - Ungarisches Magazin
    final int index = result.indexOf(':');
    if (index > 0
        && !result.startsWith("ZIB")
        && !result.startsWith("DOKeins")
        && !result.contains("Ungarisches Magazin")) {
      return result.substring(0, index).trim();
    }
    return result;
  }

  /**
   * determines the links to the letter pages.
   *
   * @param aDocument the html document with letter links
   * @return list with urls
   */
  static List<String> parseLetterLinks(final Document aDocument) {
    final List<String> results = new ArrayList<>();

    final Elements links = aDocument.select(LETTER_URL_SELECTOR);
    links.forEach(
        element -> {
          if (element.hasAttr(HtmlConsts.ATTRIBUTE_HREF)) {
            final String subpage = element.attr(HtmlConsts.ATTRIBUTE_HREF);
            results.add(OrfConstants.URL_BASE + subpage);
          }
        });

    return results;
  }
}
