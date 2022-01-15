package mServer.crawler.sender.funk.json;

import com.google.gson.JsonObject;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.funk.FilmInfoDto;
import mServer.crawler.sender.funk.FunkUrls;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class FunkVideoDeserializer extends AbstractFunkElementDeserializer<FilmInfoDto> {
  private static final Logger LOG = LogManager.getLogger(FunkVideoDeserializer.class);
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

  @Override
  protected FilmInfoDto mapToElement(final JsonObject jsonObject) {
    final FilmInfoDto filmInfo =
            new FilmInfoDto(createNexxCloudUrl(jsonObject.get(TAG_ENTITY_ID).getAsString()));
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
    }
    // Optionals
    JsonUtils.getAttributeAsString(jsonObject, TAG_DESCRIPTION).ifPresent(filmInfo::setDescription);

    final Optional<String> channelAlias =
            JsonUtils.getAttributeAsString(jsonObject, TAG_CHANNEL_ALIAS);
    final Optional<String> alias = JsonUtils.getAttributeAsString(jsonObject, TAG_ALIAS);
    if (channelAlias.isPresent() && alias.isPresent()) {
      filmInfo.setWebsite(
              FunkUrls.WEBSITE.getAsString(channelAlias.get(), alias.get()));
    }

    return filmInfo;
  }

  private String createNexxCloudUrl(final String entityId) {
    return FunkUrls.NEXX_CLOUD_VIDEO.getAsString(entityId);
  }

  @Override
  protected String[] getRequiredTags() {
    return new String[]{
            TAG_TITLE, TAG_ENTITY_ID, TAG_DURATION, TAG_PUBLICATION_DATE, TAG_CHANNEL_ID
    };
  }

  @Override
  protected String getElementListTag() {
    return TAG_VIDEO_DTO_LIST;
  }
}
