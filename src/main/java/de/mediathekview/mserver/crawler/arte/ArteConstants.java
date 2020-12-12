package de.mediathekview.mserver.crawler.arte;

public class ArteConstants {

  public static final String BASE_URL_WWW = "https://www.arte.tv";

  public static final String DAY_PAGE_URL =
      BASE_URL_WWW + "/guide/api/emac/v3/%s/web/pages/TV_GUIDE/?day=%s";

  public static final int SUBCATEGORY_LIMIT = 100;
  public static final String URL_SUBCATEGORIES =
      "https://api.arte.tv/api/opa/v3/subcategories?language=%s&limit="+SUBCATEGORY_LIMIT;
  public static final String URL_SUBCATEGORY_VIDEOS =
      "%s/guide/api/emac/v3/%s/web/zones/videos_subcategory/?id=%s&limit="+SUBCATEGORY_LIMIT+"&page=%s";

  public static final String URL_FILM_DETAILS = "https://api.arte.tv/api/opa/v3/programs/%s/%s";
  public static final String URL_FILM_VIDEOS =
      "https://api.arte.tv/api/player/v1/config/%s/%s?platform=ARTE_NEXT";

  public static final String AUTH_TOKEN =
      "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";

  private ArteConstants() {}
}
