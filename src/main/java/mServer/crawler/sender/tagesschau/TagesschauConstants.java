package mServer.crawler.sender.tagesschau;

/**
 * Constants for the Tagesschau crawler.
 * Handles the "vor 20 Jahren" (20 years ago) archive with daily news broadcasts.
 */
public final class TagesschauConstants {

  // Starting point: Tagesschau vor 20 Jahren (20 years ago)
  public static final String ARCHIVE_START_URL = "https://www.tagesschau.de/inland/tsvorzwanzigjahren-ts-142.html";

  // Private constructor to hide the implicit public one
  private TagesschauConstants() {
    // Utility class, do not instantiate
  }
}


