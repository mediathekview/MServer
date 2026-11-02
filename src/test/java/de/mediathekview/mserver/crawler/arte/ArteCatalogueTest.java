package de.mediathekview.mserver.crawler.arte;

import de.mediathekview.mserver.daten.*;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CancellationException;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArteCatalogueTest {
  private static final String ROOT = ArteCatalogue.ALL_VIDEOS.formatted("de");
  private static final String PLAYER = "https://api.arte.tv/api/player/v2/config/de/118267-006-A";
  private static final String MASTER = "https://manifest-arte.akamaized.net/api/manifest/v1/Generate/test-id/de/XQ+KS+CHEV1/118267-006-A.m3u8";

  @Test
  void discoversPaginatedZonesAndCollectionsBeforeLoadingPlayers() throws IOException {
    String collection = "https://www.arte.tv/de/videos/RC-022391/example/";
    String episodes = "https://api-cdn.arte.tv/api/emac/v4/de/web/zones/8fbf35e4-324d-4d3e-a194-28f7d288789b/content?collectionId=RC-022391&subCollectionId=RC-022392&page=1&pageId=collection&type=collection";
    var calls = new ArrayList<String>();
    var responses = Map.of(
        ROOT, """
            {"zones":[{"content":{"data":[
              {"id":"118267-006-A_de", "kind":{"code":"SHOW"}, "genre":{"label":"Dokus"}},
              {"id":"118267-008-A_de", "availability":{"hasVideoStreams":false}},
              {"id":"RC-022391_de", "kind":{"isCollection":true}, "url":"%s"}
            ],"pagination":{"page":1,"pages":3,"links":{"next":"https://example.org/feed?page=2"}}}},
            {"content":{"data":[{"id":"118267-006-A_de"}]}}]}
            """.formatted(collection),
        collection, "<script>subCollectionId\\u003dRC-022392</script>",
        episodes, """
        {"data":[{"id":"118267-007-A_de"},{"id":"118267-006-A_de"}]}""",
        "https://example.org/feed?page=2", """
            {"data":[{"id":"118267-009-A_de"}],"pagination":{"page":2,"pages":3,"links":{"next":"https://example.org/feed?page=3"}}}
            """);
    var api = api("de", 2, url -> { calls.add(url); return Objects.requireNonNull(responses.get(url), url); });

    var programmes = api.discover();

    assertEquals(Set.of("118267-006-A", "118267-007-A", "118267-009-A"),
        programmes.stream().map(ArteCatalogue.Programme::programmeId).collect(java.util.stream.Collectors.toSet()));
    assertEquals(4, calls.size());
    assertTrue(calls.stream().noneMatch(s -> s.contains("/config/") || s.contains("page=3")));
    var first = programmes.iterator().next();
    assertEquals("118267-006-A_SHOW_ARTE_NEXT_DE_de", first.id());
    assertEquals(PLAYER, first.configUrl());
    assertEquals("Dokus", first.topic());
  }

  @Test
  void createsLanguageSpecificHlsWithMetadataAudioDescriptionAndSubtitles() throws IOException {
    var calls = new ArrayList<String>();
    var responses = Map.of(
        PLAYER, player("""
            {"code":"VA", "audioLanguage":"de"},
            {"code":"VA-STMA", "audioLanguage":"de", "subtitleLanguage":"de", "closedCaptioning":true},
            {"code":"VOF-STA", "audioLanguage":"fr", "subtitleLanguage":"de"},
            {"code":"VAAUD", "audioLanguage":"de", "audioDescription":true}
            """, MASTER, "2026-03-29T01:30:00Z"),
        MASTER.replace("/de/", "/VOF-STA/"), "#EXTM3U",
        MASTER.replace("/de/", "/VAAUD/"), "#EXTM3U",
        MASTER, """
            #EXTM3U
            #EXT-X-MEDIA:TYPE=SUBTITLES,GROUP-ID="subs",LANGUAGE="de",URI="subs/de.m3u8"
            """,
        MASTER.substring(0, MASTER.lastIndexOf('/') + 1) + "subs/de.m3u8", "#EXTM3U\n#EXTINF:5,\nde.vtt\n#EXT-X-ENDLIST");
    var api = api("de", 1, url -> { calls.add(url); return Objects.requireNonNull(responses.get(url), url); });

    var films = api.films(programme("de"));

    assertEquals(2, films.size());
    Film german = films.getFirst();
    assertEquals("A title - A subtitle", german.getTitel());
    assertEquals(Sender.ARTE_DE, german.getSender());
    assertEquals("deu", german.getAudioLanguage());
    assertEquals(LocalDateTime.of(2026, 3, 29, 3, 30), german.getTime());
    assertEquals(Duration.ofSeconds(1815), german.getDuration());
    assertEquals("Dokus", german.getThema());
    assertEquals("118267-006-A_SHOW_ARTE_NEXT_DE_de", german.getId());
    assertEquals(MASTER, german.getDefaultUrl().orElseThrow().getUrl().toString());
    assertEquals(-1, german.getDefaultUrl().orElseThrow().getFileSize());
    assertEquals(MASTER.replace("/de/", "/VAAUD/"), german.getAudioDescription(Resolution.NORMAL).getUrl().toString());
    assertEquals(1, german.getSubtitles().size());
    assertTrue(german.getSubtitles().iterator().next().toString().endsWith("/subs/de.vtt"));
    assertEquals("fra", films.get(1).getAudioLanguage());
    assertTrue(films.get(1).getTitel().endsWith("(Französisch)"));
    assertEquals(5, calls.size()); // Player, two additional versions and one shared subtitle lookup; no size requests.
  }

  @Test
  void polishCatalogueSelectsPolishAudioAndEncodesVersionCode() throws IOException {
    var api = api("pl", 1, url -> url.contains("/config/") ? player("""
        {"code":"VA", "audioLanguage":"de"},
        {"code":"VE[POL]", "audioLanguage":"pl"}
        """, MASTER, "2026-11-01T11:00:00+01:00") : "#EXTM3U");
    Film film = api.films(programme("pl")).getFirst();
    assertEquals("pol", film.getAudioLanguage());
    assertEquals(Sender.ARTE_PL, film.getSender());
    assertEquals("A title - A subtitle", film.getTitel());
    assertTrue(film.getDefaultUrl().orElseThrow().getUrl().toString().contains("/VE%5BPOL%5D/"));
    assertEquals(LocalDateTime.of(2026, 11, 1, 11, 0), film.getTime());
  }

  @Test
  void keepsOriginalAudioForSubtitleOnlyCataloguesWithoutInventingALanguage() throws IOException {
    var api = api("pl", 1, url -> url.contains("/config/")
        ? player("""
        {"code":"VOEU-STE[POL]", "audioLanguage":"mul", "subtitleLanguage":"pl"}""", MASTER, "")
        : "#EXTM3U");
    Film film = api.films(programme("pl")).getFirst();
    assertNull(film.getAudioLanguage());
    assertTrue(film.getTitel().endsWith("(Originalversion)"));
    assertNull(film.getTime());
    assertFalse(film.getUrls().isEmpty());
  }

  @Test
  void keepsOriginalConcertAudioWithoutSubtitles() throws IOException {
    var api = api("de", 1, _ -> player("""
        {"code":"VO", "audioLanguage":"en"}""", MASTER, ""));
    Film film = api.films(programme("de")).getFirst();
    assertEquals("eng", film.getAudioLanguage());
    assertTrue(film.getTitel().endsWith("(Englisch)"));
  }

  @Test
  void keepsPublishedDefaultAndSkipsUnavailableSynthesizedVersions() throws IOException {
    var calls = new ArrayList<String>();
    var api = api("de", 1, url -> {
      calls.add(url);
      if (!url.contains("/config/")) return ""; // Version-specific 404/410 response.
      return player("""
          {"code":"VA", "audioLanguage":"de"},
          {"code":"VOEU[FRA]-STA", "audioLanguage":"mul", "subtitleLanguage":"de"}
          """, MASTER, "");
    });
    var films = api.films(programme("de"));
    assertEquals(1, films.size());
    assertEquals(MASTER, films.getFirst().getDefaultUrl().orElseThrow().getUrl().toString());
    assertTrue(calls.stream().noneMatch(url -> url.contains("/VA/")));
  }

  @Test
  void skipsUnavailablePlayers() throws IOException {
    var api = api("de", 1, _ -> """
        {"data":{"attributes":{"metadata":{"title":"Expired"},"streams":[]}}}""");
    assertTrue(api.films(programme("de")).isEmpty());
  }

  @Test
  void aFailedPageDoesNotDiscardOtherDiscoveredProgrammes() throws IOException {
    var errors = new ArrayList<Exception>();
    var api = new ArteCatalogue("de", Sender.ARTE_DE, 2, url -> {
      if (!url.equals(ROOT)) throw new IOException("page failed");
      return """
          {"zones":[{"content":{"data":[{"id":"118267-006-A_de"}],
          "pagination":{"page":1,"pages":2,"links":{"next":"https://example.org/page2"}}}}]}
          """;
    }, errors::add);
    assertEquals(1, api.discover().size());
    assertEquals(1, errors.size());
  }

  @Test
  void aSocketTimeoutDoesNotDiscardDiscoveredProgrammes() throws IOException {
    var errors = new ArrayList<Exception>();
    var api = new ArteCatalogue("de", Sender.ARTE_DE, 2, url -> {
      if (!url.equals(ROOT)) throw new SocketTimeoutException("page timed out");
      return """
          {"data":[{"id":"118267-006-A_de"}],
           "pagination":{"page":1,"pages":2,"links":{"next":"https://example.org/page2"}}}
          """;
    }, errors::add);
    assertEquals(1, api.discover().size());
    assertEquals(1, errors.size());
  }

  @Test
  void optionalSubtitleTimeoutDoesNotDiscardPlayableFilm() throws IOException {
    var errors = new ArrayList<Exception>();
    var api = new ArteCatalogue("de", Sender.ARTE_DE, 1, url -> {
      if (!url.equals(PLAYER)) throw new SocketTimeoutException("subtitle timed out");
      return player("""
          {"code":"VA", "audioLanguage":"de", "subtitleLanguage":"de"}
          """, MASTER, "");
    }, errors::add);
    var films = api.films(programme("de"));
    assertEquals(1, films.size());
    assertTrue(films.getFirst().getSubtitles().isEmpty());
    assertEquals(1, errors.size());
  }

  @Test
  void malformedItemDoesNotHideFollowingProgramme() throws IOException {
    var errors = new ArrayList<Exception>();
    var api = new ArteCatalogue("de", Sender.ARTE_DE, 1, _ -> """
        {"data":[{"id":"118267-006-A_de"},{"id":{"malformed":true}},{"id":"118267-007-A_de"}]}
        """, errors::add);
    assertEquals(2, api.discover().size());
    assertEquals(1, errors.size());
  }

  @Test
  void cancellationPropagatesWithoutBeingReportedAsPageFailure() {
    var errors = new ArrayList<Exception>();
    var api = new ArteCatalogue("de", Sender.ARTE_DE, 1, _ -> {
      throw new CancellationException("cancelled");
    }, errors::add);
    assertThrows(CancellationException.class, api::discover);
    assertTrue(errors.isEmpty());
  }

  private ArteCatalogue api(String language, int maxPages, ArteCatalogue.Http http) {
    Sender sender = language.equals("pl") ? Sender.ARTE_PL : Sender.ARTE_DE;
    return new ArteCatalogue(language, sender, maxPages, http, e -> fail(e));
  }

  private ArteCatalogue.Programme programme(String language) {
    return new ArteCatalogue.Programme("118267-006-A", language, "SHOW", "Dokus", "https://www.arte.tv/", PLAYER);
  }

  private String player(String versions, String url, String begin) {
    return """
        {"data":{"attributes":{
          "metadata":{"title":"A title","subtitle":"A subtitle","description":"Description",
          "link":{"url":"https://www.arte.tv/de/videos/118267-006-A/example/"},"duration":{"seconds":1815}},
          "rights":{"begin":"%s"},"restriction":{"geoblocking":{"code":"DE_FR"}},
          "streams":[{"url":"%s", "versions":[%s]}]
        }}}
        """.formatted(begin, url, versions);
  }
}
