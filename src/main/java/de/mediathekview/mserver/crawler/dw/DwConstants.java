package de.mediathekview.mserver.crawler.dw;

import java.util.Arrays;
import java.util.List;

public class DwConstants {
  private DwConstants() {}
  
  public static final String URL_BASE = "https://api.dw.com/api";

  public static final String URL_OVERVIEW = "/list/mediacenter/1?pageIndex=1";
  
  public static final List<String> REGULAR_TOPICS = Arrays.asList("Euromaxx", "Shift", "Fokus Europa", "Projekt Zukunft", "Global Us");
  
}
