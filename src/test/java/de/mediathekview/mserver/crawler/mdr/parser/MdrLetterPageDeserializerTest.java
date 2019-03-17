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
public class MdrLetterPageDeserializerTest {

    private String htmlFile;
    private CrawlerUrlDTO[] expectedUrls;

    public MdrLetterPageDeserializerTest(
            final String aHtmlFile, final CrawlerUrlDTO[] aExpectedUrls) {

        htmlFile = aHtmlFile;
        expectedUrls = aExpectedUrls;
    }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
            new Object[][]{
                    {
                            "/mdr/mdr_topic_index.html",
                            new CrawlerUrlDTO[]{
                                    new CrawlerUrlDTO(
                                            "https://www.mdr.de/mediathek/fernsehen/a-z/akteex100_zc-ca8ec3f4_zs-73445a6d.html"),
                                    new CrawlerUrlDTO(
                                            "https://www.mdr.de/mediathek/fernsehen/a-z/allesklara106_zc-ca8ec3f4_zs-73445a6d.html"),
                                    new CrawlerUrlDTO(
                                            "https://www.mdr.de/mediathek/fernsehen/a-z/alleswirdwieneusein100_zc-ca8ec3f4_zs-73445a6d.html"),
                                    new CrawlerUrlDTO(
                                            "https://www.mdr.de/mediathek/fernsehen/a-z/artour100_zc-ca8ec3f4_zs-73445a6d.html"),
                                    new CrawlerUrlDTO(
                                            "https://www.mdr.de/mediathek/fernsehen/a-z/aufschmalerspur100_zc-ca8ec3f4_zs-73445a6d.html"),
                                    new CrawlerUrlDTO(
                                            "https://www.mdr.de/mediathek/fernsehen/a-z/auenseiterspitzenreiter100_zc-ca8ec3f4_zs-73445a6d.html"),
                            }
                    }
            });
  }

  @Test
  public void test() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    MdrLetterPageDeserializer target = new MdrLetterPageDeserializer(MdrConstants.URL_BASE);
    Set<CrawlerUrlDTO> actual = target.deserialize(document);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
