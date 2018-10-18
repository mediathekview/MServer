package de.mediathekview.mserver.crawler.hr.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
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
@PowerMockIgnore("javax.net.ssl.*")
public class HrSendungsfolgedetailsTaskTest extends HrTaskTestBase {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "https://www.hr-fernsehen.de/sendungen-a-z/engel-fragt/sendungen/engel-fragt-bin-ich-schoen,sendung-29818.html",
                "/hr/hr_film_detail1.html",
                "Engel fragt",
                "Bin ich schön?",
                "Unser Schönheitsideal haben wir heutzutage in der Hosentasche. Ein Blick auf die soziale Plattform Instagram, und es ist klar, wie der vermeintlich perfekte Körper auszusehen hat. Die Messlatte für das eigene Ich - ständig präsent.",
                LocalDateTime.of(2018, 4, 10, 21, 45, 0),
                Duration.ofMinutes(28).plusSeconds(20),
                "",
                "https://hr-a.akamaihd.net/video/as/engelfragt/2018_04/hrLogo_180409113655_0196595_512x288-25p-500kbit.mp4",
                "",
                "https://hr-a.akamaihd.net/video/as/engelfragt/2018_04/hrLogo_180409113655_0196595_512x288-25p-500kbit.vtt",
                GeoLocations.GEO_NONE
            },
            {
                "https://www.hr-fernsehen.de/sendungen-a-z/erlebnis-hessen/sendungen/altstaedte-im-aufbruch---die-neue-liebe-fuer-hessens-alte-stadtkerne-,sendung-43798.html",
                "/hr/hr_film_detail2.html",
                "Erlebnis Hessen",
                "Altstädte im Aufbruch - die neue Liebe für Hessens alte Stadtkerne",
                "Nicht nur Frankfurt will seine Altstadt zurück – auch in Schlitz, Marburg oder Büdingen beschäftigen sich Stadtplaner und Bürger damit, wie sie ihre Stadtzentren wieder zu einem lebendigen Mittelpunkt machen können.",
                LocalDateTime.of(2018, 9, 29, 21, 0, 0),
                Duration.ofMinutes(44).plusSeconds(20),
                "https://hr-a.akamaihd.net/video/as/erlebnishessen/2018_09/hrLogo_180929171859_0199232_512x288-25p-500kbit.mp4",
                "https://hr-a.akamaihd.net/video/as/erlebnishessen/2018_09/hrLogo_180929171859_0199232_960x540-50p-1800kbit.mp4",
                "https://hr-a.akamaihd.net/video/as/erlebnishessen/2018_09/hrLogo_180929171859_0199232_1280x720-50p-5000kbit.mp4",
                "https://hr-a.akamaihd.net/video/as/erlebnishessen/2018_09/hrLogo_180929171859_0199232_512x288-25p-500kbit.vtt",
                GeoLocations.GEO_NONE
            },
            {
                "https://www.hessenschau.de/tv-sendung/hessenschau---ganze-sendung,video-73874.html",
                "/hr/hr_film_detail_hessenschau.html",
                "hessenschau",
                "hessenschau - ganze Sendung",
                "- Gründung der \"Juden in der AfD\" von Protesten begleitet - Landesgartenschau schließt ihre Pforten - Vereine verwandeln Altpapier in Bares - Weitergedreht: Räumungs-Wirrwarr in Offenbach - Modemacher (7): Handschuh-Designerin aus Frankfurt",
                LocalDateTime.of(2018, 10, 7, 19, 30, 0),
                Duration.ofMinutes(27).plusSeconds(30),
                "https://hr-a.akamaihd.net/video/as/hessenschau/2018_10/181007203848_2018-10-07_19-25-02_hessenschau_512x288-25p-500kbit.mp4",
                "https://hr-a.akamaihd.net/video/as/hessenschau/2018_10/181007203848_2018-10-07_19-25-02_hessenschau_960x540-50p-1800kbit.mp4",
                "https://hr-a.akamaihd.net/video/as/hessenschau/2018_10/181007203848_2018-10-07_19-25-02_hessenschau_1280x720-50p-5000kbit.mp4",
                "https://hr-a.akamaihd.net/video/as/hessenschau/2018_10/181007203848_2018-10-07_19-25-02_hessenschau_512x288-25p-500kbit.vtt",
                GeoLocations.GEO_NONE
            },
            {
                "https://www.hr-fernsehen.de/sendungen-a-z/hessenreporter/sendungen/im-einsatz-gegen-mietwucher-die-sozialfahnderin,sendung-34026.html",
                "/hr/hr_film_detail3.html",
                "Hessenreporter",
                "Im Einsatz gegen Mietwucher",
                "Drei Familien mit insgesamt elf Personen in einer Fünf-Zimmer-Wohnung - ohne Heizung, ohne Küche, die einzige Toilette auf halber Treppe im Hausflur: Solches Elend sieht Petra Windrich fast täglich. Der Vermieter kassiert von jeder Partei 700 Euro, insgesamt also 2.100 Euro – jeden Monat. Ein Fall von Mietwucher.",
                LocalDateTime.of(2018, 5, 28, 18, 30, 0),
                Duration.ofMinutes(29).plusSeconds(30),
                "",
                "https://hr-a.akamaihd.net/video/as/hessenreporter/2018_05/hrLogo_180528130435_0197529_512x288-25p-500kbit.mp4",
                "",
                "https://hr-a.akamaihd.net/video/as/hessenreporter/2018_05/hrLogo_180528130435_0197529_512x288-25p-500kbit.vtt",
                GeoLocations.GEO_NONE
            }
        });
  }

  private final String requestUrl;
  private final String htmlPage;
  private final String expectedTopic;
  private final String expectedTitle;
  private final String expectedDescription;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;

  public HrSendungsfolgedetailsTaskTest(final String aRequestUrl, final String aHtmlPage,
      final String aExpectedTopic, final String aExpectedTitle, final String aExpectedDescription, final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration, final String aExpectedUrlSmall, final String aExpectedUrlNormal, final String aExpectedUrlHd,
      final String aExpectedSubtitle, final GeoLocations aExpectedGeo) {
    requestUrl = aRequestUrl;
    htmlPage = aHtmlPage;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedDescription = aExpectedDescription;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedSubtitle = aExpectedSubtitle;
    expectedGeo = aExpectedGeo;
  }

  @Test
  public void test() throws IOException {
    JsoupMock.mock(requestUrl, htmlPage);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final HrSendungsfolgedetailsTask target = new HrSendungsfolgedetailsTask(createCrawler(), urls);
    final Set<Film> actual = target.invoke();

    assertThat(actual.size(), equalTo(1));
    AssertFilm.assertEquals(actual.iterator().next(), Sender.HR, expectedTopic, expectedTitle, expectedTime, expectedDuration,
        expectedDescription,
        requestUrl, new GeoLocations[]{expectedGeo}, expectedUrlSmall, expectedUrlNormal, expectedUrlHd, expectedSubtitle);
  }
}