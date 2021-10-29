package de.mediathekview.mserver.base.utils;

/** A set of util methods to work with dates. */
public class DateUtils {
  private static final String SPLITTED_NUMBERS_REGEX_PATTERN = "$1:$2";
  private static final String SPLIT_NUMBERS_REGEX_PATTERN = "(\\+\\d{1,2})(\\d{1,2})";

  private DateUtils() {
    super();
  }

  // Java 8 misses a ISO 8601 support. See:
  // https://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date
  public static String changeDateTimeForMissingISO8601Support(final String aDateTimeString) {
    return aDateTimeString.replaceAll(SPLIT_NUMBERS_REGEX_PATTERN, SPLITTED_NUMBERS_REGEX_PATTERN);
  }
}
