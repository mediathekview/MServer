package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.srf.tasks.SrfTaskTestBase;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class SrfFilmJsonDeserializerTest extends SrfTaskTestBase {

  private final String jsonFile;
  private final String m3u8File;
  private final String m3u8Url;
  private final String theme;
  private final String title;
  private final LocalDateTime dateTime;
  private final long duration;
  private final String description;
  private final String website;
  private final String smallUrl;
  private final String normalUrl;
  private final String hdUrl;
  private final String subtitleUrl;

  public SrfFilmJsonDeserializerTest(
      final String aJsonFile,
      final String aM3u8File,
      final String aM3u8Url,
      final String aTheme,
      final String aTitle,
      final LocalDateTime aLocalDateTime,
      final long aDuration,
      final String aDescription,
      final String aWebsite,
      final String aSmallUrl,
      final String aNormalUrl,
      final String aHdUrl,
      final String aSubtitleUrl) {
    jsonFile = aJsonFile;
    m3u8File = aM3u8File;
    m3u8Url = aM3u8Url;
    theme = aTheme;
    title = aTitle;
    dateTime = aLocalDateTime;
    duration = aDuration;
    description = aDescription;
    website = aWebsite;
    smallUrl = aSmallUrl;
    normalUrl = aNormalUrl;
    hdUrl = aHdUrl;
    subtitleUrl = aSubtitleUrl;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
            {
                "/srf/srf_film_page1.json",
                "/srf/srf_film_page1.m3u8",
                "/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/master.m3u8?start=0.0&end=3305.1",
                "1 gegen 100",
                "1 gegen 100 vom 17.05.2010",
                LocalDateTime.of(2010, 5, 17, 20, 7, 6),
                3305100,
                "Spannung pur, wenn Susanne Kunz die Frage stellt und der Kandidat zwar eine Ahnung hat aber nicht ganz sicher ist ob die Antwort stimmt. Dann wird es im Studio «1 gegen 100» ruhig und man spürt die Anspannung des Kandidaten förmlich. Nimmt er nun einen Joker zur Hilfe oder setzt er alles auf eine Karte und riskiert, ohne Geld und als Verlierer vom Platz zu gehen? Köpfchen, Mut und Taktik sind gefr\n.....",
                "https://www.srf.ch/play/tv/1-gegen-100/video/1-gegen-100-vom-17-05-2010?id=22b9dd2c-d1fd-463b-91de-d804eda74889",
                "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_av.m3u8",
                "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_2_av.m3u8",
                "",
                ""
            },
            {
                "/srf/srf_film_page_with_subtitle.json",
                "/srf/srf_film_page_with_hd.m3u8",
                "/i/vod/meteo/2018/01/meteo_20180102_195400_8400830_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/master.m3u8?start=0.0&end=333.0",
                "SRF Meteo",
                "Meteo vom 02.01.2018, 19:55",
                LocalDateTime.of(2018, 1, 2, 19, 54, 0),
                333000,
                "Meteo",
                "https://www.srf.ch/play/tv/srf-meteo/video/meteo-vom-02-01-2018-1955?id=4228f550-8702-4276-8001-03a1589804ef",
                "https://srfvodhd-vh.akamaihd.net/i/vod/meteo/2018/01/meteo_20180102_195400_8400830_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_3_av.m3u8",
                "https://srfvodhd-vh.akamaihd.net/i/vod/meteo/2018/01/meteo_20180102_195400_8400830_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_4_av.m3u8",
                "https://srfvodhd-vh.akamaihd.net/i/vod/meteo/2018/01/meteo_20180102_195400_8400830_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_5_av.m3u8",
                "https://ws.srf.ch/subtitles/urn:srf:ais:video:4228f550-8702-4276-8001-03a1589804ef/subtitle.ttml"
            },
            {
                "/srf/srf_film_page_multiple_xmpeg.json",
                "/srf/srf_film_page_multiple_xmpeg.m3u8",
                "/i/vod/sportflashtv/2018/11/sportflashtv_20181108_200102_12368079_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/master.m3u8?start=0.0&end=183.96",
                "sportflash",
                "sportflash",
                LocalDateTime.of(2018, 11, 8, 20, 0, 0),
                183960,
                "«sportflash» ist jung und schnell. Der Einstieg in den Hauptabend auf SRF zwei bringt das Sportgeschehen aus aller Welt auf den Punkt. Von Montag bis Samstag zeigt das Format die wichtigsten News und besten Bilder des Tages. Auch Unkonventionelles und Unterhaltendes hat in der Sendung Platz.",
                "https://www.srf.ch/play/tv/sportflash/video/sportflash?id=0e2490f3-743a-440d-9a58-f5d9d3e33e87",
                "https://srfvodhd-vh.akamaihd.net/i/vod/sportflashtv/2018/11/sportflashtv_20181108_200102_12368079_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_3_av.m3u8",
                "https://srfvodhd-vh.akamaihd.net/i/vod/sportflashtv/2018/11/sportflashtv_20181108_200102_12368079_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_4_av.m3u8",
                "https://srfvodhd-vh.akamaihd.net/i/vod/sportflashtv/2018/11/sportflashtv_20181108_200102_12368079_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_5_av.m3u8",
                ""
            },
            {
                "/srf/srf_film_page_with_optimize_m3u8url.json",
                "/srf/srf_film_page_with_optimize_m3u8url.m3u8",
                "/i/vod/lena/2018/11/lena_20181114_114517_12440540_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/master.m3u8?start=0.0&end=2549.76",
                "Lena – Liebe meines Lebens",
                "Kapitel 156",
                LocalDateTime.of(2018, 11, 14, 11, 45, 0),
                2549760,
                "Was soll Vanessa bloss tun? Sind ihre Tricks und Manipulationen plötzlich wirkungslos? Doch gefühlslos ist Vanessa nicht: Sie vermisst ihr kleines Kind ganz schrecklich.",
                "https://www.srf.ch/play/tv/lena-–-liebe-meines-lebens/video/kapitel-156?id=69d9fc3f-a3fd-4802-b2ee-ede92145e87c",
                "https://srfvodhd-vh.akamaihd.net/i/vod/lena/2018/11/lena_20181114_114517_12440540_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_3_av.m3u8",
                "https://srfvodhd-vh.akamaihd.net/i/vod/lena/2018/11/lena_20181114_114517_12440540_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_4_av.m3u8",
                "https://srfvodhd-vh.akamaihd.net/i/vod/lena/2018/11/lena_20181114_114517_12440540_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_5_av.m3u8",
                "https://ws.srf.ch/subtitles/urn:srf:ais:video:69d9fc3f-a3fd-4802-b2ee-ede92145e87c/subtitle.ttml"
            },
            {
                "/srf/srf_film_page_with_subtitle1.json",
                "/srf/srf_film_page_with_subtitle1.m3u8",
                "/i/vod/reporter/2019/12/reporter_20191211_172931_18722867_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/master.m3u8?start=0.0&end=1319.0&caption=srf/07630ff9-9858-4a00-bf10-9856c9891970/episode/de/vod/vod.m3u8:de:Deutsch:sdh&webvttbaseurl=www.srf.ch/subtitles",
                "Reporter",
                "Cliqme – Der Berner Star im Kosovo",
                LocalDateTime.of(2020, 1, 26, 22, 25, 0),
                1319000,
                "Seit einem Jahr wohnt Gassann Nyangi in Pristina. Der sprachbegabte Berner spricht bereits Albanisch mit kosovarischem Akzent. Das gefällt den Einheimischen: Seit er gemeinsam mit dem kosovarischen Rapper Capital T. einen Song produziert hat, wurde Cliqme dort auf einen Schlag bekannt. In der Schweiz trat er bereits als MC auf – als sogenannter Warmup für Superstars. Jetzt reisst Cliqme auf der Bü\n.....",
                "https://www.srf.ch/play/tv/reporter/video/cliqme-–-der-berner-star-im-kosovo?id=2b08d6d3-9a7a-4827-9f7b-20f89e1ad144",
                "https://hdvodsrforigin-f.akamaihd.net/i/vod/reporter/2019/12/reporter_20191211_172931_18722867_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_3_av.m3u8",
                "https://hdvodsrforigin-f.akamaihd.net/i/vod/reporter/2019/12/reporter_20191211_172931_18722867_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_4_av.m3u8",
                "https://hdvodsrforigin-f.akamaihd.net/i/vod/reporter/2019/12/reporter_20191211_172931_18722867_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_5_av.m3u8",
                "https://www.srf.ch/subtitles/srf/07630ff9-9858-4a00-bf10-9856c9891970/episode/de/vod/vod.vtt"
            }
        });
  }

  @Test
  public void test() {
    final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

    setupSuccessfulResponse(m3u8Url, m3u8File);

    final SrfFilmJsonDeserializer target = new SrfFilmJsonDeserializer(createCrawler());
    final Optional<Film> actual = target.deserialize(jsonElement, Film.class, null);

    assertThat(actual.isPresent(), equalTo(true));
    final Film actualFilm = actual.get();
    AssertFilm.assertEquals(
        actualFilm,
        Sender.SRF,
        theme,
        title,
        dateTime,
        Duration.of(duration, ChronoUnit.MILLIS),
        description,
        website,
        new GeoLocations[0],
        smallUrl,
        normalUrl,
        hdUrl,
        subtitleUrl);
  }
}