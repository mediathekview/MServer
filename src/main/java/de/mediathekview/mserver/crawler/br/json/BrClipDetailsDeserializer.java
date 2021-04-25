package de.mediathekview.mserver.crawler.br.json;

import com.google.gson.*;
import de.mediathekview.mlib.daten.*;
import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLElementNames;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLNodeNames;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.graphql.GsonGraphQLHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BrClipDetailsDeserializer implements JsonDeserializer<Optional<Film>> {

  private static final String DEFAULT_BR_VIDEO_URL_PRAEFIX = "https://www.br.de/mediathek/video/";
  private static final Logger LOG = LogManager.getLogger(BrClipDetailsDeserializer.class);
  private static final String JSON_ELEMENT_INITIAL_SCREENING = "initialScreening";

  private final BrID id;
  private final Sender sender;

  public BrClipDetailsDeserializer(final Sender sender, final BrID id) {
    super();
    this.sender = sender;
    this.id = id;
  }

  @Override
  public Optional<Film> deserialize(
      final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {

    final JsonObject rootObject = json.getAsJsonObject();

    if (GsonGraphQLHelper.checkForErrors(rootObject)) {
      throw new IllegalStateException(
          "Fehler beim auflösen des aktuellen Films mit ID: " + id.getId());
    }

    final Optional<JsonObject> clipDetails = getClipDetailsNode(rootObject);
    if (clipDetails.isPresent()) {
      final JsonObject clipDetailRoot = clipDetails.get();

      if (id.getType() == null) {
        String type =
            clipDetailRoot
                .getAsJsonPrimitive(BrGraphQLElementNames.GRAPHQL_TYPE_ELEMENT.getName())
                .getAsString();
        this.id.setType(BrClipType.getInstanceByName(type));
      }
      // Done
      final Optional<String> titel = getTitel(clipDetailRoot);
      final Optional<String> thema = getThema(clipDetailRoot);
      final Optional<LocalDateTime> sendeZeitpunkt = getSendeZeitpunkt(clipDetailRoot);
      final Optional<Duration> clipLaenge = getClipLaenge(clipDetailRoot);

      final Optional<Set<URL>> subtitles = getSubtitles(clipDetailRoot);
      final Optional<Map<Resolution, FilmUrl>> videoUrls = getVideos(clipDetailRoot);
      final Optional<String> beschreibung = getBeschreibung(clipDetailRoot);
      final Optional<URL> webSite = getWebSite(clipDetailRoot);

      if (videoUrls.isPresent()
          && titel.isPresent()
          && thema.isPresent()
          && clipLaenge.isPresent()) {
        final Film currentFilm =
            new Film(
                UUID.randomUUID(),
                sender,
                titel.get(),
                thema.get(),
                sendeZeitpunkt.orElse(null),
                clipLaenge.get());

        videoUrls.ifPresent(currentFilm::addAllUrls);

        currentFilm.setGeoLocations(getGeoLocations(currentFilm.getUrl(Resolution.NORMAL)));
        beschreibung.ifPresent(currentFilm::setBeschreibung);

        currentFilm.setWebsite(webSite.orElse(null));

        subtitles.ifPresent(currentFilm::addAllSubtitleUrls);
        return Optional.of(currentFilm);
      } else {
        LOG.error(
            "Kein komplett gültiger Film: {} Titel da? {} Thema da? {} Länge da? {} Video da? {}",
            id.getId(),
            titel.isPresent(),
            thema.isPresent(),
            clipLaenge.isPresent(),
            videoUrls.isPresent());
      }
    }
    return Optional.empty();
  }

  private Collection<GeoLocations> getGeoLocations(final FilmUrl videoUrls) {
    Set<GeoLocations> geoLocations = new HashSet<>();

    if (videoUrls.getUrl().toString().contains("/geo/")) {
      geoLocations.add(GeoLocations.GEO_DE);
    } else {
      geoLocations.add(GeoLocations.GEO_NONE);
    }

    return geoLocations;
  }

  private Optional<JsonObject> getClipDetailsNode(final JsonObject rootObject) {
    final Optional<JsonObject> dataNodeOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            rootObject, BrGraphQLNodeNames.RESULT_ROOT_NODE.getName());
    if (dataNodeOptional.isEmpty()) {
      return Optional.empty();
    }

    return GsonGraphQLHelper.getChildObjectIfExists(
            dataNodeOptional.get(), BrGraphQLNodeNames.RESULT_ROOT_BR_NODE.getName())
        .flatMap(
            viewerNode ->
                GsonGraphQLHelper.getChildObjectIfExists(
                    viewerNode, BrGraphQLNodeNames.RESULT_CLIP_DETAILS_ROOT.getName()));
  }

  private Optional<String> getTitel(final JsonObject clipDetailRoot) {
    return getJsonPrimitiveTextFromChildPrimitiveIfExists(
        clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_TITLE);
  }

  private Optional<String> getJsonPrimitiveTextFromChildPrimitiveIfExists(
      final JsonObject clipDetailRoot, BrGraphQLElementNames brGraphQLElementName) {
    final Optional<JsonPrimitive> element =
        GsonGraphQLHelper.getChildPrimitiveIfExists(clipDetailRoot, brGraphQLElementName.getName());
    return element.map(JsonPrimitive::getAsString);
  }

  private Optional<String> buildThemaFromKicker(JsonObject clipDetailRoot) {
    return getJsonPrimitiveTextFromChildPrimitiveIfExists(
        clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_KICKER);
  }

  private Optional<String> getTitleFromElement(final JsonObject node) {
    final Optional<JsonPrimitive> itemOfTitleElementOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            node, BrGraphQLElementNames.STRING_CLIP_TITLE.getName());
    if (itemOfTitleElementOptional.isPresent()) {

      final JsonPrimitive itemOfTitleElement = itemOfTitleElementOptional.get();

      return Optional.of(itemOfTitleElement.getAsString());
    }
    return Optional.empty();
  }

  private Optional<String> getThema(final JsonObject clipDetailRoot) {

    /*
     * Ist der aktuelle Titel ein Programm wird versucht zu prüfen, ob der aktuelle Titel
     * Teil einer Serie ist und wenn das so ist, den entsprechenden Titel als Thema zurück
     * zu geben.
     */
    final Optional<JsonObject> node;
    switch (id.getType()) {
      case PROGRAMME:
        node = getEpisodeOfNode(clipDetailRoot);
        break;
      case ITEM:
        node = getItemOfNode(clipDetailRoot);
        break;
      default:
        node = Optional.empty();
        break;
    }

    if (node.isPresent()) {
      return getTitleFromElement(node.get());
    }

    /*
     * Wenn wir hier ankommen ist weder episodeOf noch itemOf gefüllt. Dann nehmen wir halt den kicker auch wenn der nicht
     * so gut ist ein Thema zu bilden. Aber besser wie gar nichts.
     */
    return buildThemaFromKicker(clipDetailRoot);
  }

  private Optional<JsonObject> getEpisodeOfNode(final JsonObject clipDetailRoot) {
    final Optional<JsonObject> episodeOfNodeOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            clipDetailRoot, BrGraphQLNodeNames.RESULT_CLIP_EPISONEOF.getName());
    if (episodeOfNodeOptional.isEmpty()) {
      return Optional.empty();
    }
    final JsonObject episodeOfNode = episodeOfNodeOptional.get();

    if (episodeOfNode.entrySet().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(episodeOfNode);
  }

  private Optional<JsonObject> getItemOfNode(final JsonObject clipDetailRoot) {

    final Optional<JsonObject> itemOfRootNodeOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            clipDetailRoot, BrGraphQLNodeNames.RESULT_CLIP_ITEMOF.getName());
    return getFirstNode(itemOfRootNodeOptional);
  }

  @NotNull
  private Optional<JsonObject> getFirstNode(final Optional<JsonObject> itemOfRootNodeOptional) {
    final JsonArray itemOfEdgesNode = getEdgeNodes(itemOfRootNodeOptional);
    if (itemOfEdgesNode == null) {
      return Optional.empty();
    }

    if (itemOfEdgesNode.size() >= 1) {
      if (itemOfEdgesNode.size() > 1) {
        LOG.debug("Der Node hat mehr als ein itemOf-Node: {}", id.getId());
      }
      final JsonObject firstItemOfEdge = itemOfEdgesNode.get(0).getAsJsonObject();

      return GsonGraphQLHelper.getChildObjectIfExists(
          firstItemOfEdge, BrGraphQLNodeNames.RESULT_NODE.getName());
    }

    return Optional.empty();
  }

  @Nullable
  private JsonArray getEdgeNodes(final Optional<JsonObject> itemOfRootNodeOptional) {
    if (itemOfRootNodeOptional.isEmpty()) {
      return null;
    }
    final JsonObject itemOfRootNode = itemOfRootNodeOptional.get();

    final Optional<JsonArray> itemOfEdgesNodeOptional =
        GsonGraphQLHelper.getChildArrayIfExists(
            itemOfRootNode, BrGraphQLNodeNames.RESULT_NODE_EDGES.getName());
    if (itemOfEdgesNodeOptional.isEmpty()) {
      return null;
    }
    final JsonArray itemOfEdgesNode = itemOfEdgesNodeOptional.get();

    if (itemOfEdgesNode.size() == 0) {
      return null;
    }
    return itemOfEdgesNode;
  }

  private Optional<Duration> getClipLaenge(final JsonObject clipDetailRoot) {

    final Optional<JsonPrimitive> durationElementOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.INT_CLIP_DURATION.getName());
    if (durationElementOptional.isEmpty()) {
      return Optional.empty();
    }
    final JsonPrimitive durationElement = durationElementOptional.get();

    return Optional.of(Duration.ofSeconds(durationElement.getAsInt()));
  }

  private Optional<LocalDateTime> getSendeZeitpunkt(final JsonObject clipDetailRoot) {

    /*
     * Normale ITEMS besitzen keinen Ausstrahlungszeitpunkt, Programme normalerweise schon.
     */
    if (!id.getType().equals(BrClipType.PROGRAMME)) {
      return Optional.empty();
    }

    if (!clipDetailRoot.has(JSON_ELEMENT_INITIAL_SCREENING)) {
      return Optional.empty();
    }

    final JsonElement initialScreeningElement = clipDetailRoot.get(JSON_ELEMENT_INITIAL_SCREENING);
    if (initialScreeningElement.isJsonNull()) {
      return Optional.empty();
    }
    final JsonObject initialScreening = initialScreeningElement.getAsJsonObject();
    if (!initialScreening.has(BrGraphQLElementNames.STRING_CLIP_START.getName())) {
      return Optional.empty();
    }

    final String startDateTimeString =
        initialScreening.get(BrGraphQLElementNames.STRING_CLIP_START.getName()).getAsString();

    return Optional.of(brDateTimeString2LocalDateTime(startDateTimeString));
  }

  private Optional<Map<Resolution, FilmUrl>> getVideos(final JsonObject clipDetailRoot) {

    final Optional<JsonObject> videoFilesOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            clipDetailRoot, BrGraphQLNodeNames.RESULT_CLIP_VIDEO_FILES.getName());
    final JsonArray videoFilesEdges = getEdgeNodes(videoFilesOptional);

    if (videoFilesEdges == null) {
      return Optional.empty();
    }

    final Map<Resolution, FilmUrl> videoListe = new ConcurrentHashMap<>();

    videoFilesEdges.forEach(
        (JsonElement currentEdge) -> {
          if (currentEdge.isJsonObject()) {

            final Optional<JsonObject> videoFilesEdgeNodeOptional =
                GsonGraphQLHelper.getChildObjectIfExists(
                    currentEdge.getAsJsonObject(), BrGraphQLNodeNames.RESULT_NODE.getName());
            if (videoFilesEdgeNodeOptional.isPresent()) {

              final JsonObject videoFilesEdgeNode = videoFilesEdgeNodeOptional.get();

              final Optional<JsonPrimitive> videoFileURLOptional =
                  GsonGraphQLHelper.getChildPrimitiveIfExists(
                      videoFilesEdgeNode, BrGraphQLElementNames.STRING_CLIP_URL.getName());
              final Optional<JsonPrimitive> fileSizeOptional =
                  GsonGraphQLHelper.getChildPrimitiveIfExists(
                      videoFilesEdgeNode, BrGraphQLElementNames.STRING_CLIP_FILE_SIZE.getName());

              final Optional<JsonObject> accessibleInOptional =
                  GsonGraphQLHelper.getChildObjectIfExists(videoFilesEdgeNode, "accessibleIn");
              if (accessibleInOptional.isPresent()) {
                final Optional<JsonPrimitive> countAccessibleInEdgesOptional =
                    GsonGraphQLHelper.getChildPrimitiveIfExists(
                        accessibleInOptional.get(), "count");
                if (countAccessibleInEdgesOptional.isPresent()
                    && countAccessibleInEdgesOptional.get().getAsInt() > 0) {
                  LOG.debug("{} hat Geoinformationen?!", id.getId());
                }
              }

              final Optional<JsonObject> videoFileProfileNodeOptional =
                  GsonGraphQLHelper.getChildObjectIfExists(
                      videoFilesEdgeNode, BrGraphQLNodeNames.RESULT_CLIP_VIDEO_PROFILE.getName());
              if (videoFileProfileNodeOptional.isPresent()) {
                final JsonObject videoFileProfileNode = videoFileProfileNodeOptional.get();

                final Optional<JsonPrimitive> videoProfileIDOptional =
                    GsonGraphQLHelper.getChildPrimitiveIfExists(
                        videoFileProfileNode, BrGraphQLElementNames.ID_ELEMENT.getName());

                if (videoFileURLOptional.isPresent() && videoProfileIDOptional.isPresent()) {

                  final JsonPrimitive videoFileURL = videoFileURLOptional.get();
                  final JsonPrimitive videoFileProfile = videoProfileIDOptional.get();

                  if (videoFileURL.isString() && videoFileProfile.isString()) {

                    // Nur hier haben wir sowohl eine gültige URL als auch ein VideoProfil um einen
                    // MapEintrag zu erzeugen!

                    final Resolution resolution =
                        Resolution.getResolutionFromArdAudioVideoOrdinalsByProfileName(
                            videoFileProfile.getAsString());

                    final URL videoURL;
                    try {
                      videoURL = new URL(videoFileURL.getAsString());
                      long fileSize = 0L;
                      if (fileSizeOptional.isPresent()) {
                        fileSize = fileSizeOptional.get().getAsLong() / 1024 / 1024;
                      }
                      final FilmUrl filmUrl = new FilmUrl(videoURL, fileSize);

                      if (!videoListe.containsKey(resolution)) {
                        videoListe.put(resolution, filmUrl);
                      }

                    } catch (final MalformedURLException e) {
                      // Nothing to be done here
                      LOG.error(
                          "Fehlerhafte URL in den VideoURLs vorhanden! Clip-ID: {}", id.getId());
                    }
                  }
                }
              }
            }
          }
        });

    if (videoListe.size() > 0) {
      return Optional.of(videoListe);
    }
    LOG.error("Erzeugung der VideoURLs fehlgeschlagen für ID: {}", id.getId());
    return Optional.empty();
  }

  private Optional<String> getBeschreibung(final JsonObject clipDetailRoot) {

    final Optional<JsonPrimitive> descriptionOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_DESCRIPTION.getName());
    if (descriptionOptional.isPresent()) {

      final JsonPrimitive description = descriptionOptional.get();
      if (description.isString() && StringUtils.isNotEmpty(description.getAsString())) {
        return Optional.of(description.getAsString());
      }
    }

    final Optional<JsonPrimitive> shortDescriptionOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_SHORT_DESCRIPTION.getName());
    if (shortDescriptionOptional.isPresent()) {

      final JsonPrimitive shortDescription = shortDescriptionOptional.get();
      if (shortDescription.isString() && StringUtils.isNotEmpty(shortDescription.getAsString())) {
        return Optional.of(shortDescription.getAsString());
      }
    }

    return Optional.empty();
  }

  private Optional<URL> getWebSite(final JsonObject clipDetailRoot) {

    final Optional<JsonPrimitive> slugOptional =
        GsonGraphQLHelper.getChildPrimitiveIfExists(
            clipDetailRoot, BrGraphQLElementNames.STRING_CLIP_SLUG.getName());
    if (slugOptional.isPresent()) {

      final JsonPrimitive slug = slugOptional.get();

      if (slug.isString() && StringUtils.isNotEmpty(slug.getAsString())) {
        try {
          return Optional.of(
              new URL(DEFAULT_BR_VIDEO_URL_PRAEFIX + slug.getAsString() + "-" + id.getId()));
        } catch (final MalformedURLException e) {
          // Wird ein Empty!
        }
      }
    }

    return Optional.empty();
  }

  private Optional<Set<URL>> getSubtitles(final JsonObject clipDetailRoot) {

    final Optional<JsonObject> captionFilesOptional =
        GsonGraphQLHelper.getChildObjectIfExists(
            clipDetailRoot, BrGraphQLNodeNames.RESULT_CLIP_CAPTION_FILES.getName());
    if (captionFilesOptional.isEmpty()) {
      return Optional.empty();
    }

    final JsonObject captionFiles = captionFilesOptional.get();

    final Optional<JsonArray> captionFilesEdgesOptional =
        GsonGraphQLHelper.getChildArrayIfExists(
            captionFiles, BrGraphQLNodeNames.RESULT_NODE_EDGES.getName());
    if (captionFilesEdgesOptional.isEmpty()) {
      return Optional.empty();
    }

    final JsonArray captionFilesEdges = captionFilesEdgesOptional.get();
    if (captionFilesEdges.size() == 0) {
      return Optional.empty();
    }

    final Set<URL> subtitleUrls = new HashSet<>();

    captionFilesEdges.forEach(
        (JsonElement currentEdge) -> {
          if (currentEdge.isJsonObject()) {

            final Optional<JsonObject> captionFilesNodeOptional =
                GsonGraphQLHelper.getChildObjectIfExists(
                    currentEdge.getAsJsonObject(), BrGraphQLNodeNames.RESULT_NODE.getName());
            if (captionFilesNodeOptional.isPresent()) {

              final JsonObject captionFilesNode = captionFilesNodeOptional.get();

              final Optional<JsonPrimitive> publicLocationOptional =
                  GsonGraphQLHelper.getChildPrimitiveIfExists(
                      captionFilesNode, BrGraphQLElementNames.STRING_CLIP_URL.getName());
              if (publicLocationOptional.isPresent()) {
                final JsonPrimitive captionFileUrl = publicLocationOptional.get();

                if (captionFileUrl.isString()) {

                  try {
                    subtitleUrls.add(new URL(captionFileUrl.getAsString()));
                  } catch (final MalformedURLException e) {
                    // Keine gültige URL kein Eintrag fürs Set
                  }
                }
              }
            }
          }
        });

    if (!subtitleUrls.isEmpty()) {
      return Optional.of(subtitleUrls);
    }

    return Optional.empty();
  }

  private LocalDateTime brDateTimeString2LocalDateTime(final String dateTimeString) {

    final ZonedDateTime inputDateTime =
        ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);

    return inputDateTime.withZoneSameInstant(ZoneId.of("Europe/Berlin")).toLocalDateTime();
  }
}
