package de.mediathekview.mserver.crawler.dw.tasks;


import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.dw.DwCrawler;
import de.mediathekview.mserver.crawler.dw.parser.DWSendungOverviewDeserializer;
import de.mediathekview.mserver.crawler.dw.parser.DwFilmDetailDeserializer;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonElement;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;


@RunWith(Parameterized.class)
public class DWDetailDeserializerTest extends DwTaskTestBase {

  private final String responseAsFile;
  private final String title;
  private final String topic;
  private final String website;
  private final Duration duration;
  private final LocalDateTime time;
  private final String video_q0;
  private final String video_q1;
  private final String video_q2;
  private final String video_q3;
  

  public DWDetailDeserializerTest(
      final String responseAsFile,
      final String title,
      final String topic,
      final String website,
      final Duration duration,
      final LocalDateTime time,
      final String video_q0,
      final String video_q1,
      final String video_q2,
      final String video_q3
      ) {
    this.responseAsFile = responseAsFile;
    this.title = title;
    this.topic = topic;
    this.website = website;
    this.duration = duration;
    this.time = time;
    this.video_q0 = video_q0;
    this.video_q1 = video_q1;
    this.video_q2 = video_q2;
    this.video_q3 = video_q3;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/dw/dw_film_detail_59567960.json",
            "Kremlkritiker Alexej Nawalny erhält Sacharow-Preis",
            "DW Nachrichten",
            "https://p.dw.com/p/41wLY",
            Duration.ofSeconds(102),
            LocalDateTime.of(2021, 10, 20, 19, 15, 40, 878*1000000),
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/jd/jd20211020_sacha19cneu_sd_sor.mp4",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/jd/jd20211020_sacha19cneu_sd_avc.mp4",
            "https://tvdownloaddw-a.akamaihd.net/Events/mp4/jd/jd20211020_sacha19cneu_sd.mp4",
            "https://tvdownloaddw-a.akamaihd.net/Events/mp4/jd/jd20211020_sacha19cneu_hd.mp4"
          },
          {
              "/dw/dw_film_detail_59567962.json",
              "Afghanistan-Konferenz in Moskau",
              "DW Nachrichten",
              "https://p.dw.com/p/41wLa",
              Duration.ofSeconds(122),
              LocalDateTime.of(2021, 10, 20, 19, 03, 32, 916*1000000),
              "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/jd/jd20211020_afghan19c_sd_sor.mp4",
              "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/jd/jd20211020_afghan19c_sd_avc.mp4",
              "https://tvdownloaddw-a.akamaihd.net/Events/mp4/jd/jd20211020_afghan19c_sd.mp4",
              "https://tvdownloaddw-a.akamaihd.net/Events/mp4/jd/jd20211020_afghan19c_hd.mp4"
            }
          
        });
  }

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() throws IOException {
    final JsonElement jsonElement = JsonFileReader.readJson(responseAsFile);
    final DwFilmDetailDeserializer target = new DwFilmDetailDeserializer(createCrawler());
    final Optional<Film> actual = target.deserialize(jsonElement, null, null);
    //
    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get().getTitel(), equalTo(title));
    assertThat(actual.get().getThema(), equalTo(topic));
    assertThat(actual.get().getDuration(), equalTo(duration));
    assertThat(actual.get().getTime(), equalTo(time));
    assertThat(actual.get().getUrl(Resolution.VERY_SMALL).getUrl().toString(), equalTo(video_q0));
    assertThat(actual.get().getUrl(Resolution.SMALL).getUrl().toString(), equalTo(video_q1));
    assertThat(actual.get().getUrl(Resolution.NORMAL).getUrl().toString(), equalTo(video_q2));
    assertThat(actual.get().getUrl(Resolution.HD).getUrl().toString(), equalTo(video_q3));

  }

}
