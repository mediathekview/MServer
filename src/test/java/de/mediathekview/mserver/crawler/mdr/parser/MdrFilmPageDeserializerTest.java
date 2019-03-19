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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class MdrFilmPageDeserializerTest {

    private String htmlFile;
    private CrawlerUrlDTO[] expectedFilmEntries;

    public MdrFilmPageDeserializerTest(
            final String aHtmlFile, final CrawlerUrlDTO[] aExpectedFilmEntries) {
        htmlFile = aHtmlFile;
        expectedFilmEntries = aExpectedFilmEntries;
    }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
            new Object[][]{
                    {
                            "/mdr/mdr_film_simple.html",
                            new CrawlerUrlDTO[]{
                                    new CrawlerUrlDTO(
                                            MdrConstants.URL_BASE + "/mediathek/fernsehen/video-226902-avCustom.xml")
                            }
                    },
                    {
                            "/mdr/mdr_film_with_entries.html",
                            new CrawlerUrlDTO[]{
                                    new CrawlerUrlDTO(
                                            MdrConstants.URL_BASE + "/mediathek/fernsehen/a-z/video-227446-avCustom.xml"),
                                    new CrawlerUrlDTO(
                                            MdrConstants.URL_BASE + "/mediathek/fernsehen/a-z/video-227420-avCustom.xml"),
                                    new CrawlerUrlDTO(
                                            MdrConstants.URL_BASE + "/mediathek/fernsehen/a-z/video-227440-avCustom.xml"),
                                    new CrawlerUrlDTO(
                                            MdrConstants.URL_BASE + "/mediathek/fernsehen/a-z/video-227432-avCustom.xml")
                            }
                    },
                    {
                            "/mdr/mdr_film_with_ad.html",
                            new CrawlerUrlDTO[]{
                                    new CrawlerUrlDTO(
                                            MdrConstants.URL_BASE + "/mediathek/fernsehen/video-224746-avCustom.xml"),
                                    new CrawlerUrlDTO(
                                            MdrConstants.URL_BASE + "/mediathek/fernsehen/video-224960-avCustom.xml")
                            }
                    },
                    {
                            "/mdr/mdr_film_with_sorbisch.html",
                            new CrawlerUrlDTO[]{
                                    new CrawlerUrlDTO(
                                            MdrConstants.URL_BASE + "/mediathek/fernsehen/a-z/video-227164-avCustom.xml"),
                                    new CrawlerUrlDTO(
                                            MdrConstants.URL_BASE + "/mediathek/fernsehen/a-z/video-223854-avCustom.xml")
                            }
                    }
        });
  }

  @Test
  public void test() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    MdrFilmPageDeserializer target = new MdrFilmPageDeserializer(MdrConstants.URL_BASE);
    Set<CrawlerUrlDTO> actual = target.deserialize(document);

    assertThat(actual.size(), equalTo(expectedFilmEntries.length));
    assertThat(actual, Matchers.containsInAnyOrder(expectedFilmEntries));
  }
}
