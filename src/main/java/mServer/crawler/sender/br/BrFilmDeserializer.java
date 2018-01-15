package mServer.crawler.sender.br;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.DatenFilm;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import static mServer.crawler.sender.MediathekBr.SENDERNAME;
import mServer.crawler.sender.MediathekReader;

public class BrFilmDeserializer implements JsonDeserializer<Optional<DatenFilm>> {
  private static final String ERROR_NO_START_TEMPLATE =
      "The BR film \"%s - %s\" has no broadcast start so it will using the actual date and time.";
  private static final String HD = "HD";
  private static final String FILM_WEBSITE_TEMPLATE = "%s/video/%s";
  private static final String ERROR_MISSING_DETAIL_TEMPLATE =
      "A BR film can't be created because of missing details. The JSON element \"%s\" is missing.";

  private static final Logger LOG = LogManager.getLogger(BrFilmDeserializer.class);

  private static final String JSON_ELEMENT_DATA = "data";
  private static final String JSON_ELEMENT_VIEWER = "viewer";
  private static final String JSON_ELEMENT_CLIP = "clip";
  private static final String JSON_ELEMENT_VIDEO_FILES = "videoFiles";
  private static final String JSON_ELEMENT_EDGES = "edges";
  private static final String JSON_ELEMENT_NODE = "node";
  private static final String JSON_ELEMENT_ID = "id";
  private static final String JSON_ELEMENT_CAPTION_FILES = "captionFiles";
  private static final String JSON_ELEMENT_EPISODEOF = "episodeOf";

  private static final String JSON_ELEMENT_DETAIL_CLIP = "detailClip";
  private static final String JSON_ELEMENT_TITLE = "title";
  private static final String JSON_ELEMENT_KICKER = "kicker";
  private static final String JSON_ELEMENT_DURATION = "duration";
  private static final String JSON_ELEMENT_BROADCASTS = "broadcasts";
  private static final String JSON_ELEMENT_START = "start";
  private static final String JSON_ELEMENT_SHORT_DESCRIPTION = "shortDescription";
  private static final String JSON_ELEMENT_DESCRIPTION = "description";
  private static final String JSON_ELEMENT_PUBLIC_LOCATION = "publicLocation";

  private static final String JSON_ELEMENT_VIDEO_PROFILE = "videoProfile";
  private static final String JSON_ELEMENT_WIDTH = "width";

  private static final ZoneId ZONE_ID = ZoneId.of( "Europe/Berlin" );

  private final MediathekReader crawler;
  private final String filmId;
  
  private final DateTimeFormatter dateFormatDatenFilm = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private final DateTimeFormatter timeFormatDatenFilm = DateTimeFormatter.ofPattern("HH:mm:ss");

  public BrFilmDeserializer(final MediathekReader aCrawler, final String aFilmId) {
    crawler = aCrawler;
    filmId = aFilmId;
  }

  /**
   * Resolves the Film details which and creates a Film of it.<br>
   * The data has this structure:
   * <code>data -> viewer -> clip -> videoFiles -> edges[] -> node -> id</code><br>
   * <code>data -> viewer -> detailClip -> title</code><br>
   * <code>data -> viewer -> detailClip -> kicker</code><br>
   * <code>data -> viewer -> detailClip -> duration</code><br>
   * <code>data -> viewer -> detailClip -> broadcasts -> edges[0] -> node -> start</code><br>
   * Optional: <code>data -> viewer -> detailClip -> shortDescription</code><br>
   * Optional: <code>data -> viewer -> detailClip -> description</code>
   */
  @Override
  public Optional<DatenFilm> deserialize(final JsonElement aElement, final Type aType,
      final JsonDeserializationContext aContext) {
    try {
      final Optional<JsonObject> viewer = getViewer(aElement.getAsJsonObject());
      if (viewer.isPresent()) {

        final Optional<JsonObject> detailClip = getDetailClip(viewer.get());

        return buildFilm(detailClip, viewer.get());


      } else {
        printMissingDetails(JSON_ELEMENT_VIEWER);
      }
    } catch (final UnsupportedOperationException unsupportedOperationException) {
      // This will happen when a element is JsonNull.
      LOG.error("BR: A needed JSON element is JsonNull.", unsupportedOperationException);
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLER);
    }

    return Optional.empty();
  }

  private String getDescriptions(final JsonObject aDetailClip) {
    String description = "";
    
    if (aDetailClip.has(JSON_ELEMENT_DESCRIPTION)
        && !aDetailClip.get(JSON_ELEMENT_DESCRIPTION).isJsonNull()) {
      description = aDetailClip.get(JSON_ELEMENT_DESCRIPTION).getAsString();
    } else if (aDetailClip.has(JSON_ELEMENT_SHORT_DESCRIPTION)
        && !aDetailClip.get(JSON_ELEMENT_SHORT_DESCRIPTION).isJsonNull()) {
      description = aDetailClip.get(JSON_ELEMENT_SHORT_DESCRIPTION).getAsString();
    }
    
    return description;
  }

  private Map<Resolution, String> getUrls(final JsonObject viewer) {
    Map<Resolution, String> urlMap = new HashMap<>();
    
    final Set<BrUrlDTO> urls = edgesToUrls(viewer);
    if (!urls.isEmpty()) {
      // Sorts the urls by width descending, then it limits the amount to three to get the three
      // best.
      final List<BrUrlDTO> bestUrls =
          urls.stream().sorted(Comparator.comparingInt(BrUrlDTO::getWidth).reversed()).limit(3)
              .collect(Collectors.toList());

      for (int id = 0; id < bestUrls.size(); id++) {
        final Resolution resolution = Resolution.getResolutionFromArdAudioVideoOrdinalsByProfileName(bestUrls.get(id).getVideoProfile());
        final String url = bestUrls.get(id).getUrl();

        if (url != null && !url.isEmpty() && !urlMap.containsKey(resolution)) {
          urlMap.put(resolution, url);
        }
      }
    }
    
    return urlMap;
  }

  private Optional<DatenFilm> buildFilm(final Optional<JsonObject> detailClip, final JsonObject viewer) {
    final Optional<DatenFilm> newFilm;
    if (detailClip.isPresent()) {
      String description = getDescriptions(detailClip.get());
      Map<Resolution, String> urls = getUrls(viewer);
      
      if(urls.containsKey(Resolution.NORMAL) && MediathekReader.urlExists(urls.get(Resolution.NORMAL))) {
        Optional<String> subTitle = getSubtitleUrl(viewer);
        newFilm = createFilm(detailClip.get(), description, subTitle, urls);
        return newFilm;
      }
    } else {
      printMissingDetails(JSON_ELEMENT_DETAIL_CLIP);
    }
    return Optional.empty();
  }

  private Optional<String> getSubtitleUrl(JsonObject viewer) {
    String subtitle = "";

    if(viewer.has(JSON_ELEMENT_CLIP)) {
      JsonObject clip = viewer.getAsJsonObject(JSON_ELEMENT_CLIP);
      if(clip.has(JSON_ELEMENT_CAPTION_FILES)) {
        JsonObject captionFiles = clip.getAsJsonObject(JSON_ELEMENT_CAPTION_FILES);
        if(captionFiles.has(JSON_ELEMENT_EDGES)) {
          JsonArray edges = captionFiles.getAsJsonArray(JSON_ELEMENT_EDGES);
          if (edges.size() > 0) {
            
            for (JsonElement edge : edges) {
              if(edge.getAsJsonObject().has(JSON_ELEMENT_NODE)) {
                JsonObject node = edge.getAsJsonObject().getAsJsonObject(JSON_ELEMENT_NODE);
                if(node.has(JSON_ELEMENT_PUBLIC_LOCATION)) {
                  String value = node.get(JSON_ELEMENT_PUBLIC_LOCATION).getAsString();
                  if (subtitle.isEmpty()) {
                    subtitle = value;
                  } else {
                    // ttml anderen Formaten vorziehen
                    if (value.endsWith(".ttml")) {
                      subtitle = value;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    
    if (!subtitle.isEmpty()) {
      return Optional.of(subtitle);
    }
    
    return Optional.empty();
  }

  private String getTheme(final JsonObject aDetailClip) {
    
    String theme = "";
    
    if (aDetailClip.has(JSON_ELEMENT_EPISODEOF)) {
      JsonElement element = aDetailClip.get(JSON_ELEMENT_EPISODEOF);
      if (!element.isJsonNull()) {
        JsonObject episodeOf = aDetailClip.getAsJsonObject(JSON_ELEMENT_EPISODEOF);
        if (episodeOf.has(JSON_ELEMENT_TITLE)) {
          theme = episodeOf.get(JSON_ELEMENT_TITLE).getAsString();
        }
      }
    }
    
    if (theme.isEmpty()) {
      theme = aDetailClip.get(JSON_ELEMENT_KICKER).getAsString();
    }
    
    return theme;
  }
  
  private Optional<DatenFilm> createFilm(final JsonObject aDetailClip, String aDescription, Optional<String> aSubTitle, Map<Resolution, String> aUrls) {
    final Optional<JsonElement> start = getBroadcastStart(aDetailClip);
    if (aDetailClip.has(JSON_ELEMENT_TITLE) && aDetailClip.has(JSON_ELEMENT_KICKER)
        && aDetailClip.has(JSON_ELEMENT_DURATION)) {
      final String title = aDetailClip.get(JSON_ELEMENT_TITLE).getAsString();
      final String thema = getTheme(aDetailClip);

      final String dateValue;
      final String timeValue;
      if (start.isPresent()) {
        final LocalDateTime time = toTime(start.get().getAsString());
        dateValue = time.format(dateFormatDatenFilm);
        timeValue = time.format(timeFormatDatenFilm);
      } else {
        LOG.debug(String.format(ERROR_NO_START_TEMPLATE, thema, title));
        dateValue = "";
        timeValue = "";
      }

      final Duration duration = toDuration(aDetailClip.get(JSON_ELEMENT_DURATION).getAsLong());

      final String website = String.format(FILM_WEBSITE_TEMPLATE, BrCrawler.BASE_URL, filmId);
      DatenFilm film = new DatenFilm(SENDERNAME, thema, website, title, aUrls.get(Resolution.NORMAL),"",
              dateValue, timeValue, duration.getSeconds(), aDescription);
              
      if (aUrls.containsKey(Resolution.SMALL)) {
          CrawlerTool.addUrlKlein(film, aUrls.get(Resolution.SMALL), "");
      }
      if (aUrls.containsKey(Resolution.HD)) {
          CrawlerTool.addUrlHd(film, aUrls.get(Resolution.HD), "");
      }
      if (aSubTitle.isPresent()) {
        CrawlerTool.addUrlSubtitle(film, aSubTitle.get());
      }
              
      return Optional.of(film);
    } else {
      if (!aDetailClip.has(JSON_ELEMENT_TITLE)) {
        printMissingDetails(JSON_ELEMENT_TITLE);
      }

      if (!aDetailClip.has(JSON_ELEMENT_KICKER)) {
        printMissingDetails(JSON_ELEMENT_KICKER);
      }

      if (!aDetailClip.has(JSON_ELEMENT_DURATION)) {
        printMissingDetails(JSON_ELEMENT_DURATION);
      }
    }
    return Optional.empty();
  }

  private Set<BrUrlDTO> edgesToUrls(final JsonObject viewer) {
    final Set<BrUrlDTO> urls = new HashSet<>();
    final Optional<JsonArray> edges = getVideoFileEdges(viewer);
    if (edges.isPresent()) {
      for (final JsonElement edge : edges.get()) {
        final JsonObject ebdgeObj = edge.getAsJsonObject();
        if (ebdgeObj.has(JSON_ELEMENT_NODE)) {
          final JsonObject node = ebdgeObj.getAsJsonObject(JSON_ELEMENT_NODE);
          final Optional<BrUrlDTO> url = nodeToUrl(node);
          if (url.isPresent()) {
            urls.add(url.get());
          }
        }
      }
    }
    return urls;
  }

  private Optional<JsonElement> getBroadcastStart(final JsonObject aDetailClip) {
    if (!aDetailClip.has(JSON_ELEMENT_BROADCASTS)) {
      return Optional.empty();
    }

    final JsonObject broadcast = aDetailClip.getAsJsonObject(JSON_ELEMENT_BROADCASTS);
    if (!broadcast.has(JSON_ELEMENT_EDGES)) {
      return Optional.empty();
    }

    final JsonArray edges = broadcast.getAsJsonArray(JSON_ELEMENT_EDGES);
    if (edges.size() <= 0) {
      return Optional.empty();
    }
    
    final JsonObject arrayItem = edges.get(0).getAsJsonObject();
    if (!arrayItem.has(JSON_ELEMENT_NODE)) {
      return Optional.empty();
    }
    
    final JsonObject node = arrayItem.getAsJsonObject(JSON_ELEMENT_NODE);
    if (!node.has(JSON_ELEMENT_START)) {
      return Optional.empty();
    }

    return Optional.of(node.get(JSON_ELEMENT_START));
  }

  private Optional<JsonObject> getDetailClip(final JsonObject aViewer) {
    if (!aViewer.has(JSON_ELEMENT_DETAIL_CLIP)) {
      return Optional.empty();
    }

    return Optional.of(aViewer.getAsJsonObject(JSON_ELEMENT_DETAIL_CLIP));
  }

  private Optional<JsonArray> getVideoFileEdges(final JsonObject aViewer) {
    if (!aViewer.has(JSON_ELEMENT_CLIP)) {
      return Optional.empty();
    }

    final JsonObject clip = aViewer.getAsJsonObject(JSON_ELEMENT_CLIP);
    if (!clip.has(JSON_ELEMENT_VIDEO_FILES)) {
      return Optional.empty();
    }

    final JsonObject videoFiles = clip.getAsJsonObject(JSON_ELEMENT_VIDEO_FILES);
    if (!videoFiles.has(JSON_ELEMENT_EDGES)) {
      return Optional.empty();
    }

    return Optional.of(videoFiles.getAsJsonArray(JSON_ELEMENT_EDGES));
  }

  private Optional<JsonObject> getViewer(final JsonObject aBaseObject) {
    if (!aBaseObject.has(JSON_ELEMENT_DATA)) {
      return Optional.empty();
    }

    final JsonObject data = aBaseObject.getAsJsonObject(JSON_ELEMENT_DATA);
    if (!data.has(JSON_ELEMENT_VIEWER)) {
      return Optional.empty();
    }

    return Optional.of(data.getAsJsonObject(JSON_ELEMENT_VIEWER));
  }

  private Optional<BrUrlDTO> nodeToUrl(final JsonObject aNode) {
    if (aNode.has(JSON_ELEMENT_PUBLIC_LOCATION)) {
      if (aNode.has(JSON_ELEMENT_VIDEO_PROFILE)) {
        final JsonObject videoProfile = aNode.getAsJsonObject(JSON_ELEMENT_VIDEO_PROFILE);
        if (videoProfile.has(JSON_ELEMENT_ID)) {
          if (videoProfile.has(JSON_ELEMENT_WIDTH)) {
            if (!videoProfile.get(JSON_ELEMENT_WIDTH).isJsonNull()
                && !videoProfile.get(JSON_ELEMENT_ID).isJsonNull()) {
              return Optional.of(new BrUrlDTO(aNode.get(JSON_ELEMENT_PUBLIC_LOCATION).getAsString(),
                  videoProfile.get(JSON_ELEMENT_WIDTH).getAsInt(),
                  videoProfile.get(JSON_ELEMENT_ID).getAsString()));
            }

          } else {
            printMissingDetails(JSON_ELEMENT_VIDEO_PROFILE + " -> " + JSON_ELEMENT_WIDTH);
          }

        } else {
          printMissingDetails(JSON_ELEMENT_VIDEO_PROFILE + " -> " + JSON_ELEMENT_ID);
        }

      } else {
        printMissingDetails(JSON_ELEMENT_VIDEO_PROFILE);
      }

    } else {
      printMissingDetails(JSON_ELEMENT_PUBLIC_LOCATION);
    }
    return Optional.empty();
  }

  private void printMissingDetails(final String aMissingJsonElement) {
    LOG.error(String.format(ERROR_MISSING_DETAIL_TEMPLATE, aMissingJsonElement));
  }

  private Duration toDuration(final long aSeconds) {
    return Duration.of(aSeconds, ChronoUnit.SECONDS);
  }

  private LocalDateTime toTime(final String aStart) {
    LocalDateTime local = LocalDateTime.parse(aStart, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    ZonedDateTime zoned = local.atZone(ZONE_ID);
    int hoursToAdd = zoned.getOffset().getTotalSeconds()/3600;
    return local.plusHours(hoursToAdd);
  }

}
