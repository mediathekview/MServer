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
public class MdrLetterPageUrlDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/mdr/mdr_topic_index.html",
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-A_numberofelements-1_zc-ef89b6fa.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-B_numberofelements-1_zc-84a1e244.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-C_numberofelements-1_zc-db46c87d.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-D_numberofelements-1_zc-0352e461.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-E_numberofelements-1_zc-89d157fe.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-F_numberofelements-1_zc-8c17df0a.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-G_numberofelements-1_zc-c5153dc7.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-H_numberofelements-1_zc-06d67d07.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-I_numberofelements-1_zc-38940788.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-K_numberofelements-1_zc-e45bab41.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-L_numberofelements-1_zc-1be931e3.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-M_numberofelements-1_zc-5d83c3ef.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-N_numberofelements-1_zc-3c216798.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-O_numberofelements-1_zc-2bfb0a35.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-P_numberofelements-1_zc-5f53f4ab.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-Q_numberofelements-1_zc-11f988f8.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-R_numberofelements-1_zc-974c7492.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-S_numberofelements-1_zc-727dc70f.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-T_numberofelements-1_zc-241c1f6b.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-U_numberofelements-1_zc-16901548.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-V_numberofelements-1_zc-8cfa116f.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-W_numberofelements-1_zc-04b7218f.html#letternavi"),
                    new CrawlerUrlDTO(
                        "https://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-Z_numberofelements-1_zc-31352588.html#letternavi")
                }
            }});
  }

  private String htmlFile;
  private CrawlerUrlDTO[] expectedUrls;

  public MdrLetterPageUrlDeserializerTest(final String aHtmlFile, final CrawlerUrlDTO[] aExpectedUrls) {

    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
  }

  @Test
  public void test() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    MdrLetterPageUrlDeserializer target = new MdrLetterPageUrlDeserializer(MdrConstants.URL_BASE);
    Set<CrawlerUrlDTO> actual = target.deserialize(document);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}