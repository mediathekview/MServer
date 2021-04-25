package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
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

public class OrfHistoryTopicTaskTest extends OrfTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() throws IOException {
    final String requestUrl =
        "https://tvthek.orf.at/history/Die-Geschichte-Niederoesterreichs/8378971";
    final String topic = "Die Geschichte Niederösterreichs";

    jsoupConnection = JsoupMock.mock(requestUrl, "/orf/orf_history_topic_overview.html");
    OrfCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);
    
    final TopicUrlDTO[] expected =
        new TopicUrlDTO[] {
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Johanna-Mikl-Leitner-erste-Landeshauptfrau-Niederoesterreichs/13927331"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Letzte-Rede-von-Erwin-Proell-als-Landeshauptmann/13927148"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Landeshauptmann-Erwin-Proell-OeVP-tritt-zurueck/13914863"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Der-Werdegang-des-Julius-Raab/8293346"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Leopold-Figl-Glaubt-an-dieses-Oesterreich/9697062"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Liese-Prokop-erste-Innenministerin-Oesterreichs/13919164"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Niederoesterreich-erhaelt-seine-erste-Landeshymne/8602764"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Ein-Land-sucht-seine-Hauptstadt/8293238"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Protest-gegen-Kraftwerk-in-der-Wachau/8323916"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Siegfried-Ludwig-im-Portraet/8675677"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Der-vorletzte-Kuenringer-Andreas-Maurer/8298065"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Karl-Schloegl-zieht-sich-aus-Politik-zurueck/13988092"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Eroeffnung-des-Josef-Reither-Museums/8764784"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Nachruf-Siegfried-Ludwig/8322787"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Regierungsviertel-Spatenstich-in-St-Poelten/8276763"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Landeshauptleute-und-Politik/8378973/Feierliche-Eroeffnung-des-Regierungsviertels/8276738"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/Ein-Tag-im-Stift-Melk/8313371"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/Moench-und-Manager-Ein-Blick-in-das-Stift-Altenburg/8313396"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/Das-belebte-Stift-Goettweig/8323032"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/Die-Schatzkammer-von-Klosterneuburg/8294667"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/Doppelkloster-Stift-Geras-Pernegg/8313494"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/Bildband-Die-Wienerwaldkloester/8276592"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/Kulturerbe-Stift-Lilienfeld/8313407"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/350-Jahre-Basilika-Maria-Taferl/8293327"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/900-Jahre-Stift-Seitenstetten/8302230"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/Jubilaeum-im-Stift-Zwettl/8323108"),
          new TopicUrlDTO(
              topic,
              "https://tvthek.orf.at/history/Stifte-und-Kloester/8378976/Pilgerstaette-am-Sonntagberg/8328932")
        };

    final Queue<TopicUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new TopicUrlDTO(topic, requestUrl));

    final OrfHistoryTopicTask target =
        new OrfHistoryTopicTask(crawler, queue);
    final Set<TopicUrlDTO> actual = target.invoke();

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
