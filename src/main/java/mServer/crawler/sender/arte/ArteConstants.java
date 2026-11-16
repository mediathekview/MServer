package mServer.crawler.sender.arte;

public class ArteConstants {
  public static final String VIDEOS_URL ="https://api.arte.tv/api/opa/v3/videos?limit=100&page=%s&sort=-videoRightsBegin&language=%s&kind=SHOW,MANUAL_CLIP,BONUS";
  public static final String VIDEO_URL_PLAYER ="https://api.arte.tv/api/player/v2/config/%s/%s"; //LANG/PROGRAMID
  public static final String API_TOKEN = "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";
  public static final int MAX_POSSIBLE_SUBPAGES = 100;
  private ArteConstants() {}
  
}
