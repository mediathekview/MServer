package de.mediathekview.mserver.crawler.arte;

import com.google.gson.*;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.LanguageCodeUtils;
import de.mediathekview.mserver.daten.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.concurrent.CancellationException;
import java.util.regex.Pattern;

/** Current EMAC catalogue discovery and player-v2 conversion, independent of crawl scheduling. */
final class ArteCatalogue {
  static final String ALL_VIDEOS = "https://api.arte.tv/api/emac/v4/%s/web/pages/ALL_VIDEOS";
  private static final String PLAYER = "https://api.arte.tv/api/player/v2/config/%s/%s";
  private static final String COLLECTION = "https://api-cdn.arte.tv/api/emac/v4/%s/web/zones/8fbf35e4-324d-4d3e-a194-28f7d288789b/content?collectionId=%s&subCollectionId=%s&page=1&pageId=collection&type=collection";
  private static final Pattern PROGRAMME_ID = Pattern.compile("[0-9]{6}-[0-9A-Z]{3}-[A-Z]");
  private static final Pattern SUBCOLLECTION = Pattern.compile("subCollectionId(?:=|\\\\u003d)(RC-[0-9]{6})");
  private static final Pattern MANIFEST = Pattern.compile("(https://manifest-arte\\.akamaized\\.net/api/manifest/v1/Generate/[^/]+/)[^/]+(/.*)");
  private static final Pattern ATTRIBUTE = Pattern.compile("([A-Z0-9-]+)=(?:\"([^\"]*)\"|([^,]*))");
  private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");

  @FunctionalInterface
  interface Http { String get(String url) throws IOException; }
  record Programme(String programmeId, String language, String kind, String topic, String website, String configUrl) {
    // Keep the existing database identity namespace for each catalogue/sender.
    String id() {
      return programmeId + "_" + kind + "_ARTE_NEXT_" + language.toUpperCase(Locale.ROOT) + "_" + language;
    }
  }
  private record Audio(String language, String url, String master, boolean description) {}

  private final String language;
  private final Sender sender;
  private final int maximumPages;
  private final Http http;
  private final Consumer<Exception> reportError;

  ArteCatalogue(String language, Sender sender, int maximumPages, Http http, Consumer<Exception> reportError) {
    this.language = language;
    this.sender = sender;
    this.maximumPages = Math.max(1, maximumPages);
    this.http = http;
    this.reportError = reportError;
  }

  Collection<Programme> discover() {
    var programmes = new LinkedHashMap<String, Programme>();
    var pages = new ArrayDeque<String>();
    var visited = new HashSet<String>();
    var collections = new HashSet<String>();
    pages.add(ALL_VIDEOS.formatted(language));
    while (!pages.isEmpty()) {
      checkInterrupted();
      String url = pages.remove();
      if (!visited.add(url)) continue;
      try {
        JsonElement page = json(url);
        if (!page.isJsonObject() || (!page.getAsJsonObject().has("zones") && !page.getAsJsonObject().has("data"))) {
          throw new IOException("Missing ARTE catalogue content: " + url);
        }
        readPage(page, url, pages, collections, programmes);
      } catch (CancellationException e) {
        throw e;
      } catch (IOException | RuntimeException e) {
        reportError.accept(e);
      }
    }
    return List.copyOf(programmes.values());
  }

  private void readPage(JsonElement page, String base, Queue<String> pages, Set<String> collections,
      Map<String, Programme> programmes) {
    if (!page.isJsonObject()) return;
    for (var zone : array(page, "zones")) {
      try {
        readPage(element(zone, "content"), base, pages, collections, programmes);
      } catch (CancellationException e) {
        throw e;
      } catch (RuntimeException e) {
        reportError.accept(e);
      }
    }
    for (var item : array(page, "data")) {
      try {
        readItem(item, pages, collections, programmes);
      } catch (CancellationException e) {
        throw e;
      } catch (IOException | RuntimeException e) {
        reportError.accept(e);
      }
    }
    String next = text(page, "pagination", "links", "next");
    if (!next.isBlank()) {
      int current = number(page, 1, "pagination", "page");
      int count = number(page, current, "pagination", "pages");
      if (current < maximumPages && current < count) pages.add(URI.create(base).resolve(next).toString());
    }
  }

  private void readItem(JsonElement item, Queue<String> pages, Set<String> collections,
      Map<String, Programme> programmes) throws IOException {
    if (!item.isJsonObject() || text(item, "availability", "hasVideoStreams").equals("false")) return;
    String id = text(item, "programId");
    if (id.isBlank()) id = text(item, "id").split("_", 2)[0];
    if (id.startsWith("RC-") || text(item, "kind", "isCollection").equals("true")) {
      String website = text(item, "url");
      if (!website.isBlank() && collections.add(id)) {
        var matcher = SUBCOLLECTION.matcher(http.get(website));
        while (matcher.find()) {
          String subId = matcher.group(1);
          if (!id.equals(subId)) pages.add(COLLECTION.formatted(language, id, subId));
        }
      }
    } else if (PROGRAMME_ID.matcher(id).matches()) {
      String config = text(item, "player", "config");
      if (config.isBlank()) config = PLAYER.formatted(language, id);
      String kind = text(item, "kind", "code");
      programmes.putIfAbsent(id, new Programme(id, language, kind.isBlank() ? "SHOW" : kind,
          text(item, "genre", "label"), text(item, "url"), config));
    }
  }

  List<Film> films(Programme programme) throws IOException {
    JsonElement attributes = element(json(programme.configUrl()), "data", "attributes");
    JsonElement metadata = element(attributes, "metadata");
    String title = text(metadata, "title");
    if (title.isBlank()) return List.of();
    String subtitle = text(metadata, "subtitle");
    if (!subtitle.isBlank()) title += " - " + subtitle;
    var variants = new LinkedHashMap<String, Audio>();
    var audioDescriptions = new LinkedHashMap<String, Audio>();
    Audio fallback = null;
    for (var stream : array(attributes, "streams")) {
      String url = text(stream, "url");
      if (url.isBlank()) continue;
      var manifest = MANIFEST.matcher(url);
      boolean selectable = manifest.matches();
      var versions = array(stream, "versions");
      for (var version : versions) {
        String audio = text(version, "audioLanguage");
        String subs = text(version, "subtitleLanguage");
        String code = text(version, "code");
        String versionUrl = url;
        // The published URL already selects the catalogue's default version. Some current
        // assets do not support synthesizing the equivalent version-code URL.
        if (selectable && version != versions.getFirst() && !code.isBlank()) {
          versionUrl = manifest.group(1) + URLEncoder.encode(code, StandardCharsets.UTF_8) + manifest.group(2);
        } else if (selectable && version != versions.getFirst() && versions.size() > 1) {
          continue;
        }
        boolean description = text(version, "audioDescription").equals("true");
        if (fallback == null && !description) fallback = new Audio(audio, versionUrl, url, false);
        // Keep the catalogue's dub and originals with subtitles in that catalogue's language.
        if (!sameLanguage(audio, language) && !sameLanguage(subs, language)) continue;
        var target = description ? audioDescriptions : variants;
        String key = LanguageCodeUtils.normalize(audio).orElse(audio);
        target.putIfAbsent(key, new Audio(audio, versionUrl, url, description));
        if (!selectable) break; // Legacy stream URLs already select one audio version.
      }
      if (variants.isEmpty() && versions.isEmpty()) {
        variants.put("", new Audio("", url, url, false));
      }
    }
    if (variants.isEmpty() && fallback != null) {
      variants.put(LanguageCodeUtils.normalize(fallback.language()).orElse(fallback.language()), fallback);
    }
    if (variants.isEmpty()) variants.putAll(audioDescriptions);
    var result = new ArrayList<Film>();
    var subtitleUrls = new HashMap<String, String>();
    var playlists = new HashMap<String, String>();
    for (var entry : variants.entrySet()) {
      var audio = entry.getValue();
      if (!playable(audio, playlists)) continue;
      String filmTitle = title;
      if (!audio.language().isBlank() && !sameLanguage(audio.language(), language)) {
        String label = LanguageCodeUtils.normalize(audio.language())
            .map(_ -> Locale.forLanguageTag(audio.language()).getDisplayLanguage(Locale.GERMAN))
            .orElse("Originalversion");
        filmTitle += " (" + label + ")";
      }
      Film film = new Film(UUID.randomUUID(), sender, filmTitle,
          programme.topic().isBlank() ? "ARTE" : programme.topic(),
          aired(text(attributes, "rights", "begin")),
          Duration.ofSeconds(number(metadata, 0, "duration", "seconds")));
      film.setId(programme.id());
      film.setAudioLanguage(LanguageCodeUtils.normalize(audio.language()).orElse(null));
      film.setBeschreibung(text(metadata, "description"));
      String website = text(metadata, "link", "url");
      if (website.isBlank()) website = programme.website();
      if (!website.isBlank()) film.setWebsite(URI.create(website).toURL());
      film.addGeolocation(switch (text(attributes, "restriction", "geoblocking", "code")) {
        case "DE_FR" -> GeoLocations.GEO_DE_FR;
        case "EUR_DE_FR" -> GeoLocations.GEO_DE_AT_CH_FR;
        case "SAT" -> GeoLocations.GEO_DE_AT_CH_EU;
        default -> GeoLocations.GEO_NONE;
      });
      // HLS has no meaningful Content-Length. Avoid a HEAD/GET for every film and quality.
      if (!audio.description()) film.addUrl(Resolution.NORMAL, new FilmUrl(audio.url(), -1L));
      var description = audioDescriptions.get(entry.getKey());
      if (description != null && playable(description, playlists)) {
        film.addAudioDescription(Resolution.NORMAL, new FilmUrl(description.url(), -1L));
      }
      String subtitleUrl = subtitleUrls.get(audio.master());
      if (subtitleUrl == null) {
        subtitleUrl = subtitles(audio.master(), attributes, playlists);
        subtitleUrls.put(audio.master(), subtitleUrl);
      }
      if (!subtitleUrl.isBlank()) film.addSubtitle(URI.create(subtitleUrl).toURL());
      result.add(film);
    }
    return result;
  }

  private boolean playable(Audio audio, Map<String, String> playlists) throws IOException {
    if (audio.url().equals(audio.master())) return true;
    try {
      return playlist(audio.url(), playlists).stripLeading().startsWith("#EXTM3U");
    } catch (CancellationException e) {
      throw e;
    } catch (IOException e) {
      reportError.accept(e);
      return false;
    }
  }

  private String playlist(String url, Map<String, String> playlists) throws IOException {
    String text = playlists.get(url);
    if (text == null) {
      text = http.get(url);
      playlists.put(url, text);
    }
    return text;
  }

  private String subtitles(String master, JsonElement attributes, Map<String, String> playlists) throws IOException {
    boolean available = false;
    for (var stream : array(attributes, "streams")) {
      for (var version : array(stream, "versions")) {
        if (sameLanguage(text(version, "subtitleLanguage"), language)) available = true;
      }
    }
    if (!available || master.isBlank()) return "";
    try {
      for (String line : playlist(master, playlists).lines().toList()) {
        if (!line.startsWith("#EXT-X-MEDIA:")) continue;
        var fields = new HashMap<String, String>();
        var matcher = ATTRIBUTE.matcher(line.substring(line.indexOf(':') + 1));
        while (matcher.find()) fields.put(matcher.group(1), matcher.group(2) == null ? matcher.group(3) : matcher.group(2));
        if (!"SUBTITLES".equals(fields.get("TYPE")) || !sameLanguage(fields.getOrDefault("LANGUAGE", ""), language)) continue;
        String uri = fields.get("URI");
        if (uri == null) continue;
        String playlist = URI.create(master).resolve(uri).toString();
        if (!URI.create(playlist).getPath().endsWith(".m3u8")) return playlist;
        var files = playlist(playlist, playlists).lines().map(String::trim)
            .filter(s -> !s.isBlank() && !s.startsWith("#")).toList();
        return files.size() == 1 ? URI.create(playlist).resolve(files.getFirst()).toString() : "";
      }
    } catch (CancellationException e) {
      throw e;
    } catch (IOException | RuntimeException e) {
      reportError.accept(e);
    }
    return "";
  }

  private JsonElement json(String url) throws IOException {
    String body = http.get(url);
    return body.isBlank() ? JsonNull.INSTANCE : JsonParser.parseString(body);
  }

  private static boolean sameLanguage(String first, String second) {
    var normalized = LanguageCodeUtils.normalize(first);
    return normalized.isPresent() && normalized.equals(LanguageCodeUtils.normalize(second));
  }

  private static LocalDateTime aired(String value) {
    if (value.isBlank()) return null;
    try {
      return ZonedDateTime.parse(value).withZoneSameInstant(BERLIN).toLocalDateTime();
    } catch (DateTimeParseException _) {
      return null;
    }
  }

  private static void checkInterrupted() {
    if (Thread.currentThread().isInterrupted()) throw new CancellationException("ARTE crawl interrupted");
  }

  private static String text(JsonElement json, String... path) {
    return JsonUtils.getElementValueAsString(json, path).orElse("");
  }

  private static int number(JsonElement json, int fallback, String... path) {
    return JsonUtils.getElementValueAsInteger(json, path).orElse(fallback);
  }

  private static JsonElement element(JsonElement json, String... path) {
    return JsonUtils.getElement(json, path).orElse(JsonNull.INSTANCE);
  }

  private static List<JsonElement> array(JsonElement json, String... path) {
    var value = element(json, path);
    return value.isJsonArray() ? value.getAsJsonArray().asList() : List.of();
  }
}
