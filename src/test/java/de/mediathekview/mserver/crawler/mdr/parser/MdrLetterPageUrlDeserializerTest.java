package de.mediathekview.mserver.crawler.mdr.parser;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.MdrConstants;
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

@RunWith(Parameterized.class)
public class MdrLetterPageUrlDeserializerTest {

  private final String htmlFile;
  private final CrawlerUrlDTO[] expectedUrls;

  public MdrLetterPageUrlDeserializerTest(
      final String aHtmlFile, final CrawlerUrlDTO[] aExpectedUrls) {

    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/mdr/mdr_topic_index.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-A_zc-81e206d8.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-B_zc-b220782a.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-C_zc-7b34540d.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-D_zc-742c7a73.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-E_zc-e225de76.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-F_zc-2545ea29.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-G_zc-908dfe4e.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-H_zc-bd71b385.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-I_zc-3a727df8.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-K_zc-84887216.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-L_zc-89d90630.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-M_zc-5f990448.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-N_zc-290536c6.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-O_zc-ee5929ea.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-P_zc-aaa8d669.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-Q_zc-123cc20a.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-R_zc-2e31c26d.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-S_zc-ef6bb75d.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-T_zc-4b6c71a8.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-U_zc-c6744604.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-V_zc-222fa751.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-W_zc-ead31467.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_letter-Z_zc-5ffa71f1.html#letternavi")
            }
          },
          {
            "/mdr/mdr_barrierefreiheit_gebaerdensprache.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-A_zc-81e206d8.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-B_zc-b220782a.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-C_zc-7b34540d.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-D_zc-742c7a73.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-E_zc-e225de76.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-F_zc-2545ea29.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-G_zc-908dfe4e.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-H_zc-bd71b385.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-I_zc-3a727df8.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-K_zc-84887216.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-L_zc-89d90630.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-M_zc-5f990448.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-N_zc-290536c6.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-O_zc-ee5929ea.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-P_zc-aaa8d669.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-R_zc-2e31c26d.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-S_zc-ef6bb75d.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-T_zc-4b6c71a8.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-U_zc-c6744604.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-V_zc-222fa751.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-W_zc-ead31467.html#letternavi"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/gebaerde-des-tages-sammlung-100_letter-Z_zc-5ffa71f1.html#letternavi")
            }
          }
        });
  }

  @Test
  public void test() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    final MdrLetterPageUrlDeserializer target =
        new MdrLetterPageUrlDeserializer(MdrConstants.URL_BASE);
    final Set<CrawlerUrlDTO> actual = target.deserialize(document);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
