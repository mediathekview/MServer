package de.mediathekview.mserver.crawler.swr.parser;

import static org.junit.Assert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.swr.SwrConstants;
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
public class SwrTopicsDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/swr/swr_topics.htm",
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=3d40c3b0-f02c-11e5-a804-0026b975f2e6"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=581b2cd0-43f3-11e8-abc2-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=467c27d0-53fa-11e6-a659-0026b975e0ea"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=08b34490-9733-11e4-8229-0026b975f2e6"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=f6422cf0-a4eb-11e5-abd4-0026b975e0ea"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=22844460-9b63-11e6-8e1e-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=f642f042-a4eb-11e5-abd4-0026b975e0ea"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=9f66ad40-e8e7-11df-8971-0026b975f2e6"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=c20a4e90-9bdc-11df-b44d-00199916cf68"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=98af6b90-85dc-11e4-8515-0026b975f2e6"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=b94dd550-80c4-11e6-aaed-0026b975e0ea"),
                    new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=6f713a10-430f-11e5-8d3d-0026b975f2e6")
                }
            }});
  }

  private String htmlFile;
  private CrawlerUrlDTO[] expectedUrls;

  public SwrTopicsDeserializerTest(final String aHtmlFile, final CrawlerUrlDTO[] aExpectedUrls) {

    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
  }

  @Test
  public void test() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    SwrTopicsDeserializer target = new SwrTopicsDeserializer(SwrConstants.URL_BASE);
    Set<CrawlerUrlDTO> actual = target.deserialize(document);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}