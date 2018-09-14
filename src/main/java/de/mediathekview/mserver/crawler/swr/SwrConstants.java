package de.mediathekview.mserver.crawler.swr;

public final class SwrConstants {

  /**
   * Base url of the SWR mediathek.
   */
  public static final String URL_BASE = "https://swrmediathek.de";

  /**
   * Url of the films of a day. Add the day in format YYYYMMDD.
   */
  public static final String URL_DAY_PAGE = URL_BASE + "/sendungverpasst.htm?show=&date=";

  /**
   * Url of the topics page.
   */
  public static final String URL_TOPICS = URL_BASE + "/tvlist.htm";

  /**
   * Url of json request to receive film details. Id of the film has to be added at the end.
   */
  public static final String URL_FILM_DETAIL_REQUEST = URL_BASE + "/AjaxEntry?ekey=";

  /**
   * Url of the film website. Id of the film has to be added at the end.
   */
  public static final String URL_FILM_PAGE = URL_BASE + "/player.htm?show=";

  public static final int MAX_DAYS_PAST = 7;

  private SwrConstants() {
  }
}
