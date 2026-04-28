package de.mediathekview.mserver.base.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;

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
  
  public static List<String> generateDaysToCrawl(MServerBasicConfigDTO config) {
    return generateDaysToCrawl(
        config.getMaximumDaysForSendungVerpasstSection(),
        config.getMaximumDaysForSendungVerpasstSectionFuture(),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }

  public static List<String> generateDaysToCrawl(int numberOfDaysInThePast, int numberOfDaysInTheFuture, DateTimeFormatter formatter) {
    List<String> days = new ArrayList<>();
    final LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i <= numberOfDaysInThePast; i++) {
      days.add(now.minusDays(i).format(formatter));
    }
    for (int i = 1; i < numberOfDaysInTheFuture; i++) {
      days.add(now.plusDays(i).format(formatter));
    }
    return days;
  }
}
