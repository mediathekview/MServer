package de.mediathekview.mserver.crawler.br.json;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.BrCrawler;

import mServer.crawler.CrawlerTool;

public class BrFilmDeserializer implements JsonDeserializer<Optional<Film>> {
  private static final String ERROR_NO_START_TEMPLATE =
      "The BR film \"%s - %s\" has no broadcast start so it will using the actual date and time.";
  private static final String ERROR_WEBSITE_URL =
      "The Website Url \"%s\" can't be converted to a java URL Obj.";
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
  private static final String ERROR_VIDEO_URL = "A video url can't be converted to a Java URL.";

  private static final ZoneId ZONE_ID = ZoneId.of( "Europe/Berlin" );

  private final AbstractCrawler crawler;
  private final String filmId;

  public BrFilmDeserializer(final AbstractCrawler aCrawler, final String aFilmId) {
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
  public Optional<Film> deserialize(final JsonElement aElement, final Type aType,
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
    }

    return Optional.empty();
  }

  private void addDescriptions(final Optional<Film> aNewFilm, final JsonObject aDetailClip) {
    if (aNewFilm.isPresent()) {
      if (aDetailClip.has(JSON_ELEMENT_DESCRIPTION)
          && !aDetailClip.get(JSON_ELEMENT_DESCRIPTION).isJsonNull()) {
        aNewFilm.get().setBeschreibung(aDetailClip.get(JSON_ELEMENT_DESCRIPTION).getAsString());
      } else if (aDetailClip.has(JSON_ELEMENT_SHORT_DESCRIPTION)
          && !aDetailClip.get(JSON_ELEMENT_SHORT_DESCRIPTION).isJsonNull()) {
        aNewFilm.get()
            .setBeschreibung(aDetailClip.get(JSON_ELEMENT_SHORT_DESCRIPTION).getAsString());
      }
    }
  }

  private boolean addUrls(final Optional<Film> aNewFilm, final JsonObject viewer) {
    final Set<BrUrlDTO> urls = edgesToUrls(viewer);
    boolean hasURLs = false;
    if (aNewFilm.isPresent() && !urls.isEmpty()) {
      // Sorts the urls by width descending
      final List<BrUrlDTO> bestUrls =
          urls.stream().sorted(Comparator.comparingInt(BrUrlDTO::getWidth).reversed())
              .collect(Collectors.toList());

      for (int id = 0; id < bestUrls.size(); id++) {
        final Resolution resolution =
            Resolution.getResolutionFromArdAudioVideoOrdinalsByProfileName(
                bestUrls.get(id).getVideoProfile());
        try {
          if (!aNewFilm.get().getUrls().containsKey(resolution)) {
            aNewFilm.get().addUrl(resolution,
                CrawlerTool.uriToFilmUrl(new URL(bestUrls.get(id).getUrl())));
            hasURLs = true;
          }
        } catch (final MalformedURLException malformedURLException) {
          LOG.fatal(ERROR_VIDEO_URL, malformedURLException);
          crawler.printMessage(ServerMessages.DEBUG_INVALID_URL, crawler.getSender().getName(),
              bestUrls.get(id).getUrl());
        }
      }
    }
    return hasURLs;

  }

  private Optional<Film> buildFilm(final Optional<JsonObject> detailClip, final JsonObject viewer) {
    final Optional<Film> newFilm;
    if (detailClip.isPresent()) {
      newFilm = createFilm(detailClip.get());
      addDescriptions(newFilm, detailClip.get());

      final Optional<String> subtitleUrl = getSubtitleUrl(viewer);
      if (newFilm.isPresent() && subtitleUrl.isPresent()) {
        try {
          newFilm.get().addSubtitle(new URL(subtitleUrl.get()));
        } catch (final MalformedURLException malformedURLException) {
          LOG.fatal(ERROR_VIDEO_URL, malformedURLException);
          crawler.printMessage(ServerMessages.DEBUG_INVALID_URL, crawler.getSender().getName(),
              subtitleUrl.get());
        }
      }
      if (addUrls(newFilm, viewer)) {
        return newFilm;
      } else {
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
      // TODO GEO locations
    } else {
      printMissingDetails(JSON_ELEMENT_DETAIL_CLIP);
    }
    return Optional.empty();
  }

  private Optional<Film> createFilm(final JsonObject aDetailClip) {
    final Optional<JsonElement> start = getBroadcastStart(aDetailClip);
    if (aDetailClip.has(JSON_ELEMENT_TITLE) && aDetailClip.has(JSON_ELEMENT_KICKER)
        && aDetailClip.has(JSON_ELEMENT_DURATION)) {
      final String title = aDetailClip.get(JSON_ELEMENT_TITLE).getAsString();
      final String thema = getTheme(aDetailClip);

      final LocalDateTime time;
      if (start.isPresent()) {
        time = toTime(start.get().getAsString());
      } else {
        time = LocalDateTime.now();
        LOG.debug(String.format(ERROR_NO_START_TEMPLATE, thema, title));
      }
      final Duration duration = toDuration(aDetailClip.get(JSON_ELEMENT_DURATION).getAsLong());

      final String website = String.format(FILM_WEBSITE_TEMPLATE, BrCrawler.BASE_URL, filmId);
      try {
        final Film film =
            new Film(UUID.randomUUID(), crawler.getSender(), title, thema, time, duration);
        film.setWebsite(new URL(website));
        return Optional.of(film);

      } catch (final MalformedURLException malformedURLException) {
        LOG.fatal(String.format(ERROR_WEBSITE_URL, website), malformedURLException);
        crawler.incrementAndGetErrorCount();
        crawler.printErrorMessage();
      }
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

  private Optional<String> getSubtitleUrl(final JsonObject viewer) {
    if (viewer.has(JSON_ELEMENT_CLIP)) {
      final JsonObject clip = viewer.getAsJsonObject(JSON_ELEMENT_CLIP);
      if (clip.has(JSON_ELEMENT_CAPTION_FILES)) {
        final JsonObject captionFiles = clip.getAsJsonObject(JSON_ELEMENT_CAPTION_FILES);
        if (captionFiles.has(JSON_ELEMENT_EDGES)) {
          final JsonArray edges = captionFiles.getAsJsonArray(JSON_ELEMENT_EDGES);
          if (edges.size() > 0) {
            for (final JsonElement edge : edges) {
              if (edge.getAsJsonObject().has(JSON_ELEMENT_NODE)) {
                final JsonObject node = edge.getAsJsonObject().getAsJsonObject(JSON_ELEMENT_NODE);
                if (node.has(JSON_ELEMENT_PUBLIC_LOCATION)) {
                  return Optional.of(node.get(JSON_ELEMENT_PUBLIC_LOCATION).getAsString());
                }
              }
            }
          }
        }
      }
    }
    return Optional.empty();
  }

  private String getTheme(final JsonObject aDetailClip) {

    String theme = "";

    if (aDetailClip.has(JSON_ELEMENT_EPISODEOF)) {
      final JsonElement element = aDetailClip.get(JSON_ELEMENT_EPISODEOF);
      if (!element.isJsonNull()) {
        final JsonObject episodeOf = aDetailClip.getAsJsonObject(JSON_ELEMENT_EPISODEOF);
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
    crawler.printMissingElementErrorMessage(aMissingJsonElement);
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
