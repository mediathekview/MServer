package de.mediathekview.mserver.crawler.dreisat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
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
public class DreisatDayPageHtmlDeserializerTest {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
      new Object[][]{
        {
          "/dreisat/dreisat_day_page1.html",
          new CrawlerUrlDTO[]{
            new CrawlerUrlDTO(
              "https://api.3sat.de/content/documents/sendung-vom-20-mai-2020-100.json"),
            new CrawlerUrlDTO(
              "https://api.3sat.de/content/documents/200520-sendung-100.json"),
            new CrawlerUrlDTO(
              "https://api.3sat.de/content/documents/sehen-statt-hoeren-vom-22-mai-2020-100.json"),
            new CrawlerUrlDTO(
              "https://api.3sat.de/content/documents/7-tage-auf-dem-jakobsweg-100.json"),
            new CrawlerUrlDTO(
              "https://api.3sat.de/content/documents/natur-pur-100.json"),
            new CrawlerUrlDTO(
              "https://api.3sat.de/content/documents/die-macht-der-vulkane-jahre-ohne-sommer-102.json"),
            new CrawlerUrlDTO(
              "https://api.3sat.de/content/documents/die-macht-der-vulkane-im-schatten-der-feuerberge-102.json"),
            new CrawlerUrlDTO(
              "https://api.3sat.de/content/documents/wilder-planet-wenn-die-erde-verrueckt-spielt-100.json"),
            new CrawlerUrlDTO("https://api.3sat.de/content/documents/wilder-planet-erdbeben-100.json"),
            new CrawlerUrlDTO(
              "https://api.3sat.de/content/documents/wilder-planet-vulkane-100.json"),
            new CrawlerUrlDTO("https://api.3sat.de/content/documents/fantastische-phaenomene1-104.json"),
            new CrawlerUrlDTO("https://api.3sat.de/content/documents/fantastische-phaenomene2-104.json"),
            new CrawlerUrlDTO("https://api.3sat.de/content/documents/200522-sendung-nano-102.json"),
            new CrawlerUrlDTO("https://api.3sat.de/content/documents/sendung-vom-22-mai-2020-100.json"),
            new CrawlerUrlDTO("https://api.3sat.de/content/documents/million-dollar-baby-102.json"),
            new CrawlerUrlDTO("https://api.3sat.de/content/documents/pufpaffs-happy-hour-folge-59-100.json"),
            new CrawlerUrlDTO("https://api.3sat.de/content/documents/olaf-schubert-sexy-forever-100.json"),
            new CrawlerUrlDTO("https://api.3sat.de/content/documents/miss-allie-meine-herz-und-die-toilette-100.json"),
            new CrawlerUrlDTO("https://api.3sat.de/content/documents/nessi-tausendschoen-30-jahre-zenit-104.json")
          }
        }
      });
  }

  private final DreisatDayPageHtmlDeserializer target;
  private final String htmlFile;
  private final CrawlerUrlDTO[] expectedEntries;

  public DreisatDayPageHtmlDeserializerTest(
    final String htmlFile, final CrawlerUrlDTO[] expectedEntries) {
    target = new DreisatDayPageHtmlDeserializer(DreisatConstants.URL_API_BASE);

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
