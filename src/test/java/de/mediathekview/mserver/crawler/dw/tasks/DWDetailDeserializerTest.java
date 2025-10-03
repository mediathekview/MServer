package de.mediathekview.mserver.crawler.dw.tasks;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.crawler.dw.parser.DwFilmDetailDeserializer;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class DWDetailDeserializerTest extends DwTaskTestBase {

  private final String responseAsFile;
  private final String title;
  private final String topic;
  private final String website;
  private final Duration duration;
  private final LocalDateTime time;
  private final String urlVerySmall;
  private final String urlSmall;
  private final String urlNormal;
  private final String urlHd;
  private final String urlWqhd;

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
      final String video_q3,
      final String video_q4) {
    this.responseAsFile = responseAsFile;
    this.title = title;
    this.topic = topic;
    this.website = website;
    this.duration = duration;
    this.time = time;
    this.urlVerySmall = video_q0;
    this.urlSmall = video_q1;
    this.urlNormal = video_q2;
    this.urlHd = video_q3;
    this.urlWqhd = video_q4;
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
            LocalDateTime.of(2021, 10, 20, 19, 15, 40, 878 * 1000000),
            "",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/jd/jd20211020_sacha19cneu_sd_sor.mp4",
            "https://tvdownloaddw-a.akamaihd.net/Events/mp4/jd/jd20211020_sacha19cneu_sd.mp4",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/jd/jd20211020_sacha19cneu_sd_avc.mp4",
            ""
          },
          {
            "/dw/dw_film_detail_59567962.json",
            "Afghanistan-Konferenz in Moskau",
            "DW Nachrichten",
            "https://p.dw.com/p/41wLa",
            Duration.ofSeconds(122),
            LocalDateTime.of(2021, 10, 20, 19, 3, 32, 916 * 1000000),
            "",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/jd/jd20211020_afghan19c_sd_sor.mp4",
            "https://tvdownloaddw-a.akamaihd.net/Events/mp4/jd/jd20211020_afghan19c_sd.mp4",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/jd/jd20211020_afghan19c_sd_avc.mp4",
            ""
          },
          {
            "/dw/dw_film_detail_five_video_urls.json",
            "Energiezukunft? Schwimmende Windkraftanlagen",
            "Projekt Zukunft",
            "https://p.dw.com/p/4JNwb",
            Duration.ofSeconds(385),
            LocalDateTime.of(2022, 11, 12, 22, 0, 0, 0),
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/pz/pz221111_WindkraftNEU_AVC_480x270.mp4",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/pz/pz221111_WindkraftNEU_AVC_640x360.mp4",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/pz/pz221111_WindkraftNEU_AVC_960x540.mp4",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/pz/pz221111_WindkraftNEU_AVC_1280x720.mp4",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/pz/pz221111_WindkraftNEU_AVC_1920x1080.mp4"
          }
        });
  }

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() {
    final JsonElement jsonElement = JsonFileReader.readJson(responseAsFile);
    final DwFilmDetailDeserializer target = new DwFilmDetailDeserializer(createCrawler());
    final Optional<Film> actual = target.deserialize(jsonElement, null, null);
    //
    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get().getTitel(), equalTo(title));
    assertThat(actual.get().getThema(), equalTo(topic));
    assertThat(actual.get().getDuration(), equalTo(duration));
    assertThat(actual.get().getTime(), equalTo(time));
    assertThat(actual.get().getWebsite().get().toString(), equalTo(website));
    AssertFilm.assertUrl(urlVerySmall, actual.get().getUrl(Resolution.VERY_SMALL));
    AssertFilm.assertUrl(urlSmall, actual.get().getUrl(Resolution.SMALL));
    AssertFilm.assertUrl(urlNormal, actual.get().getUrl(Resolution.NORMAL));
    AssertFilm.assertUrl(urlHd, actual.get().getUrl(Resolution.HD));
    AssertFilm.assertUrl(urlWqhd, actual.get().getUrl(Resolution.WQHD));
  }
}
