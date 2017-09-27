package de.mediathekview.mserver.base;

import java.time.format.DateTimeFormatter;

public final class Consts {
  public static final String BR_API_URL = "https://proxy-base.master.mango.express/graphql";
  public static final DateTimeFormatter BR_FORMATTER = DateTimeFormatter.ISO_DATE;
  public static final String JSON_SYNTAX_ERROR =
      "The site \"%s\" for the \"%s\" crawler in't a valid JSON page.";

  private Consts() {
    super();
  }

}
