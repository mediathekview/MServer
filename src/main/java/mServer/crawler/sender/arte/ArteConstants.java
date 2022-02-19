package mServer.crawler.sender.arte;

public class ArteConstants {

  public static final String ARTE_EN = "ARTE.EN";
  public static final String ARTE_ES = "ARTE.ES";
  public static final String ARTE_IT = "ARTE.IT";
  public static final String ARTE_PL = "ARTE.PL";

  public static final String BASE_URL_WWW = "https://www.arte.tv";

  public static final String DAY_PAGE_URL =
      BASE_URL_WWW + "/api/rproxy/emac/v3/%s/web/pages/TV_GUIDE/?day=%s";

  public static final int SUBCATEGORY_LIMIT = 40;
  public static final String URL_SUBCATEGORIES =
      "https://api.arte.tv/api/opa/v3/subcategories?language=%s&limit="+SUBCATEGORY_LIMIT;
  public static final String URL_SUBCATEGORY_VIDEOS =
      "%s/api/rproxy/emac/v3/%s/web/data/MOST_RECENT_SUBCATEGORY/?subCategoryCode=%s&page=%s&limit="+SUBCATEGORY_LIMIT;
  public static final String URL_VIDEO_LIST =
          "%s/api/rproxy/emac/v3/%s/web/data/VIDEO_LISTING/?imageFormats=landscape&authorizedAreas=DE_FR,EUR_DE_FR,SAT,ALL&videoType=%s&imageWithText=true&page=%s&limit=100";

  public static final String VIDEO_LIST_TYPE_RECENT = "MOST_RECENT";
  public static final String VIDEO_LIST_TYPE_LAST_CHANCE = "LAST_CHANCE";

  public static final String URL_FILM_DETAILS = "https://api.arte.tv/api/opa/v3/programs/%s/%s";
  public static final String URL_FILM_VIDEOS =
      "https://api.arte.tv/api/player/v1/config/%s/%s?platform=ARTE_NEXT";

  public static final String AUTH_TOKEN =
      "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";

  private ArteConstants() {}
}
