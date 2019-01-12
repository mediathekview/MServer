package de.mediathekview.mserver.crawler.arte;

public class ArteConstants {

  public static final String URL_FILM_DETAILS  = "https://api.arte.tv/api/opa/v3/programs/%s/%s";

  public static final String URL_FILM_VIDEOS = "https://api.arte.tv/api/player/v1/config/%s/%s?platform=ARTE_NEXT";

  public static final String URL_SUBCATEGORIES = "https://api.arte.tv/api/opa/v3/subcategories?language=%s&limit=100";

  public static final String URL_SUBCATEGORY_VIDEOS = "https://www.arte.tv/guide/api/api/zones/%s/videos_subcategory/?id=%s&limit=100";
  public static final String AUTH_TOKEN =
      "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";

  private ArteConstants() {}
}
