package de.mediathekview.mserver.crawler.arte.json;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArteVideoPlayerDeserializerTest {

  @Test
  void deserialize() {
    JsonObject json = JsonFileReader.readJson("/arte/arte_film_video_player.json");
    ArteVideoPlayerDeserializer target = new ArteVideoPlayerDeserializer();
    final List<ArteVideoLinkDto> actual = target.deserialize(json, null, null);

    assertEquals(5, actual.size());
    assertEquals(
        "https://manifest-arte.akamaized.net/api/manifest/v1/Generate/2906b266-de99-459f-bc38-918949928757/VA-STA/XQ+KS+CHEV1/113627-000-A.m3u8",
        actual.getFirst().getUrl().orElseThrow());
    assertEquals("113627-000-A", actual.getFirst().getProgramId().orElseThrow());
    assertEquals("XQ", actual.getFirst().getQuality().orElseThrow());
    assertEquals("1", actual.getFirst().getAudioSlot().orElseThrow());
    assertEquals("VA-STA", actual.getFirst().getAudioCode().orElseThrow());
    assertEquals("Deutsch", actual.getFirst().getAudioLabel().orElseThrow());
    assertEquals("DE", actual.getFirst().getAudioShortLabel().orElseThrow());
    assertEquals(
        "https://manifest-arte.akamaized.net/api/manifest/v1/Generate/2906b266-de99-459f-bc38-918949928757/VOEU%5BFRA%5D-STE%5BANG%5D/XQ+KS+CHEV1/113627-000-A.m3u8",
        actual.get(3).getUrl().orElseThrow());
  }

  @Test
  void deserializeNoVideo() {
    JsonObject json = JsonFileReader.readJson("/arte/arte_film_video_player_no_url.json");
    ArteVideoPlayerDeserializer target = new ArteVideoPlayerDeserializer();
    final List<ArteVideoLinkDto> actual = target.deserialize(json, null, null);

    assertTrue(actual.isEmpty());
  }

  @Test
  void deserializeReplacesAllSupportedLocalePathSegments() {
    for (String locale : List.of("de", "fr", "pl", "it", "es", "en", "ro")) {
      JsonObject json = JsonFileReader.readJson("/arte/arte_film_video_player.json");
      JsonArray streams =
          json.getAsJsonObject("data")
              .getAsJsonObject("attributes")
              .getAsJsonArray("streams");
      streams
          .get(0)
          .getAsJsonObject()
          .addProperty(
              "url",
              "https://manifest-arte.akamaized.net/api/manifest/v1/Generate/" + locale + "/XQ/");

      final List<ArteVideoLinkDto> actual =
          new ArteVideoPlayerDeserializer().deserialize(json, null, null);

      assertTrue(actual.getFirst().getUrl().orElseThrow().contains("/VA-STA/XQ/"), locale);
      assertFalse(actual.getFirst().getUrl().orElseThrow().contains("/" + locale + "/"), locale);
    }
  }
}
