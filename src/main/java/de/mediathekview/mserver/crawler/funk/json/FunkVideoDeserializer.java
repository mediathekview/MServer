package de.mediathekview.mserver.crawler.funk.json;

import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class FunkVideoDeserializer extends AbstractFunkElementDeserializer<FilmInfoDto> {
  private static final Logger LOG = LogManager.getLogger(FunkVideoDeserializer.class);
  // "https://www.funk.net/channel/[channelAlias]/[alias]"
  private static final String WEBSITE_PATTERN = "https://www.funk.net/channel/%s/%s";
  private static final String DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);
  private static final String TAG_VIDEO_DTO_LIST = "videoDTOList";
  private static final String TAG_TITLE = "title";
  private static final String TAG_ENTITY_ID = "entityId";
  private static final String TAG_DURATION = "duration";
  private static final String TAG_DESCRIPTION = "description";
  private static final String TAG_PUBLICATION_DATE = "publicationDate";
  private static final String TAG_CHANNEL_ALIAS = "channelAlias";
  private static final String TAG_ALIAS = "alias";
  private static final String TAG_CHANNEL_ID = "channelId";

  public FunkVideoDeserializer(final Optional<AbstractCrawler> aCrawler) {
    super(aCrawler);
  }

  @Override
  protected FilmInfoDto mapToElement(final JsonObject jsonObject) {
    final FilmInfoDto filmInfo = new FilmInfoDto(jsonObject.get(TAG_ENTITY_ID).getAsString());
    // Required
    filmInfo.setTitle(jsonObject.get(TAG_TITLE).getAsString());
    filmInfo.setTopic(jsonObject.get(TAG_CHANNEL_ID).getAsString());
    filmInfo.setDuration(Duration.ofSeconds(jsonObject.get(TAG_DURATION).getAsInt()));

    final String timeText = jsonObject.get(TAG_PUBLICATION_DATE).getAsString();
    try {
      filmInfo.setTime(LocalDateTime.parse(timeText, DATE_TIME_FORMATTER));
    } catch (final DateTimeParseException dateTimeParseException) {
      LOG.error(
          String.format(
              "The text \"%s\" couldn't be parsed withe the pattern \"%s\".",
              timeText, DATE_TIME_FORMAT_PATTERN),
          dateTimeParseException);
      crawler.ifPresent(
          abstractCrawler ->
              abstractCrawler.printParseDebugMessage(timeText, DATE_TIME_FORMAT_PATTERN));
    }
    // Optionals
    JsonUtils.getAttributeAsString(jsonObject, TAG_DESCRIPTION).ifPresent(filmInfo::setDescription);

    final Optional<String> channelAlias =
        JsonUtils.getAttributeAsString(jsonObject, TAG_CHANNEL_ALIAS);
    final Optional<String> alias = JsonUtils.getAttributeAsString(jsonObject, TAG_ALIAS);
    if (channelAlias.isPresent() && alias.isPresent()) {
      filmInfo.setWebsite(String.format(WEBSITE_PATTERN, channelAlias.get(), alias.get()));
    }

    return filmInfo;
  }

  @Override
  protected void addSizeToStatistic(final int actualSize) {
    super.addSizeToStatistic(actualSize);
    crawler.ifPresent(
        c -> {
          c.incrementMaxCountBySizeAndGetNewSize(actualSize);
          c.updateProgress();
        });
  }

  @Override
  protected String[] getRequiredTags() {
    return new String[] {
      TAG_TITLE, TAG_ENTITY_ID, TAG_DURATION, TAG_PUBLICATION_DATE, TAG_CHANNEL_ID
    };
  }

  @Override
  protected String getElementListTag() {
    return TAG_VIDEO_DTO_LIST;
  }
}
