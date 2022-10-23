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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    /**
     * Decides which FilmUrl to use by comparing the file sizes. If equals take the first.
     *
     * @param filmUrl        One film url to compare. This one is used if both have the same file size.
     * @param anotherFilmUrl Another film url to compare.
     * @return The film url with the bigger file size or if equal the first one.
     */
    @NotNull
    private static FilmUrl chooseFilmUrlWithBiggerSize(FilmUrl filmUrl, FilmUrl anotherFilmUrl) {
        if (filmUrl == null || filmUrl.getUrl() == null || filmUrl.getFileSize() == null) {
            return anotherFilmUrl;
        }

        if (anotherFilmUrl == null || anotherFilmUrl.getUrl() == null || anotherFilmUrl.getFileSize() == null) {
            return filmUrl;
        }
        return filmUrl.getFileSize() > anotherFilmUrl.getFileSize() ? filmUrl : anotherFilmUrl;
    }

    @NotNull
    private static Optional<String> getVideoFileUrlFromVideoFileEdgeResultNode(JsonObject videoFilesEdgeResultNode) {
        return GsonGraphQLHelper.getChildPrimitiveIfExists(videoFilesEdgeResultNode, BrGraphQLElementNames.STRING_CLIP_URL.getName())
                .filter(JsonPrimitive::isString)
                .map(JsonPrimitive::getAsString);
    }

    @NotNull
    private static Integer getGeoInformationCountFromVideoFileEdgeResultNode(JsonObject videoFilesEdgeResultNode) {
        return GsonGraphQLHelper.getChildObjectIfExists(videoFilesEdgeResultNode, "accessibleIn")
                .flatMap(accessibleInNode -> GsonGraphQLHelper.getChildPrimitiveIfExists(
                        accessibleInNode, "count"))
                .filter(JsonPrimitive::isNumber)
                .map(JsonPrimitive::getAsInt).orElse(0);
    }

    @NotNull
    private static Optional<Resolution> getResolutionFromVideoFileEdgeResultNode(JsonObject videoFilesEdgeNodeOptional) {
        return GsonGraphQLHelper.getChildObjectIfExists(
                        videoFilesEdgeNodeOptional, BrGraphQLNodeNames.RESULT_CLIP_VIDEO_PROFILE.getName())
                .flatMap(videoFileProfileNode ->
                        GsonGraphQLHelper.getChildPrimitiveIfExists(
                                videoFileProfileNode, BrGraphQLElementNames.ID_ELEMENT.getName()))
                .filter(JsonPrimitive::isString)
                .map(JsonPrimitive::getAsString)
                .map(Resolution::getResolutionFromArdAudioVideoOrdinalsByProfileName);
    }

    @NotNull
    private static Optional<String> findSubtitleUrlFromCaptionFilesEdge(JsonObject captionFilesEdge) {
        return GsonGraphQLHelper.getChildObjectIfExists(
                        captionFilesEdge, BrGraphQLNodeNames.RESULT_NODE.getName())
                .flatMap(captionFilesResultNode -> GsonGraphQLHelper.getChildPrimitiveIfExists(
                        captionFilesResultNode, BrGraphQLElementNames.STRING_CLIP_URL.getName()))
                .filter(JsonPrimitive::isString)
                .map(JsonPrimitive::getAsString);
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

            final Set<URL> subtitles = getSubtitles(clipDetailRoot);
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

                currentFilm.addAllSubtitleUrls(subtitles);
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
        return getFirstNode(itemOfRootNodeOptional.orElse(null));
    }

    private Optional<JsonObject> getFirstNode(@Nullable final JsonObject itemOfRootNodeOptional) {
        var edgeNodes = this.getEdgeNodes(itemOfRootNodeOptional);

        if (!edgeNodes.isEmpty()) {
            if (edgeNodes.size() > 1) {
                LOG.debug("Der Node hat mehr als ein itemOf-Node: {}", id.getId());
            }
            final JsonObject firstItemOfEdge = edgeNodes.get(0).getAsJsonObject();

            return GsonGraphQLHelper.getChildObjectIfExists(
                    firstItemOfEdge, BrGraphQLNodeNames.RESULT_NODE.getName());
        }

        return Optional.empty();
    }

    private JsonArray getEdgeNodes(@Nullable final JsonObject itemOfRootNode) {
        return Optional.ofNullable(itemOfRootNode)
                .flatMap(item -> GsonGraphQLHelper.getChildArrayIfExists(
                        item, BrGraphQLNodeNames.RESULT_NODE_EDGES.getName()))
                .orElse(new JsonArray());
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
        final JsonArray videoFilesEdges = getEdgeNodes(videoFilesOptional.orElse(null));

        final Map<Resolution, FilmUrl> videoListe =
                new ConcurrentHashMap<>(StreamSupport.stream(videoFilesEdges.spliterator(), false)
                        .filter(JsonElement::isJsonObject)
                        .map(JsonElement::getAsJsonObject)
                        .map(this::videoFilesEdgeToBrVideo)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toMap(BrVideo::resolution, BrVideo::url,
                                ///Resolves duplicates by picking the one with the bigger file size and if the sizes are the same take the first one
                                BrClipDetailsDeserializer::chooseFilmUrlWithBiggerSize)));

        if (videoListe.isEmpty()) {
            LOG.error("Erzeugung der VideoURLs fehlgeschlagen für ID: {}", id.getId());
            return Optional.empty();
        }
        return Optional.of(videoListe);
    }

    private Optional<BrVideo> videoFilesEdgeToBrVideo(JsonObject videoFileEdge) {
        final Optional<JsonObject> videoFilesEdgeResultNode =
                GsonGraphQLHelper.getChildObjectIfExists(
                        videoFileEdge, BrGraphQLNodeNames.RESULT_NODE.getName());
        if(videoFilesEdgeResultNode.isPresent()) {
            var geoInformationCount = getGeoInformationCountFromVideoFileEdgeResultNode(videoFilesEdgeResultNode.get());
            if (geoInformationCount > 0) {
                LOG.debug("{} hat Geoinformationen?!", id.getId());
            }

            var resolution = getResolutionFromVideoFileEdgeResultNode(videoFilesEdgeResultNode.get());
            var videoFileUrl = getVideoFileUrlFromVideoFileEdgeResultNode(videoFilesEdgeResultNode.get());

            if (videoFileUrl.isPresent() && resolution.isPresent()) {
                try {
                    final Long fileSize = videoFilesEdgeResultNode.flatMap(videoFilesEdgeNode ->
                                    GsonGraphQLHelper.getChildPrimitiveIfExists(
                                            videoFilesEdgeNode, BrGraphQLElementNames.STRING_CLIP_FILE_SIZE.getName()))
                            .filter(fileSizePrimitive -> !fileSizePrimitive.isJsonNull() && fileSizePrimitive.isNumber())
                            .map(JsonPrimitive::getAsLong).orElse(0L);

                    return Optional.of(new BrVideo(resolution.get(), new FilmUrl(videoFileUrl.get(), getFileSizeInKB(fileSize))));
                } catch (final MalformedURLException e) {
                    // Nothing to be done here
                    LOG.error(
                            "Fehlerhafte URL in den VideoURLs vorhanden! Clip-ID: {}", id.getId());
                }
            }
        }
        return Optional.empty();
    }

    private long getFileSizeInKB(Long fileSize) {
        // fileSize is in bytes
        return fileSize == 0 ? 0 : fileSize / 1024;
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

    private Set<URL> getSubtitles(final JsonObject clipDetailRoot) {

        var captionFilesEdges = GsonGraphQLHelper.getChildObjectIfExists(
                clipDetailRoot, BrGraphQLNodeNames.RESULT_CLIP_CAPTION_FILES.getName())
                .flatMap(captionFiles ->
                        GsonGraphQLHelper.getChildArrayIfExists(captionFiles, BrGraphQLNodeNames.RESULT_NODE_EDGES.getName())
                ).orElse(new JsonArray());

        return StreamSupport.stream(captionFilesEdges.spliterator(), false)
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .map(BrClipDetailsDeserializer::findSubtitleUrlFromCaptionFilesEdge)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::tryToCreateUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private @Nullable URL tryToCreateUrl(String text) {
        try {
            return new URL(text);
        } catch (final MalformedURLException e) {
            // Keine gültige URL
        }
        return null;
    }

    private LocalDateTime brDateTimeString2LocalDateTime(final String dateTimeString) {

        final ZonedDateTime inputDateTime =
                ZonedDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);

        return inputDateTime.withZoneSameInstant(ZoneId.of("Europe/Berlin")).toLocalDateTime();
    }
}
