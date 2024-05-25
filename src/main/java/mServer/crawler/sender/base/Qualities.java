package mServer.crawler.sender.base;

public enum Qualities
{
    HD("HD"), NORMAL("Normal"), SMALL("Klein"), UHD("UHD");

    private final String description;

    Qualities(String aDescription)
    {
        description = aDescription;
    }

    public String getDescription()
    {
        return description;
    }
    
    public static Qualities getResolutionFromWidth(final int width) {
      if (width > 1280) {
        return Qualities.HD;
      } else if (width > 640) {
        return Qualities.NORMAL;
      } else {
        return Qualities.SMALL;  
      }
    } 
}
