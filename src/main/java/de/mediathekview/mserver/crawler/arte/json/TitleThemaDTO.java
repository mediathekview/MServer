package de.mediathekview.mserver.crawler.arte.json;

class TitleThemaDTO {
  private String thema;
  private String title;

  public TitleThemaDTO(final String aTitle, final String aThema) {
    title = aTitle;
    thema = aThema;
  }

  public String getThema() {
    return thema;
  }

  public String getTitle() {
    return title;
  }

  public void setThema(final String aThema) {
    thema = aThema;
  }

  public void setTitle(final String aTitle) {
    title = aTitle;
  }


}
