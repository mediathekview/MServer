package de.mediathekview.mserver.crawler.srf.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.srf.tasks.SrfTaskTestBase;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SrfFilmJsonDeserializerTest extends SrfTaskTestBase {

  private final String jsonFile;
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
      String aJsonFile,
      String aTheme,
      String aTitle,
      LocalDateTime aLocalDateTime,
      long aDuration,
      String aDescription,
      String aWebsite,
      String aSmallUrl,
      String aNormalUrl,
      String aHdUrl,
      String aSubtitleUrl) {
    jsonFile = aJsonFile;
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
            "/srf/srf_film_page.json",
            "Anna erfüllt Wünsche",
            "Mit Sandra Boner aufs «Meteo»-Dach",
            LocalDateTime.of(2020, 4, 15, 15, 0, 0),
              462080,
            "Einen Tag bei «SRF Meteo» dabei sein und mit Sandra Boner aufs «Meteo»-Dach steigen, das ist der grösste Wunsch von Ladina. Sie ist ein riesiger Fan der Wettersendung und will später einmal Meteorologin werden. Ihre Schwester Flavia weiss davon und meldet den Wunsch Anna Zöllig.",
            "", //https://www.srf.ch/play/tv/anna-erfuellt-wuensche/video/mit-sandra-boner-aufs-meteo-dach?id=342a1a95-42ec-4568-b653-a042c54f7763
            "",
            "https://podcastsource.sf.tv/nps/77037528/462.08/Mit+Sandra+Boner+aufs+%C2%ABMeteo%C2%BB-Dach/podcast/annaerfuelltwuensche/2020/04/annaerfuelltwuensche_20200415_082933_20467830_v_podcast_h264_q10.mp4?assetId=342a1a95-42ec-4568-b653-a042c54f7763",
            "https://podcastsource.sf.tv/nps/210262032/462.08/Mit+Sandra+Boner+aufs+%C2%ABMeteo%C2%BB-Dach/podcast/annaerfuelltwuensche/2020/04/annaerfuelltwuensche_20200415_082933_20467830_v_podcast_h264_q30.mp4?assetId=342a1a95-42ec-4568-b653-a042c54f7763",
            ""
          }
        });
  }

  @Test
  public void test() {
    JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

    SrfFilmJsonDeserializer target = new SrfFilmJsonDeserializer(createCrawler());
    Optional<Film> actual = target.deserialize(jsonElement, Film.class, null);

    assertThat(actual.isPresent(), equalTo(true));
    Film actualFilm = actual.get();
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
