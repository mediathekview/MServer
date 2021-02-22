package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.Connection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OrfDayTaskTest extends OrfTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() throws IOException {
    final String requestUrl = "http://tvthek.orf.at/schedule/03.02.2018";
    final Connection connection = JsoupMock.mock(requestUrl, "/orf/orf_day.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final TopicUrlDTO[] expected =
        new TopicUrlDTO[] {
          new TopicUrlDTO(
              "Wetter-Panorama",
              "https://tvthek.orf.at/profile/Wetter-Panorama/7268748/Wetter-Panorama/14007692"),
          new TopicUrlDTO(
              "Hallo okidoki",
              "https://tvthek.orf.at/profile/Hallo-okidoki/2616615/Hallo-okidoki/14007693"),
          new TopicUrlDTO(
              "Tolle Tiere",
              "https://tvthek.orf.at/profile/Tolle-Tiere/13764575/Tolle-Tiere/14007694"),
          new TopicUrlDTO(
              "Skiweltcup",
              "https://tvthek.orf.at/profile/Ski-alpin-Damen-Herren/13886795/Skiweltcup-Siegerehrungen-inkl-Uebergabe-der-Kristallkugeln/14007719"),
          new TopicUrlDTO(
              "Was ich glaube",
              "https://tvthek.orf.at/profile/Was-ich-glaube/1287/Was-ich-glaube/14007720"),
          new TopicUrlDTO(
              "ZIB", "https://tvthek.orf.at/profile/ZIB-1700/71284/ZIB-1700/14007722"),
          new TopicUrlDTO(
              "Vorarlberg heute",
              "https://tvthek.orf.at/profile/Vorarlberg-heute/70024/Vorarlberg-heute/14007821"),
          new TopicUrlDTO(
              "Wien heute", "https://tvthek.orf.at/profile/Wien-heute/70018/Wien-heute/14007816"),
          new TopicUrlDTO(
              "Fußball", "https://tvthek.orf.at/profile/Fussball/8205855/Fussball/14007727"),
          new TopicUrlDTO(
              "AD | Fußball",
              "https://tvthek.orf.at/profile/AD-Fussball/13886317/AD-Fussball/14007846"),
          new TopicUrlDTO("ZIB 1", "https://tvthek.orf.at/profile/ZIB-1/1203/ZIB-1/14007730"),
          new TopicUrlDTO(
              "ZIB 1 (ÖGS)", "https://tvthek.orf.at/profile/ZIB-1-OeGS/145302/ZIB-1-OeGS/14007848"),
          new TopicUrlDTO(
              "Embrace - Du bist schön",
              "https://tvthek.orf.at/profile/Embrace-Du-bist-schoen/13890275/Embrace-Du-bist-schoen/14007745")
        };

    final Queue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(requestUrl));

    final OrfDayTask target = new OrfDayTask(createCrawler(), queue, jsoupConnection);
    final Set<TopicUrlDTO> actual = target.invoke();

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
