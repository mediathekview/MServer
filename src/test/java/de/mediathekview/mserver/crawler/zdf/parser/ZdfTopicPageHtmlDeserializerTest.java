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
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/opferzahl-100.json"),
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
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/kudamm-56-teil-1-102.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/kudamm-56-teil-2-102.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/kudamm-56-teil-3-102.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/kudamm-59-104.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/kudamm-59-106.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/kudamm-59-108.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/der-kurztrailer-zu-kudamm-59-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/kudamm-63-die-dokumentation-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/wer-ist-wer-bei-kudamm-100.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/kudamm-63-106.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/tanzclip-mit-maria-ehrich-und-giovanni-funiati-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/studiotour-mit-sabin-tambrea-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/kudamm59-kurztrailer-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/kudamm59-sonjagerhardt-emiliaschuele-mariaehrich-backstage-ueber-ihre-rollen-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/kudamm59-sonjagerhardt-trystanpuetter-musik-tanz-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/kudamm59-maria-ehrich-interview-maske-kostuem-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/kudamm59-sonjagerhardt-set-tour-102.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/visuelle-effekte-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/der-kurztrailer-zu-kudamm-56-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-50er-das-war-komplett-anders-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/als-eine-generation-das-tanzen-lernte-106.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/eine-nahe-ferne-zeit-106.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/schoenste-drehtage-kudamm63-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/liebe-unter-maennern-august-wittgenstein-ueber-seine-figur-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/claudia-michelsen-ueber-die-beziehung-von-muettern-und-toechtern-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/volle-kanne-vom-19-maerz-2021-mit-august-wittgenstein-100.json"),
            }
          },
          {
            "/zdf/zdf_topic_page_with_single_clips.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-anstalt-vom-22-juni-2021-100.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-2-190.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-7-194.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-6-190.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-3-190.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-5-190.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-1-190.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-4-190.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-anstalt-clip-10-156.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-7-192.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-4-188.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-2-188.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-8-190.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-9-188.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-6-188.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-anstalt-clip-1-188.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-anstalt-vom-4-mai-2021-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-anstalt-vom-16-maerz-2021-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-anstalt-vom-2-februar-2021-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-anstalt-vom-8-dezember-2020-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-anstalt-vom-3-november-2020-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-anstalt-vom-29-september-2020-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-anstalt-vom-14-juli-2020-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/die-gaeste-am-20-juli-2021-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/fakten-im-check-der-anstalt-118.json")
            }
          },
          {
            "/zdf/zdf_topic_page_with_main_video.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/aspekte-vom-11-juni-2021-100.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/bestenliste-104.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/das-neue-dienstleistungsproletariat-schoene-neue-arbeitswelt-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/new-work-neue-arbeitswelt-transparenz-teilhabe-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/co-working-auf-dem-land-wie-leben-und-arbeiten-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/homeoffice-freiheit-und-belastung-notloesung-oder-zukunft-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/digitaler-wandel-im-theater-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/paris-zugabe-jeremias-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/jeremias-golden-hour-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/freiheit-kampf-um-grundrechte-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/buecher-fruehling-literatur-ostdeutschland-100.json"),
            }
          },
          {
            "/zdf/zdf_topic_page1.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/bonustrailer-zur-dritten-staffel-100.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/tonio--julia---dem-himmel-so-nah-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/tonio--julia---mut-zu-leben-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/rueckblick-auf-die-zweite-staffel-102.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/volle-kanne---service-taeglich-vom-9-oktober-2020-mit-oona-devi-liebich-100.json")
            }
          },
          {
            "/zdf/zdf_topic_page_reload.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/filmgorillas-290.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/filmgorillas-288.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/filmgorillas-286.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/filmgorillas-284.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/filmgorillas-282.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/filmgorillas-280.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/filmgorillas-278.json"),
              new CrawlerUrlDTO("https://api.zdf.de/content/documents/filmgorillas-276.json"),
              new CrawlerUrlDTO(
                  "https://api.zdf.de/content/documents/auftritt-filmgorillas-100.json")
            }
          }
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
