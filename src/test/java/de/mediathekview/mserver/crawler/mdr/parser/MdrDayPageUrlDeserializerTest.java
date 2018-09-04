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
public class MdrDayPageUrlDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/mdr/mdr_day_index.html",
                7,
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180826_inheritancecontext-header_numberofelements-1_zc-4f024506.html"),
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180825_inheritancecontext-header_numberofelements-1_zc-c58dc472.html"),
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180824_inheritancecontext-header_numberofelements-1_zc-598a51a2.html"),
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180823_inheritancecontext-header_numberofelements-1_zc-bbd2a640.html"),
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180822_inheritancecontext-header_numberofelements-1_zc-dfcc73da.html"),
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180821_inheritancecontext-header_numberofelements-1_zc-659bb26c.html"),
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180820_inheritancecontext-header_numberofelements-1_zc-0d145a3a.html"),
                }
            },
            {
                "/mdr/mdr_day_index.html",
                3,
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180826_inheritancecontext-header_numberofelements-1_zc-4f024506.html"),
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180825_inheritancecontext-header_numberofelements-1_zc-c58dc472.html"),
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180824_inheritancecontext-header_numberofelements-1_zc-598a51a2.html"),
                    new CrawlerUrlDTO(MdrConstants.URL_BASE
                        + "/mediathek/fernsehen/sendung-verpasst--100_date-20180823_inheritancecontext-header_numberofelements-1_zc-bbd2a640.html"),
                }
            }
        });
  }

  private String htmlFile;
  private int maxDaysPast;
  private CrawlerUrlDTO[] expectedUrls;

  public MdrDayPageUrlDeserializerTest(final String aHtmlFile, final int aMaxDaysPast, final CrawlerUrlDTO[] aExpectedUrls) {

    htmlFile = aHtmlFile;
    maxDaysPast = aMaxDaysPast;
    expectedUrls = aExpectedUrls;
  }

  @Test
  public void test() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    MdrDayPageUrlDeserializer target = new MdrDayPageUrlDeserializer(MdrConstants.URL_BASE, maxDaysPast);
    Set<CrawlerUrlDTO> actual = target.deserialize(document);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}