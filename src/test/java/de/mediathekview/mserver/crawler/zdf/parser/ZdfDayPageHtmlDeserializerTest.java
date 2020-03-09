package de.mediathekview.mserver.crawler.zdf.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.testhelper.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ZdfDayPageHtmlDeserializerTest {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/zdf/zdf_day_page.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/zdf-morgenmagazin-vom-10-februar-2020-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/raetselhafte-tote-der-ermordete-pharao-104.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/toedliche-vergangenheit-106.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/dinner-date-vom-23-oktober-2019-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/the-true-story-of-madonna-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-80er-das-explosive-jahrzehnt-kalter-krieg-und-heisser-rock-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/long-live-the-queen-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-80er-das-explosive-jahrzehnt-superstars-und-supergau-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-80er-das-explosive-jahrzehnt-1987-89-endzeitangst-und-mauerfall-100.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/bad-banks-die-doku-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/despoten-kim-jong-il-dynastie-des-teufels-102.json")
            }
          }
        });
  }

  private final ZdfDayPageHtmlDeserializer target;
  private final String htmlFile;
  private final CrawlerUrlDTO[] expectedEntries;

  public ZdfDayPageHtmlDeserializerTest(
      final String htmlFile, final CrawlerUrlDTO[] expectedEntries) {
    target = new ZdfDayPageHtmlDeserializer(ZdfConstants.URL_API_BASE);

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
