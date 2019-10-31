package de.mediathekview.mserver.crawler.wdr.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.hamcrest.Matchers;
import org.jsoup.Connection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WdrDayPageTaskTest extends WdrTaskTestBase {

  @Mock
  JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test() throws IOException {
    final String requestUrl =
        "https://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-03022018.html";
    Connection connection = JsoupMock.mock(requestUrl, "/wdr/wdr_day.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final TopicUrlDTO[] expected =
        new TopicUrlDTO[] {
          new TopicUrlDTO(
              "WDR.DOK",
              "https://www1.wdr.de/mediathek/video/sendungen/wdr-dok/video-die-kommissare-vom-rhein----jahre-koelner-tatort-100.html"),
          new TopicUrlDTO(
              "Tatort",
              "https://www1.wdr.de/mediathek/video/sendungen/tatort/video-nachbarn-100.html"),
          new TopicUrlDTO(
              "Lokalzeitgeschichten",
              "https://www1.wdr.de/mediathek/video/sendungen/lokalzeitgeschichten/video-lokalzeit-geschichten---heimat-100.html"),
          new TopicUrlDTO(
              "Aktuelle Stunde",
              "https://www1.wdr.de/mediathek/video/sendungen/aktuelle-stunde/video-aktuelle-stunde-2150.html"),
          new TopicUrlDTO(
              "Taminas ReiseTest",
              "https://www1.wdr.de/mediathek/video/sendungen/video-taminas-reisetest-radtouren--kurztrip-auf-zwei-raedern-104.html"),
          new TopicUrlDTO(
              "Münsterland Giro 2017",
              "https://www1.wdr.de/mediathek/video/sendungen/video-muensterland-giro--104.html"),
          new TopicUrlDTO(
              "Unterhaltung",
              "https://www1.wdr.de/mediathek/video/sendungen/unterhaltung/video-ein-herz-und-eine-seele---besuch-aus-der-ostzone-100.html"),
          new TopicUrlDTO(
              "Servicezeit",
              "https://www1.wdr.de/mediathek/video/sendungen/servicezeit/video-bjoern-freitags-streetfood-duell-pulled-korean-bbq-taco-100.html"),
          new TopicUrlDTO(
              "Lecker an Bord",
              "https://www1.wdr.de/mediathek/video/sendungen/lecker-an-bord/video-kaffee-bier-und-historisches-brot--100.html"),
          new TopicUrlDTO(
              "Flussgeschichten",
              "https://www1.wdr.de/mediathek/video/sendungen/video-flussgeschichten---die-ruhr-104.html"),
          new TopicUrlDTO(
              "Fernsehfilm",
              "https://www1.wdr.de/mediathek/video/sendungen/fernsehfilm/video-ein-hausboot-zum-verlieben-100.html"),
          new TopicUrlDTO(
              "Fernsehfilm",
              "https://www1.wdr.de/mediathek/video/sendungen/fernsehfilm/video-die-farben-der-liebe-102.html")
        };

    final ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(requestUrl));

    final WdrDayPageTask target = new WdrDayPageTask(createCrawler(), queue, jsoupConnection);
    final Set<TopicUrlDTO> actual = target.invoke();

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
