package de.mediathekview.mserver.crawler.zdf.parser;

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
public class ZdfLetterListHtmlDeserializerTest {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/zdf/zdf_topics_page1.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO("https://www.zdf.de/sendungen-a-z?group=a"),
              new CrawlerUrlDTO("https://www.zdf.de/sendungen-a-z?group=b"),
              new CrawlerUrlDTO("https://www.zdf.de/sendungen-a-z?group=c"),
              new CrawlerUrlDTO("https://www.zdf.de/sendungen-a-z?group=d"),
              new CrawlerUrlDTO("https://www.zdf.de/sendungen-a-z?group=e"),
              new CrawlerUrlDTO("https://www.zdf.de/sendungen-a-z?group=z"),
              new CrawlerUrlDTO("https://www.zdf.de/sendungen-a-z?group=0 - 9")
            }
          }
        });
  }

  private final ZdfLetterListHtmlDeserializer target;
  private final String htmlFile;
  private final CrawlerUrlDTO[] expectedEntries;

  public ZdfLetterListHtmlDeserializerTest(
      final String htmlFile, final CrawlerUrlDTO[] expectedEntries) {
    target = new ZdfLetterListHtmlDeserializer();

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
