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
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class MdrTopicPageDeserializerTest {

  private final String htmlFile;
  private final CrawlerUrlDTO[] expectedUrls;
  private final Optional<CrawlerUrlDTO> expectedNextPage;

  public MdrTopicPageDeserializerTest(
      final String aHtmlFile,
      final CrawlerUrlDTO[] aExpectedUrls,
      final Optional<CrawlerUrlDTO> aExpectedNextPage) {

    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
    expectedNextPage = aExpectedNextPage;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/mdr/mdr_topic.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung782266_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung771956_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung771904_ipgctx-false_zc-ba8902b5_zs-73445a6d.html")
            },
            Optional.of(
                new CrawlerUrlDTO(
                    "https://www.mdr.de/tv/programm/aufschmalerspur100_allUrl-true_zc-c7a224ef.html"))
          },
          {
            "/mdr/mdr_topic_with_next_pages.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung793004_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung790080_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung783408_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung788706_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung786156_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung783014_ipgctx-false_zc-ba8902b5_zs-73445a6d.html")
            },
            Optional.of(
                new CrawlerUrlDTO(
                    "https://www.mdr.de/tv/programm/tierischtierisch100_allUrl-true_zc-c7a224ef.html"))
          },
          {
            "/mdr/mdr_topic_last_page.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung761586_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung761004_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung759390_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung759028_ipgctx-false_zc-ba8902b5_zs-73445a6d.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/mediathek/fernsehen/a-z/sendung757768_ipgctx-false_zc-ba8902b5_zs-73445a6d.html")
            },
            Optional.empty()
          },
          {
            "/mdr/mdr_barrierefreiheit_gebaerdensprache.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/video-74128.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/video-34600.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/video-84558.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/video-35110.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/video-43712.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/video-73574.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/video-47226.html"),
              new CrawlerUrlDTO(
                  "https://www.mdr.de/barrierefreiheit/gebaerdensprache/video-80998.html")
            },
            Optional.empty()
          }
        });
  }

  @Test
  public void test() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    final MdrTopicPageDeserializer target = new MdrTopicPageDeserializer(MdrConstants.URL_BASE);
    final MdrTopic actual = target.deserialize(document);

    assertThat(actual, notNullValue());
    assertThat(actual.getNextPage(), equalTo(expectedNextPage));
    assertThat(actual.getFilmUrls(), Matchers.containsInAnyOrder(expectedUrls));
  }
}
