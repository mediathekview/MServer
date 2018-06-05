package de.mediathekview.mserver.crawler.phoenix.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_CONTENT;

import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.jsoup.nodes.Document;

public class PhoenixFilmDeserializer {

  private static final String INFO_SELECTOR = "meta[property=og:description]";
  private static final String TITLE_SELECTOR = "meta[property=og:title]";
  private static final String DESCRIPTION_SELECTOR = "meta[name=description]";
  private static final String TIME_SELECTOR = "meta[name=dcterms.date]";
  private static final String DURATION_SELECTOR = "meta[property=video:duration]";

  /**
   * deserializes film infos.
   *
   * @param aUrlDto the topic dto.
   * @param aDocument the html document.
   * @return the extracted film infos.
   */
  public Optional<FilmInfoDto> deserialize(final CrawlerUrlDTO aUrlDto, final Document aDocument) {
    Optional<String> info = HtmlDocumentUtils.getElementAttributeString(INFO_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    Optional<String> topic = parseTopic(info.get());
    Optional<String> title = HtmlDocumentUtils.getElementAttributeString(TITLE_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    Optional<String> description = HtmlDocumentUtils.getElementAttributeString(DESCRIPTION_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    Optional<String> videoUrl = parseVideoUrl(aUrlDto.getUrl());
    final Optional<LocalDateTime> time = parseDate(info.get());

    if (topic.isPresent() && title.isPresent() && videoUrl.isPresent()) {
      FilmInfoDto dto = new FilmInfoDto(videoUrl.get());
      dto.setTopic(topic.get());
      dto.setTitle(title.get());

      description.ifPresent(dto::setDescription);
      time.ifPresent(dto::setTime);
      dto.setWebsite(aUrlDto.getUrl());

      return Optional.of(dto);
    }

    return Optional.empty();
  }

  private static Optional<String> parseTopic(final String aInfo) {
    int index = aInfo.indexOf(':');
    if (index > 0) {
      return Optional.of(aInfo.substring(0, index));
    }

    return Optional.empty();
  }

  private static Optional<LocalDateTime> parseDate(final String aInfo) {

    int indexDateBegin = aInfo.indexOf(':') + 6;
    int indexDateEnd = aInfo.indexOf(", ");
    int indexTimeBegin = indexDateEnd + 2;
    int indexTimeEnd = aInfo.indexOf(" - ");

    String date = aInfo.substring(indexDateBegin, indexDateEnd);
    String time = aInfo.substring(indexTimeBegin, indexTimeEnd);

    return Optional.empty();
  }

  private static Optional<Duration> parseDuration(final Document aDocument) {
    Optional<String> duration
        = HtmlDocumentUtils.getElementAttributeString(DURATION_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    if (!duration.isPresent()) {
      return Optional.empty();
    }

    Long durationValue = Long.parseLong(duration.get());
    return Optional.of(Duration.ofSeconds(durationValue));
  }

  private Optional<String> parseVideoUrl(final String aUrl) {
    final String documentId = "";//getDocumentIdFromUrl(aUrl);
    if (documentId.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(String.format(RbbConstants.URL_VIDEO_JSON, documentId));
  }
}
