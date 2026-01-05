package mServer.crawler.sender.arte;

public class ArteConstants {
  public static final String VIDEOS_URL ="https://api.arte.tv/api/opa/v3/videos?limit=100&page=%s&sort=-broadcastBegin&language=%s";
  public static final String VIDEOS_URL_ALT ="https://api.arte.tv/api/opa/v3/videos?limit=100&page=%s&sort=broadcastBegin&language=%s";
  public static final String VIDEO_URL ="https://www.arte.tv/hbbtvv2/services/web/index.php/OPA/v3/streams/%s/%s/%s"; //PROGRAMID/KIND/LANG
  public static final String API_TOKEN = "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";
  public static final int MAX_POSSIBLE_SUBPAGES = 100;
  private ArteConstants() {}
  
}
