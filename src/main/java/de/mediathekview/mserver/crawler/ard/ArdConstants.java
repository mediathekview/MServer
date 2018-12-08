package de.mediathekview.mserver.crawler.ard;

public class ArdConstants {

  public static final String BASE_URL = "https://api.ardmediathek.de/public-gateway";
  
  public static final String DEFAULT_CLIENT = "ard";
  public static final String DEFAULT_DEVICE = "pc";

  public static final int QUERY_DAY_SEARCH_VERSION = 1;
  public static final String QUERY_DAY_SEARCH_HASH = "506f09bd00b4e8c966f703dac3e5bba2f41fd85189a230ff65a132277d4fec3c";
  
  public static final int QUERY_FILM_VERSION = 1;
  public static final String QUERY_FILM_HASH = "a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da";

  public static final String WEBSITE_URL = "https://www.ardmediathek.de/ard/player/%s";

  private ArdConstants() {}
}
