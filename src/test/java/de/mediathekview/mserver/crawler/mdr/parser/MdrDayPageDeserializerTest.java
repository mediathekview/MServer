package de.mediathekview.mserver.crawler.mdr.parser;

import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.MdrConstants;
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
public class MdrDayPageDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/mdr/mdr_day_index.html",
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800322_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800324_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800326_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800330_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800332_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800338_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung801834_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800342_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung803136_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800346_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800348_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800350_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800352_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800354_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800356_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800358_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800362_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800364_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800366_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800368_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                    new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800370_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html"),
                  new CrawlerUrlDTO("https://www.mdr.de/mediathek/fernsehen/sendung800372_date-20180826_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-4d7ac84d_zs-1638fa4e.html")
                }
            }});
  }

  private String htmlFile;
  private CrawlerUrlDTO[] expectedUrls;

  public MdrDayPageDeserializerTest(final String aHtmlFile, final CrawlerUrlDTO[] aExpectedUrls) {

    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
  }

  @Test
  public void test() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    MdrDayPageDeserializer target = new MdrDayPageDeserializer(MdrConstants.URL_BASE);
    Set<CrawlerUrlDTO> actual = target.deserialize(document);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
