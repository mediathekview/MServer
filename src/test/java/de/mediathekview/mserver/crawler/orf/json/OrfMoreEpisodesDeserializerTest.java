package de.mediathekview.mserver.crawler.orf.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrfMoreEpisodesDeserializerTest {

  @Test
  void testDeserialize() {
    final JsonElement jsonElement = JsonFileReader.readJson("/orf/orf_film_more_episodes.json");

    final OrfMoreEpisodesDeserializer target = new OrfMoreEpisodesDeserializer();
    final CrawlerUrlDTO actual = target.deserialize(jsonElement, null, null);

    assertNotNull(actual);
    assertEquals("https://tvthek.orf.at/lane-plus/other_episodes_of_profile?profileId=13895917&profileSlug=Biester", actual.getUrl());

  }
}
