package de.mediathekview.mserver.base.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A util class to collect useful JSoup HTML {@link Document} related methods. */
public final class HtmlDocumentUtils {
  private static final Logger LOG = LogManager.getLogger(HtmlDocumentUtils.class);
  private static final String DAUER_REGEX_PATTERN = "\\d+:\\d+";

  private HtmlDocumentUtils() {
    super();
  }

  /**
   * Searches for the given selector if found it returns the given attribute of the first result.
   *
   * @param aElementSelector The selector for the searched element.
   * @param aAttributeKey The attribute to return.
   * @param aDocument The document in which will be searched.
   * @return A {@link Optional} containing the found element or else an empty {@link Optional}.
   */
  public static Optional<String> getElementAttributeString(
      final String aElementSelector, final String aAttributeKey, final Document aDocument) {
    final Elements selected = aDocument.select(aElementSelector);
    if (!selected.isEmpty() && selected.first().hasAttr(aAttributeKey)) {
      return Optional.of(selected.first().attr(aAttributeKey));
    }
    return Optional.empty();
  }

  /**
   * Searches for the given selector if found it returns the attribute of the first result.
   *
   * @param aElementSelector1 The selector for the searched element.
   * @param aElementSelector2 The selector for the searched element if aElementSelector1 is not
   *     found.
   * @param aAttributeKey The attribute to return.
   * @param aDocument The document in which will be searched.
   * @return A {@link Optional} containing the found element or else an empty {@link Optional}.
   */
  public static Optional<String> getElementAttributeString(
      final String aElementSelector1,
      final String aElementSelector2,
      final String aAttributeKey,
      final Document aDocument) {

    Optional<String> result =
        getElementAttributeString(aElementSelector1, aAttributeKey, aDocument);
    if (result.isEmpty()) {
      result = getElementAttributeString(aElementSelector2, aAttributeKey, aDocument);
    }

    return result;
  }

  /**
   * Searches for the given selector if found it returns the text of the first result.
   *
   * @param aElementSelector The selector for the searched element.
   * @param aDocument The document in which will be searched.
   * @return A {@link Optional} containing the found element or else an empty {@link Optional}.
   */
  public static Optional<String> getElementString(
      final String aElementSelector, final Document aDocument) {
    final Elements selected = aDocument.select(aElementSelector);
    if (!selected.isEmpty()) {
      return Optional.of(selected.first().text());
    }
    return Optional.empty();
  }

  /**
   * Searches for the given selector if found it returns the text of the first result.
   *
   * @param aElementSelector1 The selector for the searched element.
   * @param aElementSelector2 The selector for the searched element if aElementSelector1 is not
   *     found
   * @param aDocument The document in which will be searched.
   * @return A {@link Optional} containing the found element or else an empty {@link Optional}.
   */
  public static Optional<String> getElementString(
      final String aElementSelector1, final String aElementSelector2, final Document aDocument) {

    Optional<String> result = getElementString(aElementSelector1, aDocument);
    if (result.isEmpty()) {
      result = getElementString(aElementSelector2, aDocument);
    }

    return result;
  }

  /**
   * Searches for the given selector if found it returns the text of the first result.
   *
   * @param aElementSelector1 The selector for the searched element.
   * @param aElementSelector2 The selector for the searched element if aElementSelector1 is not
   *     found
   * @param aElementSelector3 The selector for the searched element if aElementSelector1 and
   *     aElementSelector2 is not found
   * @param aDocument The document in which will be searched.
   * @return A {@link Optional} containing the found element or else an empty {@link Optional}.
   */
  public static Optional<String> getElementString(
      final String aElementSelector1,
      final String aElementSelector2,
      final String aElementSelector3,
      final Document aDocument) {

    Optional<String> result = getElementString(aElementSelector1, aDocument);
    if (result.isEmpty()) {
      result = getElementString(aElementSelector2, aDocument);
    }
    if (result.isEmpty()) {
      result = getElementString(aElementSelector3, aDocument);
    }

    return result;
  }

  public static Optional<Duration> parseDuration(final String dauerText) {
    if (dauerText == null) {
      return Optional.empty();
    }

    final Matcher dauerMatcher = Pattern.compile(DAUER_REGEX_PATTERN).matcher(dauerText);
    if (dauerMatcher.find()) {
      final String[] dauerSplits = dauerMatcher.group().split(":");
      if (2 == dauerSplits.length) {
        try {
          return Optional.of(
              Duration.ofMinutes(Long.parseLong(dauerSplits[0]))
                  .plusSeconds(Long.parseLong(dauerSplits[1])));
        } catch (final NumberFormatException numberFormatException) {
          LOG.error("A duration for can't be parsed.", numberFormatException);
        }
      }
    }
    return Optional.empty();
  }
}
