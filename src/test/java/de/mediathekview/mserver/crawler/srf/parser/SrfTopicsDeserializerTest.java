package de.mediathekview.mserver.crawler.srf.parser;

import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.Test;

public class SrfTopicsDeserializerTest {

  @Test
  public void test() {
    final CrawlerUrlDTO[] expectedUrls =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://www.srf.ch/play/v3/api/srf/production/videos-by-show-id?showId=c6a639e7-97a0-0001-5112-19c512b01474"),
          new CrawlerUrlDTO(
              "https://www.srf.ch/play/v3/api/srf/production/videos-by-show-id?showId=c5a89422-4580-0001-4f24-1889dc30d730"),
          new CrawlerUrlDTO(
              "https://www.srf.ch/play/v3/api/srf/production/videos-by-show-id?showId=c5e431c3-ab90-0001-3228-16001350159c"),
          new CrawlerUrlDTO(
              "https://www.srf.ch/play/v3/api/srf/production/videos-by-show-id?showId=42e39d12-e1e5-4f2e-b620-7db7e23c575c"),
          new CrawlerUrlDTO(
              "https://www.srf.ch/play/v3/api/srf/production/videos-by-show-id?showId=79fe1336-b513-4342-b389-001bf89b8ea2"),
        };

    final JsonElement jsonElement = JsonFileReader.readJson("/srf/srf_topics_page.json");

    final SrfTopicsDeserializer target = new SrfTopicsDeserializer();
    final Set<CrawlerUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
