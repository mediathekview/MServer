package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.ard.PaginationUrlDto;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArdTopicsLetterDeserializerTest {

  @Test
  public void testDeserialize() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_topic_page.json");

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL3NlbmRlcmVpaGVuL2lkX2J1dGVudW5iaW5uZW4?pageSize=50"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL3NlbmRlcmVpaGVuL2lkX3Nwb3J0YmxpdHo?pageSize=50"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL3NlbmRlcmVpaGVuL2lkX2J1dGVudW5iaW5uZW53ZXR0ZXI?pageSize=50"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL3NlbmRlcmVpaGVuL2lkX2J1dGVudW5iaW5uZW51bTY?pageSize=50"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2J1dGVudW5iaW5uZW5nZWJhZXJkZW5zcHJhY2hl?pageSize=50"),
        };

    final ArdTopicsLetterDeserializer instance = new ArdTopicsLetterDeserializer();

    final PaginationUrlDto result = instance.deserialize(jsonElement, null, null);

    assertThat(result.getUrls().size(), equalTo(expected.length));
    assertThat(result.getUrls(), Matchers.containsInAnyOrder(expected));
    assertThat(result.getActualPage(), equalTo(0));
    assertThat(result.getMaxPages(), equalTo(1));
  }
}
