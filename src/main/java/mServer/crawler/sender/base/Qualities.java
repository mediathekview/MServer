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
      if (width >= 2160) {
        return Qualities.UHD;
      }
      if (width >= 1280) {
        return Qualities.HD;
      }
      if (width >= 720) {
        return Qualities.NORMAL;
      }
      return Qualities.SMALL;
    }
}
