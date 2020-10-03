package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArdTopicsOverviewDeserializerTest {

  @Test
  public void testDeserialize() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_topics.json");

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzNuYWNoOQ?pageSize=50"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2J1dGVudW5iaW5uZW4?pageSize=50"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL3Nwb3J0YmxpdHo?pageSize=50"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2J1dGVudW5iaW5uZW51bTY?pageSize=50"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2J1dGVudW5iaW5uZW53ZXR0ZXI?pageSize=50"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2Nvcm9uYQ?pageSize=50"),
        };

    final ArdTopicsOverviewDeserializer instance = new ArdTopicsOverviewDeserializer();

    final Set<CrawlerUrlDTO> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
}
