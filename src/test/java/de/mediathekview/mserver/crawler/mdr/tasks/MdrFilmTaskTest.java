package de.mediathekview.mserver.crawler.mdr.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.MdrCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockRunnerDelegate(Parameterized.class)
@PowerMockIgnore(value= {"javax.net.ssl.*","com.sun.org.apache.xerces.*","javax.management.*", "com.sun.*", "javax.xml.*", "org.xml.*","org.apache.logging.log4j.core.config.xml.*"})
public class MdrFilmTaskTest extends WireMockTestBase {

  private String requestUrl;
  private String htmlFile;
  private String xmlUrl;
  private String xmlFile;
  private String expectedTopic;
  private String expectedTitle;
  private String expectedDescription;
  private LocalDateTime expectedTime;
  private Duration expectedDuration;
  private String expectedWebsite;
  private String expectedUrlSmall;
  private String expectedUrlNormal;
  private String expectedUrlHd;
  private String expectedSubtitle;
  private GeoLocations expectedGeoLocation;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "https://www.mdr.de/mediathek/fernsehen/sendung803394_date-20180902_inheritancecontext-header_ipgctx-false_numberofelements-1_zc-abd053ed_zs-1638fa4e.html",
                "/mdr/mdr_film_simple.html",
                "/mediathek/fernsehen/video-226902-avCustom.xml",
                "/mdr/mdr_film_info.xml",
                "Musik & Show",
                "Ein Abend für Marianne Kiefer",
                "Als Paula Zipfel und Olga Knopf wurde sie berühmt: Marianne Kiefer. In vielen Lustspielen war die nur 1,54 große Künstlerin die Partnerin von Publikumslieblingen wie Herbert Köfer, Heinz Rennhack oder Ingeborg Krabbe.",
                LocalDateTime.of(2018, 9, 2, 20, 15, 0),
                Duration.ofHours(1).plusMinutes(29).plusSeconds(29),
                "https://www.mdr.de/mediathek/fernsehen/video-226902_zc-7748e51b_zs-1638fa4e.html",
                "https://odmdr-a.akamaihd.net/mp4dyn2/1/FCMS-1d15c65d-aca1-4c75-88a6-fd6589ad3741-9a4bb04739be_1d.mp4",
                "https://odmdr-a.akamaihd.net/mp4dyn2/1/FCMS-1d15c65d-aca1-4c75-88a6-fd6589ad3741-730aae549c28_1d.mp4",
                "https://odmdr-a.akamaihd.net/mp4dyn2/1/FCMS-1d15c65d-aca1-4c75-88a6-fd6589ad3741-be7c2950aac6_1d.mp4",
                "https://www.mdr.de/mediathek/mdr-videos/b/video-226902-videoSubtitle.xml",
                GeoLocations.GEO_NONE
            }
        });
  }

  public MdrFilmTaskTest(final String aRequestUrl, final String aHtmlFile, final String aXmlUrl, final String aXmlFile,
      final String aExpectedTopic, final String aExpectedTitle, final String aExpectedDescription,
      final LocalDateTime aExpectedTime, final Duration aExpectedDuration,
      final String aExpectedWebsite, final String aExpectedUrlSmall, final String aExpectedUrlNormal, final String aExpectedUrlHd,
      final String aExpectedSubtitle, GeoLocations aExpectedGeoLocation) {

    requestUrl = aRequestUrl;
    htmlFile = aHtmlFile;
    xmlUrl = aXmlUrl;
    xmlFile = aXmlFile;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedDescription = aExpectedDescription;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedWebsite = aExpectedWebsite;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedSubtitle = aExpectedSubtitle;
    expectedGeoLocation = aExpectedGeoLocation;
  }

  @Test
  public void test() throws IOException {
    JsoupMock.mock(requestUrl, htmlFile);
    setupSuccessfulXmlResponse(xmlUrl, xmlFile);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final MdrFilmTask target = new MdrFilmTask(createCrawler(), urls, WireMockTestBase.MOCK_URL_BASE);
    final Set<Film> actual = target.invoke();

    assertThat(actual.size(), equalTo(1));
    AssertFilm.assertEquals(actual.iterator().next(), Sender.MDR, expectedTopic, expectedTitle, expectedTime, expectedDuration,
        expectedDescription,
        expectedWebsite, new GeoLocations[]{expectedGeoLocation}, expectedUrlSmall, expectedUrlNormal, expectedUrlHd, expectedSubtitle);
  }

  protected MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  private AbstractCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>();
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new MdrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}