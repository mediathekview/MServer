package de.mediathekview.mserver.crawler.swr.parser;

import static org.hamcrest.MatcherAssert.assertThat;

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
public class SwrDayPageDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/swr/swr_day_page.html",
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=e5ed7f00-b28d-11e8-893a-005056a10824"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=e5efa1e0-b28d-11e8-893a-005056a10824"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=90a40732-b4d6-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=92f8c380-aa8c-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=92f938b0-aa8c-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=92f9ade0-aa8c-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=92f95fc0-aa8c-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=92f911a0-aa8c-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=f0a34c00-b651-11e8-893a-005056a10824"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=0dd22640-b682-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=0dd2c280-b682-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=2ce60310-b684-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=26e4fe80-b684-11e8-893a-005056a10824"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=ed8fc670-b67d-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=fa3cc020-9827-11e5-8281-0026b975f2e6"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=8f70ca10-f36c-11e6-9102-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=24cd3580-b699-11e8-893a-005056a10824"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=5ae44470-b69d-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=099f4570-b697-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=4ddf3520-b69b-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=a68ec8e0-b6a3-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=a69139e0-b6a3-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=5ad43b30-97c1-11e7-a5ff-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=10c6fe90-9c46-11e7-a5ff-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=a061d7e0-9936-11e7-a5ff-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=ced52930-b6a7-11e8-893a-005056a10824"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=ced96ef0-b6a7-11e8-893a-005056a10824"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=a7c3db90-b6a5-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=1d1f49a0-b6ac-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=cedbdff0-b6a7-11e8-893a-005056a10824"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=69aacd52-b6b4-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=69aaf460-b6b4-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=69aa3110-b6b4-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=125adeb0-b6ae-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=e017e230-b6a9-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=2c822730-b4ce-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=444f4600-b6b2-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=44505770-b6b2-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=2b80a2b0-b6b0-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=2b800670-b6b0-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=1e5ebc90-b6ae-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=bcdd80a0-b6bc-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=69a8aa70-b6b4-11e8-b070-005056a12b4c"),
                    new CrawlerUrlDTO("https://swrmediathek.de/AjaxEntry?ekey=69a946b2-b6b4-11e8-b070-005056a12b4c")
                }
            }});
  }

  private String htmlFile;
  private CrawlerUrlDTO[] expectedUrls;

  public SwrDayPageDeserializerTest(final String aHtmlFile, final CrawlerUrlDTO[] aExpectedUrls) {

    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
  }

  @Test
  public void test() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    SwrDayPageDeserializer target = new SwrDayPageDeserializer(SwrConstants.URL_BASE);
    Set<CrawlerUrlDTO> actual = target.deserialize(document);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
