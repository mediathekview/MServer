package mServer.crawler.sender.zdf.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.zdf.ZdfFilmDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ZdfFilmDetailDeserializer implements JsonDeserializer<Optional<ZdfFilmDto>> {

  private static final Logger LOG = LogManager.getLogger(ZdfFilmDetailDeserializer.class);

  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";

  private static final String JSON_ELEMENT_BEGIN = "airtimeBegin";
  private static final String JSON_ELEMENT_BRAND = "http://zdf.de/rels/brand";
  private static final String JSON_ELEMENT_CATEGORY = "http://zdf.de/rels/category";
  private static final String JSON_ELEMENT_BROADCAST = "http://zdf.de/rels/cmdm/broadcasts";
  private static final String JSON_ELEMENT_DURATION = "duration";
  private static final String JSON_ELEMENT_EDITORIAL_DATE = "editorialDate";
  private static final String JSON_ELEMENT_LEAD_PARAGRAPH = "leadParagraph";
  private static final String JSON_ELEMENT_MAIN_VIDEO = "mainVideoContent";
  private static final String JSON_ELEMENT_PROGRAM_ITEM = "programmeItem";
  private static final String JSON_ELEMENT_SHARING_URL = "http://zdf.de/rels/sharing-url";
  private static final String JSON_ELEMENT_SUBTITLE = "subtitle";
  private static final String JSON_ELEMENT_TARGET = "http://zdf.de/rels/target";
  private static final String JSON_ELEMENT_TITLE = "title";
  private static final String JSON_ELEMENT_TEASER_TEXT = "teasertext";
  private static final String JSON_ATTRIBUTE_TEMPLATE = "http://zdf.de/rels/streams/ptmd-template";

  private static final String PLACEHOLDER_PLAYER_ID = "{playerId}";
  private static final String PLAYER_ID = "ngplayer_2_3";

  private static final DateTimeFormatter DATE_FORMATTER_EDITORIAL
          = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"); // 2016-10-29T16:15:00.000+02:00
  private static final DateTimeFormatter DATE_FORMATTER_AIRTIME
          = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"); // 2016-10-29T16:15:00+02:00

  private final String apiUrlBase;

  public ZdfFilmDetailDeserializer(final String apiUrlBase) {
    this.apiUrlBase = apiUrlBase;
  }

  @Override
  public Optional<ZdfFilmDto> deserialize(
          JsonElement aJsonObject, Type aType, JsonDeserializationContext aContext) {
    JsonObject rootNode = aJsonObject.getAsJsonObject();
    JsonObject programItemTarget = null;
    JsonObject mainVideoTarget = null;

    if (rootNode.has(JSON_ELEMENT_PROGRAM_ITEM)
            && !rootNode.get(JSON_ELEMENT_PROGRAM_ITEM).isJsonNull()) {
      JsonArray programItem = rootNode.getAsJsonArray(JSON_ELEMENT_PROGRAM_ITEM);
      programItemTarget
              = programItem.get(0).getAsJsonObject().get(JSON_ELEMENT_TARGET).getAsJsonObject();
    }
    if (rootNode.has(JSON_ELEMENT_MAIN_VIDEO)
            && !rootNode.get(JSON_ELEMENT_MAIN_VIDEO).isJsonNull()) {
      JsonObject mainVideoElement = rootNode.get(JSON_ELEMENT_MAIN_VIDEO).getAsJsonObject();
      if (mainVideoElement != null) {
        JsonObject mainVideo = mainVideoElement.getAsJsonObject();
        mainVideoTarget = mainVideo.get(JSON_ELEMENT_TARGET).getAsJsonObject();
      }
    }

    Optional<String> title = parseTitle(rootNode, programItemTarget);
    Optional<String> topic = parseTopic(rootNode);
    Optional<String> description = parseDescription(rootNode);

    Optional<String> website = parseWebsiteUrl(rootNode);
    Optional<LocalDateTime> time = parseAirtime(rootNode, programItemTarget);
    Optional<Duration> duration = parseDuration(mainVideoTarget);

    Optional<String> downloadUrl = parseDownloadUrl(mainVideoTarget);

    if (title.isPresent() && downloadUrl.isPresent()) {
      return Optional.of(new ZdfFilmDto(downloadUrl.get(), topic, title.get(), description, website, time, duration));
    } else {
      LOG.error("ZdfFilmDetailDeserializer: no title or url found");
    }

    return Optional.empty();
  }

  private Optional<String> parseDownloadUrl(JsonObject mainVideoContent) {
    if (mainVideoContent != null) {
      Optional<String> urlOptional
              = JsonUtils.getAttributeAsString(mainVideoContent, JSON_ATTRIBUTE_TEMPLATE);

      if (urlOptional.isPresent()) {
        String url = UrlUtils.addDomainIfMissing(urlOptional.get(), apiUrlBase).replace(PLACEHOLDER_PLAYER_ID, PLAYER_ID);
        return Optional.of(url);
      }
    }
    return Optional.empty();
  }

  private Optional<LocalDateTime> parseAirtime(
          JsonObject aRootNode, JsonObject aProgramItemTarget) {
    Optional<String> date;
    DateTimeFormatter formatter;

    // use broadcast airtime if found
    if (aProgramItemTarget != null) {
      JsonArray broadcastArray = aProgramItemTarget.getAsJsonArray(JSON_ELEMENT_BROADCAST);

      if (broadcastArray == null || broadcastArray.size() < 1) {
        date = getEditorialDate(aRootNode);
        formatter = DATE_FORMATTER_EDITORIAL;
      } else {
        // array is ordered ascending though the oldest broadcast is the first entry
        date
                = Optional.of(
                        broadcastArray.get(0).getAsJsonObject().get(JSON_ELEMENT_BEGIN).getAsString());
        formatter = DATE_FORMATTER_AIRTIME;
      }
      return date.map(s -> LocalDateTime.parse(s, formatter));
    } else {
      // use editorialdate
      date = getEditorialDate(aRootNode);
      if (date.isPresent()) {
        final ZonedDateTime inputDateTime = ZonedDateTime.parse(date.get());
        final LocalDateTime localDateTime
                = inputDateTime.withZoneSameInstant(ZoneId.of(GERMAN_TIME_ZONE)).toLocalDateTime();
        return Optional.of(localDateTime);
      }
    }

    return Optional.empty();
  }

  private Optional<String> getEditorialDate(JsonObject aRootNode) {
    if (aRootNode.has(JSON_ELEMENT_EDITORIAL_DATE)) {
      return Optional.of(aRootNode.get(JSON_ELEMENT_EDITORIAL_DATE).getAsString());
    }

    return Optional.empty();
  }

  private Optional<String> parseWebsiteUrl(JsonObject aRootNode) {
    if (aRootNode.has(JSON_ELEMENT_SHARING_URL)) {
      return Optional.of(aRootNode.get(JSON_ELEMENT_SHARING_URL).getAsString());
    }

    return Optional.empty();
  }

  private Optional<Duration> parseDuration(JsonObject mainVideoTarget) {
    if (mainVideoTarget != null) {
      JsonElement duration = mainVideoTarget.get(JSON_ELEMENT_DURATION);
      if (duration != null) {
        return Optional.of(Duration.ofSeconds(duration.getAsInt()));
      }
    }

    return Optional.empty();
  }

  private Optional<String> parseDescription(JsonObject aRootNode) {
    JsonElement leadParagraph = aRootNode.get(JSON_ELEMENT_LEAD_PARAGRAPH);
    if (leadParagraph != null) {
      return Optional.of(leadParagraph.getAsString());
    } else {
      JsonElement teaserText = aRootNode.get(JSON_ELEMENT_TEASER_TEXT);
      if (teaserText != null) {
        return Optional.of(teaserText.getAsString());
      }
    }

    return Optional.empty();
  }

  private Optional<String> parseTitle(JsonObject aRootNode, JsonObject aTarget) {
    Optional<String> title = parseTitleValue(aRootNode, aTarget);
    return title.map(s -> s.replaceAll("\\(CC.*\\) - .* Creative Commons.*", ""));
  }

  private Optional<String> parseTitleValue(JsonObject aRootNode, JsonObject aTarget) {
    // use property "title" if found
    JsonElement titleElement = aRootNode.get(JSON_ELEMENT_TITLE);
    if (titleElement != null) {
      JsonElement subTitleElement = aRootNode.get(JSON_ELEMENT_SUBTITLE);
      if (subTitleElement != null) {
        return Optional.of(titleElement.getAsString().trim() + " - " + subTitleElement.getAsString());
      } else {
        return Optional.of(titleElement.getAsString());
      }
    } else {
      // programmItem target required to determine title
      if (aTarget != null && aTarget.has(JSON_ELEMENT_TITLE)) {
        String title = aTarget.get(JSON_ELEMENT_TITLE).getAsString();
        String subTitle = aTarget.get(JSON_ELEMENT_SUBTITLE).getAsString();

        if (subTitle.isEmpty()) {
          return Optional.of(title);
        } else {
          return Optional.of(title.trim() + " - " + subTitle);
        }
      }
    }

    return Optional.empty();
  }

  private Optional<String> parseTopic(JsonObject aRootNode) {
    JsonObject brand = aRootNode.getAsJsonObject(JSON_ELEMENT_BRAND);
    JsonObject category = aRootNode.getAsJsonObject(JSON_ELEMENT_CATEGORY);

    if (brand != null) {
      // first use brand
      JsonElement topic = brand.get(JSON_ELEMENT_TITLE);
      if (topic != null) {
        return Optional.of(topic.getAsString());
      }
    }

    if (category != null) {
      // second use category
      JsonElement topic = category.get(JSON_ELEMENT_TITLE);
      if (topic != null) {
        return Optional.of(topic.getAsString());
      }
    }

    return Optional.empty();
  }
}
