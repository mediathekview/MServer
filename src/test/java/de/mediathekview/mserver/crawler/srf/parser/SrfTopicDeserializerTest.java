package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SrfTopicDeserializerTest {
  @Test
  public void test() {
    final CrawlerUrlDTO[] expectedUrls =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/urn:srf:video:342a1a95-42ec-4568-b653-a042c54f7763.json"),
          new CrawlerUrlDTO(
              "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/urn:srf:video:aa44e19a-19d9-4584-88c7-ccd8658c0828.json"),
          new CrawlerUrlDTO(
              "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/urn:srf:video:eba528a3-d85c-4936-af35-bca5f2b99960.json"),
          new CrawlerUrlDTO(
              "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/urn:srf:video:5716149f-5edd-4205-b7d8-60f23334af7e.json")
        };

    final JsonElement jsonElement = JsonFileReader.readJson("/srf/srf_topic_page1.json");

    final SrfTopicDeserializer target = new SrfTopicDeserializer();
    final PagedElementListDTO<CrawlerUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.getNextPage(), equalTo(Optional.empty()));
    assertThat(actual.getElements(), Matchers.containsInAnyOrder(expectedUrls));
  }

  @Test
  public void testWithNextPage() {
    final CrawlerUrlDTO[] expectedUrls =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/urn:srf:video:a798a1a5-8808-426d-a47a-7a95b16650ea.json"),
          new CrawlerUrlDTO(
              "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/urn:srf:video:2042ad51-80b7-4a98-808f-a7bf447c8756.json")
        };

    final JsonElement jsonElement = JsonFileReader.readJson("/srf/srf_topic_page_with_next.json");

    final SrfTopicDeserializer target = new SrfTopicDeserializer();
    final PagedElementListDTO<CrawlerUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(
        actual.getNextPage(),
        equalTo(
            Optional.of(
                "09e12b6f403c2da8bfde15a1c99070d421a97f0c37210dbda272ccf5a386e3099d6a236a2a96941b339f13d41a24eeb58d4a7c6cbc5220170ed2ecbc81f080465303ba40356017efbefb7efe95be9f1ef63b0216702f1b09a6094e6a7d2631f9f40f0d2e1ca431002db278684a8e97b7a82219c4c83769ba9ad8dd65d4c6f19620b837f9a1ef66fda473f7212a326361da7e4b3b8386668475ef72e3ad347396d147878b2d7408856a23cf20af8180946bf36c00af3044485321237fa36db791a80f46df2744fd825ddf82fba29e6f0e02e70e5c4d0aa422de2aa86c7e3bf98e0502fcbae23341621e5edde21c9471f8f3ed52b5ce30a6dfa66b2c4aca2ff51ea7ed010bf147e87253a7a5123b8e0ba835ebeb15d27362d1748f0e6287c845818e03431d2a13d2bcad2e2b40ed63a03bb5ed2a5a64c874a2")));
    assertThat(actual.getElements(), Matchers.containsInAnyOrder(expectedUrls));
  }
}
