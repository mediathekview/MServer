package de.mediathekview.mserver.crawler.arte.json;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ArteVideoPlayerDeserializerTest {

  @Test
  void deserialize() {
    JsonObject json = JsonFileReader.readJson("/arte/arte_film_video_player.json");
    ArteVideoPlayerDeserializer target = new ArteVideoPlayerDeserializer();
    final Optional<CrawlerUrlDTO> actual = target.deserialize(json, null, null);

    assertTrue(actual.isPresent());
    assertEquals(
        "https://manifest-arte.akamaized.net/api/manifest/v1/Generate/2906b266-de99-459f-bc38-918949928757/de/XQ+KS+CHEV1/113627-000-A.m3u8",
        actual.get().getUrl());
  }

  @Test
  void deserializeNoVideo() {
    JsonObject json = JsonFileReader.readJson("/arte/arte_film_video_player_no_url.json");
    ArteVideoPlayerDeserializer target = new ArteVideoPlayerDeserializer();
    final Optional<CrawlerUrlDTO> actual = target.deserialize(json, null, null);

    assertTrue(actual.isEmpty());
  }
}
