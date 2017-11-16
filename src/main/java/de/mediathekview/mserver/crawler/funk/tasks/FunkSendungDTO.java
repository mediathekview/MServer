package de.mediathekview.mserver.crawler.funk.tasks;

import java.util.Optional;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class FunkSendungDTO extends CrawlerUrlDTO {

  private Optional<String> thema;

  public FunkSendungDTO(final String aUrl) {
    super(aUrl);
    thema = Optional.empty();
  }

  public Optional<String> getThema() {
    return thema;
  }

  public void setThema(final Optional<String> aThema) {
    thema = aThema;
  }

}
