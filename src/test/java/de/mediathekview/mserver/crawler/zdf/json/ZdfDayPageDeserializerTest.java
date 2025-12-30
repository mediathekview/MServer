package de.mediathekview.mserver.crawler.zdf.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ZdfDayPageDeserializerTest {

  private final ZdfDayPageDeserializer target;
  private final String jsonFile;
  private final CrawlerUrlDTO[] expectedEntries;
  private final Optional<String> expectedNextPageUrl;

  public ZdfDayPageDeserializerTest(
      final String aJsonFile,
      final CrawlerUrlDTO[] aExpectedEntries,
      final Optional<String> aExpectedNextPageUrl) {
    target = new ZdfDayPageDeserializer(ZdfConstants.URL_API_BASE);

    jsonFile = aJsonFile;
    expectedEntries = aExpectedEntries;
    expectedNextPageUrl = aExpectedNextPageUrl;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/zdf/zdf_day_page_single.json",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/olympia-im-technikwahn-100.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/gestrandet-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/menschen---das-magazin-vom-24-februar-2018-100.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-orakel-krake-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/siegerehrung-maenner-staffel-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/siegerehrung-vom-parallelslalom-der-frauen-100.json")
            },
            Optional.empty()
          },
          {
            "/zdf/zdf_day_page_multiple1.json",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/plan-b-die-multi-kulti-macher-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/grippewelle-weitet-sich-aus-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/lausitz-fuerchtet-wirtschaftlichen-ruin-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/einblick-in-bayerns-heimatministerium-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/neonazis-auf-dem-rueckzug-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/hammer-der-woche-glasfaserkabel-doppelt-verlegt-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/laenderspiegel-vom-24-februar-2018-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/deutschlandreise-nach-pellworm-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/christian-ehrhoff-fahnentraeger-bei-schlussfeier-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/solange-du-wild-bist-102.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/leons-hoehenflug-102.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/porsche-modellauto-100.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/silberbecher-106.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/halskette-102.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/petroleumleuchter-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/bares-fuer-rares-vom-27-september-2016-102.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/brillantring-126.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/leica-kamera-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/snowboard-parallel-riesenslalom-in-der-zusammenfassung-100.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/nick-raeumt-auf-100.json")
            },
            Optional.of(
                "http://localhost:8589"
                    + "/search/documents?hasVideo=true&q=*&types=page-video&sortOrder=desc&from=2018-02-24T12%3A00%3A00.000%2B01%3A00&sortBy=date&to=2018-02-24T18%3A00%3A00.878%2B01%3A00&page=2")
          }
        });
  }

  @Test
  public void deserializeTest() {
    final JsonObject json = JsonFileReader.readJson(jsonFile);

    final ZdfDayPageDto actual = target.deserialize(json, ZdfDayPageDto.class, null);

    assertThat(actual, notNullValue());
    assertThat(actual.getNextPageUrl(), equalTo(expectedNextPageUrl));
    assertThat(actual.getEntries().size(), equalTo(expectedEntries.length));
    actual.getEntries().stream().map(v->new CrawlerUrlDTO(v.getUrl())).toArray(CrawlerUrlDTO[]::new);
    assertThat(actual.getEntries().stream().map(v->new CrawlerUrlDTO(v.getUrl())).toList(), Matchers.containsInAnyOrder(expectedEntries));
  }
}
