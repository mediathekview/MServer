package de.mediathekview.mserver.crawler.swr.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.parser.MdrTopic;
import de.mediathekview.mserver.crawler.swr.SwrConstants;
import de.mediathekview.mserver.testhelper.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SwrTopicPageDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/swr/swr_topic_page1.htm",
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=fadea760-a886-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=ce8074e0-a882-11e8-8218-005056a10824"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=e16a7540-a884-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=ce81ad60-a882-11e8-8218-005056a10824"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=fadd20c0-a886-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=c48b1d71-9d86-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=c48b1d70-9d86-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=4c21cc00-9802-11e8-b070-005056a12b4c")
                },
                Optional.of(new CrawlerUrlDTO("https://swrmediathek.de/tvshow.htm?show=bacfa550-9d4c-11df-8bd1-00199916cf68&pc=1"))
            },
            {
                "/swr/swr_topic_page_last.htm",
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=7eca57c0-d78d-11e7-a5ff-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=012355b0-d783-11e7-a5ff-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=f7481763-bd5b-11e7-a5ff-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=f17a9ee0-bbfb-11e7-a5ff-005056a12b4c")
                },
                Optional.empty()
            },
            {
                "/swr/swr_topic_no_entries.htm",
                new CrawlerUrlDTO[0],
                Optional.empty()
            }
        });
  }

  private String htmlFile;
  private CrawlerUrlDTO[] expectedUrls;
  private Optional<CrawlerUrlDTO> expectedNextPage;

  public SwrTopicPageDeserializerTest(final String aHtmlFile, final CrawlerUrlDTO[] aExpectedUrls,
      final Optional<CrawlerUrlDTO> aExpectedNextPage) {

    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
    expectedNextPage = aExpectedNextPage;
  }

  @Test
  public void test() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    SwrTopicPageDeserializer target = new SwrTopicPageDeserializer(SwrConstants.URL_BASE);
    MdrTopic actual = target.deserialize(document);

    assertThat(actual, notNullValue());
    assertThat(actual.getNextPage(), equalTo(expectedNextPage));
    assertThat(actual.getFilmUrls(), Matchers.containsInAnyOrder(expectedUrls));
  }
}