package de.mediathekview.mserver.crawler.tagesschau;

/**
 * Constants for the Tagesschau crawler.
 * Handles the "vor 20 Jahren" (20 years ago) archive with daily news broadcasts.
 */
public final class TagesschauConstants {

  // Starting point: Tagesschau vor 20 Jahren (20 years ago)
  public static final String ARCHIVE_START_URL = "https://www.tagesschau.de/inland/tsvorzwanzigjahren-ts-142.html";

  // Pattern for accessing specific month archives
  // Example: /multimedia/sendung/ts/vor20jahren/ts-vor20jahren-20060401.html
  public static final String ARCHIVE_DAY_URL_PATTERN = "https://www.tagesschau.de/multimedia/sendung/ts/vor20jahren/ts-vor20jahren-%s.html";

  // Base URL for archive pages
  public static final String ARCHIVE_MONTH_BASE = "https://www.tagesschau.de/multimedia/sendung/ts/vor20jahren/";

  public static final String VIDEO_JSON = "https://zagent7.h-cdn.com/cmd/get_links_info?customer=ard_de&zone=gen&ver=1.165.211&url=https%3A%2F%2Fwww.tagesschau.de%2Fmultimedia%2Fsendung%2Ftagesschau_vor_20_jahren%2Fvideo-%s.html";

  // Private constructor to hide the implicit public one
  private TagesschauConstants() {
    // Utility class, do not instantiate
  }
}


