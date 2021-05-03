package de.mediathekview.mserver.crawler.basic;

public class GraphQlUrlDto extends CrawlerUrlDTO {

  private final String requestBody;

  public GraphQlUrlDto(String aUrl, String aRequestBody) {
    super(aUrl);
    this.requestBody = aRequestBody;
  }

  public String getRequestBody() {
    return requestBody;
  }
}
