package de.mediathekview.mserver.crawler.zdf.parser;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.testhelper.FileReader;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class ZdfTopicPageHtmlDeserializerTest {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/zdf/zdf_topic_page.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/arne-dahl-ungeschoren-104.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/arne-dahl-totenmesse-104.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/arne-dahl-dunkelziffer-104.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/opferzahl-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/arne-dahl-bussestunde-110.json")
            }
          },
          {
            "/zdf/zdf_topic_page_staffeln.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/kudamm-63-100.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/kudamm-63-102.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/kudamm-63-104.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/der-kurztrailer-zu-kudamm-56-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-50er-das-war-komplett-anders-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/als-eine-generation-das-tanzen-lernte-106.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/eine-nahe-ferne-zeit-106.json")
            }
          }
          // TODO with single clips
          // TODO mit nachladen, evtl. Sportstudio
          // TODO aspekte: hauptsendung fehlt
                // TODO heldt: funktioniert nicht
        });
  }

  private final ZdfTopicPageHtmlDeserializer target;
  private final String htmlFile;
  private final CrawlerUrlDTO[] expectedEntries;

  public ZdfTopicPageHtmlDeserializerTest(
      final String htmlFile, final CrawlerUrlDTO[] expectedEntries) {
    target = new ZdfTopicPageHtmlDeserializer(ZdfConstants.URL_API_BASE);

    this.htmlFile = htmlFile;
    this.expectedEntries = expectedEntries;
  }

  @Test
  public void deserializeTest() {
    final Document document = Jsoup.parse(FileReader.readFile(htmlFile));

    final Set<CrawlerUrlDTO> actual = target.deserialize(document);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expectedEntries.length));
    assertThat(actual, Matchers.containsInAnyOrder(expectedEntries));
  }
}
