package de.mediathekview.mserver.base;

import java.time.format.DateTimeFormatter;

public final class Consts {
  public static final DateTimeFormatter BR_FORMATTER = DateTimeFormatter.ISO_DATE;
  public static final String JSON_SYNTAX_ERROR =
      "The site \"%s\" for the \"%s\" crawler in't a valid JSON page.";

  public static final String ATTRIBUTE_CONTENT = "content";
  public static final String ATTRIBUTE_HREF = "href";
  public static final String ATTRIBUTE_SRC = "src";
  public static final String ATTRIBUTE_TITLE = "title";
  public static final String ATTRIBUTE_VALUE = "value";

  private Consts() {
    super();
  }

}
