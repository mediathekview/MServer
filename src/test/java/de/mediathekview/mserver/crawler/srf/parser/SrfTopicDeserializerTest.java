package de.mediathekview.mserver.crawler.srf.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Test;

public class SrfTopicDeserializerTest {
  @Test
  public void test() {
    final CrawlerUrlDTO[] expectedUrls =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://www.srf.ch/play/v3/api/srf/production/video?id=342a1a95-42ec-4568-b653-a042c54f7763"),
          new CrawlerUrlDTO(
              "https://www.srf.ch/play/v3/api/srf/production/video?id=aa44e19a-19d9-4584-88c7-ccd8658c0828"),
          new CrawlerUrlDTO(
              "https://www.srf.ch/play/v3/api/srf/production/video?id=eba528a3-d85c-4936-af35-bca5f2b99960"),
          new CrawlerUrlDTO(
              "https://www.srf.ch/play/v3/api/srf/production/video?id=5716149f-5edd-4205-b7d8-60f23334af7e")
        };

    final JsonElement jsonElement = JsonFileReader.readJson("/srf/srf_topic_page1.json");

    final SrfTopicDeserializer target = new SrfTopicDeserializer();
    final PagedElementListDTO<CrawlerUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.getNextPage(), equalTo(Optional.of("")));
    assertThat(actual.getElements(), Matchers.containsInAnyOrder(expectedUrls));
  }
}
