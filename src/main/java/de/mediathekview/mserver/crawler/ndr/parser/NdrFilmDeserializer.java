package de.mediathekview.mserver.crawler.ndr.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_CONTENT;
import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_SRC;

import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.ndr.NdrConstants;
import de.mediathekview.mserver.crawler.rbb.parser.RbbFilmInfoDto;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NdrFilmDeserializer {

  private static final String TITLE_SELECTOR = "meta[name=title]";
  private static final String TOPIC1_SELECTOR = "header > h1 > span[itemprop=headline]";
  private static final String TOPIC2_SELECTOR = "span[itemprop=alternateName]";
  private static final String DESCRIPTION_SELECTOR = "meta[name=description]";
  private static final String TIME_SELECTOR2 = "span[itemprop=datePublished]";
  private static final String TIME_SELECTOR3 = "span[itemprop=uploadDate]";
  private static final String DURATION_SELECTOR = "span[itemprop=duration]";
  private static final String START_DATE_SELECTOR = "span[itemprop=startDate]";
  private static final String END_DATE_SELECTOR = "span[itemprop=endDate]";
  private static final String IFRAME_SELECTOR = "iframe";

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

  /**
   * deserializes film infos.
   *
   * @param aUrlDto the topic dto.
   * @param aDocument the html document.
   * @return the extracted film infos.
   */
  public Optional<RbbFilmInfoDto> deserialize(final CrawlerUrlDTO aUrlDto, final Document aDocument) {

    Optional<String> topic = parseTopic(aDocument);
    Optional<String> title = HtmlDocumentUtils.getElementAttributeString(TITLE_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    Optional<String> description = HtmlDocumentUtils.getElementAttributeString(DESCRIPTION_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    Optional<String> videoUrl = parseVideoUrl(aDocument);
    final Optional<LocalDateTime> time = parseDate(aDocument);
    final Optional<Duration> duration = parseDuration(aDocument);

    if (topic.isPresent() && title.isPresent() && videoUrl.isPresent()) {
      RbbFilmInfoDto dto = new RbbFilmInfoDto(videoUrl.get());
      dto.setTopic(topic.get().trim());
      dto.setTitle(title.get().trim());

      description.ifPresent(dto::setDescription);
      duration.ifPresent(dto::setDuration);
      time.ifPresent(dto::setTime);
      dto.setWebsite(aUrlDto.getUrl());

      return Optional.of(dto);
    }

    return Optional.empty();
  }

  private static Optional<String> parseTopic(final Document aDocument) {
    Optional<String> topic = HtmlDocumentUtils.getElementString(TOPIC1_SELECTOR, aDocument);

    if (!topic.isPresent()) {
      Elements topicElements = aDocument.select(TOPIC2_SELECTOR);

      // sometimes there are several elements of TOPIC2_SELECTOR and the first one is empty
      for (Element topicElement : topicElements) {
        final String text = topicElement.text();
        if (!text.isEmpty()) {
          topic = Optional.of(text);
          break;
        }
      }

    }

    return topic;
  }

  private static Optional<LocalDateTime> parseDate(final Document aDocument) {
    Optional<String> dateTime
        = HtmlDocumentUtils.getElementAttributeString(START_DATE_SELECTOR, ATTRIBUTE_CONTENT, aDocument);

    if (!dateTime.isPresent() || dateTime.get().isEmpty()) {
      dateTime = HtmlDocumentUtils.getElementAttributeString(TIME_SELECTOR2, ATTRIBUTE_CONTENT, aDocument);
    }
    if (!dateTime.isPresent() || dateTime.get().isEmpty()) {
      dateTime = HtmlDocumentUtils.getElementAttributeString(TIME_SELECTOR3, ATTRIBUTE_CONTENT, aDocument);
    }

    if (dateTime.isPresent() && !dateTime.get().isEmpty()) {
      LocalDateTime localDateTime
          = LocalDateTime.parse(dateTime.get(), DATE_TIME_FORMATTER);
      return Optional.of(localDateTime);
    }

    return Optional.empty();
  }

  private static Optional<Duration> parseDuration(final Document aDocument) {
    Optional<String> duration
        = HtmlDocumentUtils.getElementAttributeString(DURATION_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    if (duration.isPresent() && !duration.get().isEmpty()) {
      return parseDurationStringValue(duration.get());
    }

    return calculateDuration(aDocument);
  }

  private static Optional<Duration> calculateDuration(final Document aDocument) {
    Optional<String> startDate = HtmlDocumentUtils.getElementAttributeString(START_DATE_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    Optional<String> endDate = HtmlDocumentUtils.getElementAttributeString(END_DATE_SELECTOR, ATTRIBUTE_CONTENT, aDocument);

    if (!startDate.isPresent() || !endDate.isPresent()) {
      return Optional.empty();
    }

    LocalDateTime startDateTime
        = LocalDateTime.parse(startDate.get(), DATE_TIME_FORMATTER);
    LocalDateTime endDateTime
        = LocalDateTime.parse(endDate.get(), DATE_TIME_FORMATTER);

    long seconds = ChronoUnit.SECONDS.between(startDateTime, endDateTime);
    return Optional.of(Duration.ofSeconds(seconds));
  }

  private static Optional<Duration> parseDurationStringValue(final String aDuration) {
    final String[] parts = aDuration.split(":");
    int index = 0;
    Long durationValue = 0L;

    if (parts.length == 3) {
      durationValue += (Long.parseLong(parts[index]) * 3600);
      index++;
    }
    if (parts.length >= 2) {
      durationValue += (Long.parseLong(parts[index])) * 60;
      index++;
    }
    if (parts.length >= 1) {
      durationValue += (Long.parseLong(parts[index]));
    }

    return Optional.of(Duration.ofSeconds(durationValue));
  }

  private Optional<String> parseVideoUrl(final Document aDocument) {

    // From
    // http://www.ndr.de/fernsehen/sendungen/sportclub/schwenker172-ardplayer_image-58390aa6-8e8a-458b-b3a7-d7b23e91e186_theme-ndrde.html
    // To
    // http://www.ndr.de/fernsehen/sendungen/sportclub/schwenker172-ardjson_image-58390aa6-8e8a-458b-b3a7-d7b23e91e186.json
    final Optional<String> playerUrl = HtmlDocumentUtils.getElementAttributeString(IFRAME_SELECTOR, ATTRIBUTE_SRC, aDocument);
    if (playerUrl.isPresent()) {
      String url = playerUrl.get()
          .replaceAll("ardplayer", "ardjson")
          .replaceAll("_theme-ndrde.html", ".json");

      // remove query parameter "autoplay"
      int jsonIndex = url.lastIndexOf("?autoplay");
      if (jsonIndex > 0) {
        url = url.substring(0, jsonIndex);
      }

      url = UrlUtils.addDomainIfMissing(url, NdrConstants.URL_BASE);
      return Optional.of(url);
    }

    return Optional.empty();
  }
}
