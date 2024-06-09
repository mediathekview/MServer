package de.mediathekview.mserver.crawler.artem;

public final class ArteMConstants {
  //
  public static final int PAGE_LIMIT = 100;
  //
  public static final String HOST = "https://api.arte.tv";
  //
  public static final String ALL_VIDEOS = HOST + "/api/opa/v3/videos?language=de&sort=-lastModified&limit=" + PAGE_LIMIT;
  //
  public static final String AUTH = "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";
  
  private ArteMConstants() {}
  //
}
