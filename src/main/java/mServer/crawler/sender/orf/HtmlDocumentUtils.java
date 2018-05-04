package mServer.crawler.sender.orf;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * A util class to collect useful JSoup HTML {@link Document} related methods.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public final class HtmlDocumentUtils {
  private static final Logger LOG = LogManager.getLogger(HtmlDocumentUtils.class);
  private static final String DAUER_REGEX_PATTERN = "\\d+:\\d+";

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
  public static Optional<String> getElementAttributeString(final String aElementSelector,
      final String aAttributeKey, final Document aDocument) {
    final Elements selected = aDocument.select(aElementSelector);
    if (!selected.isEmpty() && selected.first().hasAttr(aAttributeKey)) {
      return Optional.of(selected.first().attr(aAttributeKey));
    }
    return Optional.empty();
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

  public static Optional<Duration> parseDuration(final Optional<String> aDauerText) {
    return aDauerText.isPresent() ? parseDuration(aDauerText.get()) : Optional.empty();
  }

  public static Optional<Duration> parseDuration(final String aDauerText) {
    final Matcher dauerMatcher = Pattern.compile(DAUER_REGEX_PATTERN).matcher(aDauerText);
    if (dauerMatcher.find()) {
      final String[] dauerSplits = dauerMatcher.group().split(":");
      if (2 == dauerSplits.length) {
        try {
          return Optional.of(Duration.ofMinutes(Long.parseLong(dauerSplits[0]))
              .withSeconds(Long.parseLong(dauerSplits[1])));
        } catch (final NumberFormatException numberFormatException) {
          LOG.error("A duration for can't be parsed.", numberFormatException);
        }
      }
    }
    return Optional.empty();
  }
}
