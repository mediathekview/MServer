package mServer.crawler.sender.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdFilmDto;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.base.JsonUtils;
import mServer.crawler.sender.base.Qualities;
import mServer.crawler.sender.base.UrlUtils;

import org.apache.logging.log4j.LogManager;

public class ArdFilmDeserializer implements JsonDeserializer<List<ArdFilmDto>> {

  private static final org.apache.logging.log4j.Logger LOG
      = LogManager.getLogger(ArdFilmDeserializer.class);

  private static final String GERMAN_TIME_ZONE = "Europe/Berlin";

  private static final String ELEMENT_EMBEDDED = "embedded";
  private static final String ELEMENT_MEDIA_COLLECTION = "mediaCollection";
  private static final String ELEMENT_PUBLICATION_SERVICE = "publicationService";
  private static final String ELEMENT_SHOW = "show";
  private static final String ELEMENT_TEASERS = "teasers";
  private static final String ELEMENT_WIDGETS = "widgets";
  private static final String[] ELEMENT_SUBTITLES = {"mediaCollection","embedded","subtitles"};
  private static final String ELEMENT_SOURCES = "sources";
  private static final String ELEMENT_STREAMS = "streams";
  private static final String ELEMENT_MEDIA = "media";
  private static final String ELEMENT_AUDIO = "audios";
  

  private static final String ATTRIBUTE_BROADCAST = "broadcastedOn";
  private static final String[] ATTRIBUTE_DURATION = {"meta","duration"};
  private static final String[] ATTRIBUTE_DURATION_SEC = {"meta","durationSeconds"};
  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_NAME = "name";
  private static final String ATTRIBUTE_PARTNER = "partner";
  private static final String ATTRIBUTE_SYNOPSIS = "synopsis";
  private static final String ATTRIBUTE_TITLE = "title";
  private static final String ATTRIBUTE_URL = "url";
  private static final String ATTRIBUTE_RESOLUTION_H = "maxHResolutionPx";
  private static final String ATTRIBUTE_MIME = "mimeType";
  private static final String ATTRIBUTE_KIND = "kind";
  private static final String ATTRIBUTE_ADUIO_LANG = "languageCode";

  private static final String MARKER_VIDEO_MP4 = "video/mp4"; 
  private static final String MARKER_VIDEO_STANDARD = "standard";
  private static final String MARKER_VIDEO_CATEGORY_MAIN = "main";
  private static final String MARKER_VIDEO_CATEGORY_MPEG = "application/vnd.apple.mpegurl";
  private static final String MARKER_VIDEO_AD = "audio-description";
  private static final String MARKER_VIDEO_DGS = "sign-language";
  private static final String MARKER_VIDEO_OV = "OV";
  private static final String MARKER_VIDEO_DE = "deu";
  
  private static final DateTimeFormatter DATE_FORMAT
      = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
      = DateTimeFormatter.ofPattern("HH:mm:ss");

  // the key of the map is the value of publicationService.channelType in film.json
  private static final Map<String, String> ADDITIONAL_SENDER = new HashMap<>();

  static {
    ADDITIONAL_SENDER.put("rbb", Const.RBB);
    ADDITIONAL_SENDER.put("swr", Const.SWR);
    ADDITIONAL_SENDER.put("mdr", Const.MDR);
    ADDITIONAL_SENDER.put("ndr", Const.NDR);
    ADDITIONAL_SENDER.put("wdr", Const.WDR);
    ADDITIONAL_SENDER.put("hr", Const.HR);
    ADDITIONAL_SENDER.put("br", Const.BR);
    ADDITIONAL_SENDER.put("radio_bremen", Const.RBTV);
    ADDITIONAL_SENDER.put("tagesschau24", Const.TAGESSCHAU24);
    ADDITIONAL_SENDER.put("das_erste", Const.ARD);
    ADDITIONAL_SENDER.put("one", Const.ONE); // ONE
    ADDITIONAL_SENDER.put("ard-alpha", Const.ARD_ALPHA); // ARD-alpha
    ADDITIONAL_SENDER.put("funk", "Funk.net"); // Funk.net
    ADDITIONAL_SENDER.put("sr", Const.SR);
    ADDITIONAL_SENDER.put("phoenix", Const.PHOENIX);
    ADDITIONAL_SENDER.put("ard", Const.ARD);
    //IGNORED_SENDER "zdf", "kika", "3sat", "arte"
  }

  private static Optional<JsonObject> getMediaCollectionObject(final JsonObject itemObject) {
    if (itemObject.has(ELEMENT_MEDIA_COLLECTION)
        && !itemObject.get(ELEMENT_MEDIA_COLLECTION).isJsonNull()
        && itemObject.getAsJsonObject(ELEMENT_MEDIA_COLLECTION).has(ELEMENT_EMBEDDED)
        && !itemObject.getAsJsonObject(ELEMENT_MEDIA_COLLECTION).get(ELEMENT_EMBEDDED)
        .isJsonNull()) {

      return Optional.of(itemObject.getAsJsonObject(ELEMENT_MEDIA_COLLECTION)
          .getAsJsonObject(ELEMENT_EMBEDDED));
    }

    return Optional.empty();
  }

  private static Optional<String> parseTopic(final JsonObject playerPageObject) {
    Optional<String> topic;
    if (playerPageObject.has(ELEMENT_SHOW) && !playerPageObject.get(ELEMENT_SHOW).isJsonNull()) {
      final JsonObject showObject = playerPageObject.get(ELEMENT_SHOW).getAsJsonObject();
      topic = JsonUtils.getAttributeAsString(showObject, ATTRIBUTE_TITLE);
    } else {
      // no show element found -> use title as topic
      topic = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
    }

    if (topic.isPresent()) {
      // remove time in topic
      if (topic.get().contains("MDR aktuell")) {
        return Optional.of(topic.get().replaceAll("[0-9][0-9]:[0-9][0-9] Uhr$", "").trim());
      }
    }

    return topic;
  }

  private Optional<String> parseTitle(final JsonObject playerPageObject) {
    Optional<String> title = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_TITLE);
    if (title.isPresent()) {
      String[] replaceWords = {" - Hörfassung", " (mit Gebärdensprache)", " mit Gebärdensprache"," (mit Audiodeskription)", "Audiodeskription", " - (Originalversion)", " (OV)"};
      String cleanTitle = title.get().trim();
      for (String replaceWord : replaceWords) {
        cleanTitle = cleanTitle.replace(replaceWord, "");
      }
      cleanTitle = cleanTitle.trim();
      return Optional.of(cleanTitle);
    }
    return title;
  }

  private static Optional<LocalDateTime> parseDate(final JsonObject playerPageObject) {
    final Optional<String> dateValue
        = JsonUtils.getAttributeAsString(playerPageObject, ATTRIBUTE_BROADCAST);
    if (dateValue.isPresent()) {
      try {
        final ZonedDateTime inputDateTime = ZonedDateTime.parse(dateValue.get());
        final LocalDateTime localDateTime
            = inputDateTime.withZoneSameInstant(ZoneId.of(GERMAN_TIME_ZONE)).toLocalDateTime();
        return Optional.of(localDateTime);
      } catch (final DateTimeParseException ex) {
        LOG.error("Error parsing date time value " + dateValue.get(), ex);
      }
    }

    return Optional.empty();
  }

  private static Optional<Duration> parseDuration(final JsonObject playerPageObject) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isPresent()) {
      final Optional<JsonElement> durationElement = JsonUtils.getElement(mediaCollectionObject.get(), ATTRIBUTE_DURATION);
      final Optional<JsonElement> durationElementSec = JsonUtils.getElement(mediaCollectionObject.get(), ATTRIBUTE_DURATION_SEC);
      if (durationElement.isPresent()) {
        return Optional.of(Duration.ofSeconds(durationElement.get().getAsLong()));
      } else if (durationElementSec.isPresent()) {
        return Optional.of(Duration.ofSeconds(durationElementSec.get().getAsLong()));
      }
    }
    return Optional.empty();
  }

  private Optional<String> prepareSubtitleUrl(final JsonElement embeddedElement) {
    Optional<JsonElement> subtitle = JsonUtils.getElement(embeddedElement, ELEMENT_SUBTITLES);
    if (subtitle.isEmpty() || !subtitle.get().isJsonArray() || (subtitle.get().getAsJsonArray().size() == 0))
      return Optional.empty();
    Optional<JsonElement> sources = JsonUtils.getElement(subtitle.get().getAsJsonArray().get(0), ELEMENT_SOURCES);
    if (sources.isEmpty() || !sources.get().isJsonArray())
      return Optional.empty();
    Set<String> urls = new HashSet<>();
    for (JsonElement url : sources.get().getAsJsonArray()) {
      JsonUtils.getElementValueAsString(url, ATTRIBUTE_URL).ifPresent(urls::add);
    }
    return urls.stream()
        .filter(s -> !s.endsWith(".vtt"))
        .findFirst();
  }

  @Override
  public List<ArdFilmDto> deserialize(
      final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) {

    List<ArdFilmDto> films = new ArrayList<>();

    if (!JsonUtils.hasElements(jsonElement, ELEMENT_WIDGETS)
        || !jsonElement.getAsJsonObject().get(ELEMENT_WIDGETS).isJsonArray()) {
      return films;
    }

    final JsonArray widgets = jsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_WIDGETS);
    if (widgets.size() == 0) {
      return films;
    }

    final JsonObject itemObject = widgets.get(0).getAsJsonObject();

    final Optional<String> topic = parseTopic(itemObject);
    final Optional<String> title = parseTitle(itemObject);
    final Optional<String> titleoriginal = JsonUtils.getAttributeAsString(itemObject, ATTRIBUTE_TITLE);
    final Optional<String> description = JsonUtils.getAttributeAsString(itemObject, ATTRIBUTE_SYNOPSIS);
    final Optional<LocalDateTime> date = parseDate(itemObject);
    final Optional<Duration> duration = parseDuration(itemObject);
    final Optional<String> partner = parsePartner(itemObject);
    Optional<Map<Qualities, String>> videoInfoStandard = parseVideoUrls(itemObject, MARKER_VIDEO_CATEGORY_MAIN, MARKER_VIDEO_STANDARD, MARKER_VIDEO_MP4, MARKER_VIDEO_DE);
    Optional<Map<Qualities, String>> videoInfoAdaptive = parseVideoUrls(itemObject, MARKER_VIDEO_CATEGORY_MAIN, MARKER_VIDEO_STANDARD, MARKER_VIDEO_CATEGORY_MPEG, MARKER_VIDEO_DE);
    Optional<Map<Qualities, String>> videoInfoAD = parseVideoUrls(itemObject, MARKER_VIDEO_CATEGORY_MAIN, MARKER_VIDEO_AD, MARKER_VIDEO_MP4, MARKER_VIDEO_DE);
    Optional<Map<Qualities, String>> videoInfoDGS = parseVideoUrls(itemObject, MARKER_VIDEO_DGS, MARKER_VIDEO_STANDARD, MARKER_VIDEO_MP4, MARKER_VIDEO_DE);
    Optional<Map<Qualities, String>> videoInfoOV = parseVideoUrls(itemObject, MARKER_VIDEO_CATEGORY_MAIN, MARKER_VIDEO_STANDARD, MARKER_VIDEO_MP4, MARKER_VIDEO_OV);
    Optional<String> subtitles = prepareSubtitleUrl(itemObject);
    
    if (topic.isEmpty() || title.isEmpty() || partner.isEmpty() || ADDITIONAL_SENDER.get(partner.get()) == null) {
      if (partner.isPresent() && ADDITIONAL_SENDER.get(partner.get()) == null) {
        LOG.warn("Missing Partner " + partner.get());
      }
      return films;
    }
    boolean subVideoPage = titleoriginal.get().contains(" - (Originalversion)") || titleoriginal.get().contains(" (OV)") 
        || titleoriginal.get().contains(" (mit Gebärdensprache)") || titleoriginal.get().contains(" mit Gebärdensprache")
        || titleoriginal.get().contains("- Hörfassung") || titleoriginal.get().contains("(mit Audiodeskription)");
    // mainly funk
    if (videoInfoStandard.isEmpty() && videoInfoAD.isEmpty() && videoInfoDGS.isEmpty() && videoInfoOV.isEmpty() && videoInfoAdaptive.isPresent()) {
      videoInfoStandard = resolveFallbackFromPlaylist(videoInfoAdaptive);
    }
    // incorrect langueage code for OV
    if ((titleoriginal.get().contains(" - (Originalversion)") || titleoriginal.get().contains(" (OV)")) && videoInfoOV.isEmpty()) {
      videoInfoOV = parseVideoUrls(itemObject, MARKER_VIDEO_CATEGORY_MAIN, MARKER_VIDEO_STANDARD, MARKER_VIDEO_MP4, "*");
    }
    // mainly tagesschau
    if ((titleoriginal.get().contains(" (mit Gebärdensprache)") || titleoriginal.get().contains(" mit Gebärdensprache")) && videoInfoStandard.isPresent() && videoInfoDGS.isEmpty()) {
      videoInfoDGS = videoInfoStandard;
      videoInfoStandard = Optional.empty();
    } 
    // in aller freundschaft
    if ((titleoriginal.get().contains("- Hörfassung") || titleoriginal.get().contains("(mit Audiodeskription)")) && videoInfoStandard.isPresent() && videoInfoAD.isEmpty()) {
      videoInfoAD = videoInfoStandard;
      videoInfoStandard = Optional.empty();
    }
    //
    if (!subVideoPage && videoInfoStandard.isPresent() && videoInfoStandard.get().size() > 0) {
      // add film standard
      final ArdFilmDto filmDto
          = new ArdFilmDto(
          createFilm(
              ADDITIONAL_SENDER.get(partner.get()),
              topic.get(),
              title.get(),
              description,
              date,
              duration,
              videoInfoStandard.get(),
              subtitles));
      films.add(filmDto);
      if (widgets.size() > 1) {
        parseRelatedFilms(filmDto, widgets.get(1).getAsJsonObject());
      }
    }
    //
    if (videoInfoOV.isPresent() && videoInfoOV.get().size() > 0) {
      Map<Qualities, String> vid =  videoInfoOV.get();
      final ArdFilmDto filmDto
      = new ArdFilmDto(
      createFilm(
          ADDITIONAL_SENDER.get(partner.get()),
          topic.get(),
          title.get() + " (Originalversion)",
          description,
          date,
          duration,
          vid,
          subtitles));
      films.add(filmDto);
    }
    //
    if (videoInfoAD.isPresent() && videoInfoAD.get().size() > 0) {
      // add film ad
      final ArdFilmDto filmDto
          = new ArdFilmDto(
          createFilm(
              ADDITIONAL_SENDER.get(partner.get()),
              topic.get(),
              title.get() + " (Audiodeskription)",
              description,
              date,
              duration,
              videoInfoAD.get(),
              subtitles));
      films.add(filmDto);
    }
    //
    if (videoInfoDGS.isPresent() && videoInfoDGS.get().size() > 0) {
      // add film dgs
      final ArdFilmDto filmDto
          = new ArdFilmDto(
          createFilm(
              ADDITIONAL_SENDER.get(partner.get()),
              topic.get(),
              title.get() + " (Gebärdensprache)",
              description,
              date,
              duration,
              videoInfoDGS.get(),
              subtitles));
      films.add(filmDto);
    }

    return films;
  }

  private Optional<String> parsePartner(JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_PUBLICATION_SERVICE)) {
      JsonObject publicationServiceObject
          = playerPageObject.get(ELEMENT_PUBLICATION_SERVICE).getAsJsonObject();
      Optional<String> partnerAttribute = JsonUtils
          .getAttributeAsString(publicationServiceObject, ATTRIBUTE_PARTNER);
      if (partnerAttribute.isPresent()) {
        return partnerAttribute;
      }

      Optional<String> nameAttribute = JsonUtils
          .getAttributeAsString(publicationServiceObject, ATTRIBUTE_NAME);
      if (nameAttribute.isPresent()) {
        return Optional.of(nameAttribute.get());
      }
    }

    return Optional.empty();
  }

  private void parseRelatedFilms(final ArdFilmDto filmDto, final JsonObject playerPageObject) {
    if (playerPageObject.has(ELEMENT_TEASERS)) {
      final JsonElement teasersElement = playerPageObject.get(ELEMENT_TEASERS);
      if (teasersElement.isJsonArray()) {
        for (final JsonElement teasersItemElement : teasersElement.getAsJsonArray()) {
          final JsonObject teasersItemObject = teasersItemElement.getAsJsonObject();
          final Optional<String> id
              = JsonUtils.getAttributeAsString(teasersItemObject, ATTRIBUTE_ID);
          if (id.isPresent()) {
            final String url = String.format(ArdConstants.ITEM_URL, id.get());
            filmDto.addRelatedFilm(new ArdFilmInfoDto(id.get(), url, 0));
          }
        }
      }
    }
  }

  private DatenFilm createFilm(
      final String sender,
      final String topic,
      final String title,
      final Optional<String> description,
      final Optional<LocalDateTime> date,
      final Optional<Duration> duration,
      final Map<Qualities, String> videoUrls,
      final Optional<String> sub) {

    LocalDateTime time = date.orElse(LocalDateTime.now());

    String dateValue = time.format(DATE_FORMAT);
    String timeValue = time.format(TIME_FORMAT);
    String baseUrl = videoUrls.get(Qualities.NORMAL);
    DatenFilm film = new DatenFilm(sender, topic, "", title, baseUrl, "",
        dateValue, timeValue, duration.orElse(Duration.ZERO).getSeconds(), description.orElse(""));
    if (videoUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(film, videoUrls.get(Qualities.SMALL));
    }
    if (videoUrls.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(film, videoUrls.get(Qualities.HD));
    }
    if (sub.isPresent()) {
      CrawlerTool.addUrlSubtitle(film, sub.get());
    }

    return film;
  }

  private Optional<Map<Qualities, String>> parseVideoUrls(final JsonObject playerPageObject, String streamType, String aduioType, String mimeType, String language) {
    Optional<Map<Integer, String>> urls = parseVideoUrlMap(playerPageObject, streamType, aduioType, mimeType, language);
    if (urls.isEmpty()) {
      return Optional.empty();
    }
    Map<Qualities, String> videoInfo = new EnumMap<>(Qualities.class);
    for (Map.Entry<Integer, String> entry : urls.get().entrySet()) {
      Qualities resolution = Qualities.getResolutionFromWidth(entry.getKey());
      if(!videoInfo.containsKey(resolution)) {
        videoInfo.put(resolution, entry.getValue());
      }
    }
    // issue if we do not have normal res
    if (!videoInfo.containsKey(Qualities.NORMAL)) {
      if (videoInfo.containsKey(Qualities.HD)) {
        videoInfo.put(Qualities.NORMAL, videoInfo.get(Qualities.HD));
        videoInfo.remove(Qualities.HD);
      } else {
        videoInfo.put(Qualities.NORMAL, videoInfo.get(Qualities.SMALL));
        videoInfo.remove(Qualities.SMALL);
      }
    }
    return Optional.of(videoInfo);
  }

  private Optional<Map<Integer, String>> parseVideoUrlMap(final JsonObject playerPageObject, String streamType, String aduioType, String mimeType, String language) {
    final Optional<JsonObject> mediaCollectionObject = getMediaCollectionObject(playerPageObject);
    if (mediaCollectionObject.isEmpty())
      return Optional.empty();
    final Optional<JsonElement> streams = JsonUtils.getElement(mediaCollectionObject.get(), ELEMENT_STREAMS);
    if (streams.isEmpty() || !streams.get().isJsonArray() || (streams.get().getAsJsonArray().size() == 0))
      return Optional.empty();
    //
    Map<Integer, String> videoInfo = new TreeMap<>(Comparator.reverseOrder());
    for (JsonElement streamsCategory : streams.get().getAsJsonArray()) {
      final Optional<String> streamKind = JsonUtils.getElementValueAsString(streamsCategory, ATTRIBUTE_KIND);
      final Optional<JsonElement> media = JsonUtils.getElement(streamsCategory, ELEMENT_MEDIA);
      if (media.isEmpty() || !media.get().isJsonArray() || (media.get().getAsJsonArray().size() == 0))
        return Optional.empty();
      if (streamKind.orElse("").equalsIgnoreCase(streamType)) {
        for (JsonElement video : media.get().getAsJsonArray()) {
          Optional<String> mime = JsonUtils.getElementValueAsString(video, ATTRIBUTE_MIME);
          if (mime.isPresent() && mime.get().equalsIgnoreCase(mimeType)) {
            Optional<JsonElement> audios = JsonUtils.getElement(video, ELEMENT_AUDIO);
            if (audios.isPresent() && audios.get().isJsonArray() && audios.get().getAsJsonArray().size() > 0) {
              Optional<String> kind = JsonUtils.getElementValueAsString(audios.get().getAsJsonArray().get(0), ATTRIBUTE_KIND);
              Optional<String> resh = JsonUtils.getElementValueAsString(video, ATTRIBUTE_RESOLUTION_H);
              Optional<String> url = JsonUtils.getElementValueAsString(video, ATTRIBUTE_URL);
              Optional<String> languageCode = JsonUtils.getElementValueAsString(audios.get().getAsJsonArray().get(0), ATTRIBUTE_ADUIO_LANG);
              if (url.isPresent() && resh.isPresent() && kind.isPresent() && kind.get().equalsIgnoreCase(aduioType) && 
                  (languageCode.orElse("").equalsIgnoreCase(language) || (language.equalsIgnoreCase("*") && !languageCode.orElse("").equalsIgnoreCase("deu")))) {
                videoInfo.put(Integer.parseInt(resh.get()), UrlUtils.removeParameters(url.get()));
              }
            }
          }
        }
      }
    }
    if (videoInfo.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(videoInfo);
  }
  
  private Optional<Map<Qualities, String>> resolveFallbackFromPlaylist(Optional<Map<Qualities, String>> videoInfoAdaptive) {
     Map<Qualities, URL> qualitiesUrls = videoInfoAdaptive.get().entrySet().stream()
         .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
             try {
                 return new URL(entry.getValue());
             } catch (MalformedURLException e) {
                 LOG.error("failed converting string {} to url", entry.getValue(), e);
                 return null;
             }
         }));
     if (!qualitiesUrls.containsKey(Qualities.NORMAL)) {
       qualitiesUrls.put(Qualities.NORMAL,  qualitiesUrls.entrySet().stream().findFirst().get().getValue());
     }
     //
     ArdVideoInfoJsonDeserializer.loadM3U8(qualitiesUrls);
     //
     Map<Qualities, String> fallback = qualitiesUrls.entrySet().stream()
     .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
     //
     if (!fallback.containsKey(Qualities.NORMAL) && !fallback.isEmpty()) {
       fallback.put(Qualities.NORMAL, fallback.entrySet().stream().findFirst().get().getValue());
     }
     
     return Optional.of(fallback);
   }
}
