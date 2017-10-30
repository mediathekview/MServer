package de.mediathekview.mserver.base.utils;

import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * A util class to collect useful JSoup HTML {@link Document} related methods.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public final class HtmlDocumentUtils {
  private HtmlDocumentUtils() {
    super();
  }

  /**
   * Searches for the given selector if found it returns the text of the first result.
   *
   * @param aElementSelector The selector for the searched element.
   * @param aDocument The document in which will be searched.
   * @return A {@link Optional} containing the found element or else an empty {@link Optional}.
   */
  public static Optional<String> getElementString(final String aElementSelector,
      final Document aDocument) {
    final Elements selected = aDocument.select(aElementSelector);
    if (!selected.isEmpty()) {
      return Optional.of(selected.first().text());
    }
    return Optional.empty();
  }
}
