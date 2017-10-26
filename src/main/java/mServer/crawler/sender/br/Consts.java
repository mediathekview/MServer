package mServer.crawler.sender.br;

import java.time.format.DateTimeFormatter;

public final class Consts {
  public static final String BR_API_URL = "https://proxy-base.master.mango.express/graphql";
  public static final DateTimeFormatter BR_FORMATTER = DateTimeFormatter.ISO_DATE;
  public static final String JSON_SYNTAX_ERROR =
      "The site \"%s\" for the \"%s\" crawler in't a valid JSON page.";
  public static final String ATTRIBUTE_HREF = "href";

  private Consts() {
    super();
  }
}