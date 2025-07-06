package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ZdfPubFormResult {
  private final PagedElementListDTO<ZdfTopicUrlDto> topics;
  private final Set<ZdfFilmDto> films;

  public ZdfPubFormResult() {
    this.topics = new PagedElementListDTO<>();
    this.films = new HashSet<>();
  }

  public PagedElementListDTO<ZdfTopicUrlDto> getTopics() {
    return topics;
  }

  public Set<ZdfFilmDto> getFilms() {
    return films;
  }

  public void addTopic(ZdfTopicUrlDto topicUrlDto) {
    this.topics.addElement(topicUrlDto);
  }

  public void setNextPage(Optional<String> pageInfo) {
    this.topics.setNextPage(pageInfo);
  }

  public void addFilms(Set<ZdfFilmDto> films) {
    this.films.addAll(films);
  }
}
