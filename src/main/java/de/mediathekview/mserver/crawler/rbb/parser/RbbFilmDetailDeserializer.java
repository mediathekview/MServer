package de.mediathekview.mserver.crawler.rbb.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_CONTENT;

import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;

/**
 * Extracts film infos from html page.
 */
public class RbbFilmDetailDeserializer {

  private static final String TOPIC_SELECTOR = "meta[name=dcterms.isPartOf]";
  private static final String TITLE_SELECTOR = "meta[name=dcterms.title]";
  private static final String DESCRIPTION_SELECTOR = "meta[name=description]";
  private static final String TIME_SELECTOR = "meta[name=dcterms.date]";
  private static final String DURATION_SELECTOR = "meta[property=video:duration]";
  private static final String REGEX_PATTERN_DOCUMENT_ID = "(?<=&documentId=)\\d+";

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

  private final String baseUrl;

  public RbbFilmDetailDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  /**
   * deserializes film infos.
   *
   * @param aUrlDto the topic dto.
   * @param aDocument the html document.
   * @return the extracted film infos.
   */
  public Optional<FilmInfoDto> deserialize(final CrawlerUrlDTO aUrlDto, final Document aDocument) {

    Optional<String> topic = HtmlDocumentUtils.getElementAttributeString(TOPIC_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    Optional<String> title = HtmlDocumentUtils.getElementAttributeString(TITLE_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    Optional<String> description = HtmlDocumentUtils.getElementAttributeString(DESCRIPTION_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    Optional<String> videoUrl = parseVideoUrl(aUrlDto.getUrl());
    final Optional<LocalDateTime> time = parseDate(aDocument);
    final Optional<Duration> duration = parseDuration(aDocument);

    if (topic.isPresent() && title.isPresent() && videoUrl.isPresent()) {
      FilmInfoDto dto = new FilmInfoDto(videoUrl.get());
      dto.setTopic(topic.get());
      dto.setTitle(title.get());

      description.ifPresent(dto::setDescription);
      duration.ifPresent(dto::setDuration);
      time.ifPresent(dto::setTime);
      dto.setWebsite(aUrlDto.getUrl());

      return Optional.of(dto);
    }

    return Optional.empty();
  }

  private static Optional<LocalDateTime> parseDate(final Document aDocument) {
    Optional<String> dateTime
        = HtmlDocumentUtils.getElementAttributeString(TIME_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    if (dateTime.isPresent()) {
      LocalDateTime localDateTime
          = LocalDateTime.parse(dateTime.get(), DATE_TIME_FORMATTER);
      return Optional.of(localDateTime);
    }

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
    final String documentId = getDocumentIdFromUrl(aUrl);
    if (documentId.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(String.format(RbbConstants.URL_VIDEO_JSON, baseUrl, documentId));
  }

  private String getDocumentIdFromUrl(final String aUrl) {
    final Matcher documentIdRegexMatcher = Pattern.compile(REGEX_PATTERN_DOCUMENT_ID).matcher(aUrl);
    return documentIdRegexMatcher.find() ? documentIdRegexMatcher.group() : "";
  }
}
