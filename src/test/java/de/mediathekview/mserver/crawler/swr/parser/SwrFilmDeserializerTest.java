package de.mediathekview.mserver.crawler.swr.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SwrFilmDeserializerTest extends WireMockTestBase {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "/swr/swr_film_detail.json",
            "Landesschau Baden-Württemberg",
            "Landesschau Baden-Württemberg vom 12.09.2018",
            LocalDateTime.of(2018, 9, 12, 18, 45, 0),
            Duration.ofMinutes(44).plusSeconds(49),
            "Mit diesen Themen unter anderem: Anti-Terror-Übung am Stuttgarter Hauptbahnhof *** Neue Medikament zur Behandlung von Migräne zugelassen *** Zu Gast ist Lars Eidinger, Schauspieler, der die Rolle des Bertolt Brecht übernommen hat.",
            "https://swrmediathek.de/player.htm?show=bcdd80a0-b6bc-11e8-b070-005056a12b4c",
            WireMockTestBase.MOCK_URL_BASE + "/swr/swr-fernsehen/landesschau-bw/00-hauptbeitrag/1053798.m.mp4",
            WireMockTestBase.MOCK_URL_BASE + "/swr/swr-fernsehen/landesschau-bw/00-hauptbeitrag/1053798.l.mp4",
            WireMockTestBase.MOCK_URL_BASE + "/swr/swr-fernsehen/landesschau-bw/00-hauptbeitrag/1053798.xxl.mp4",
            "https://subtitles.swr.de/swr/swr-fernsehen/landesschau-bw/00-hauptbeitrag/1053798.xml"
        }
    });
  }

  private final String jsonFile;
  private final String theme;
  private final String title;
  private final LocalDateTime dateTime;
  private final Duration duration;
  private final String description;
  private final String website;
  private final String smallUrl;
  private final String normalUrl;
  private final String hdUrl;
  private final String subtitleUrl;

  public SwrFilmDeserializerTest(String aJsonFile, String aTheme, String aTitle,
      LocalDateTime aLocalDateTime, Duration aDuration,
      String aDescription, String aWebsite,
      String aSmallUrl, String aNormalUrl, String aHdUrl,
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

  @Test
  public void test() {
    JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

    setupHeadResponse(200);

    SwrFilmDeserializer target = new SwrFilmDeserializer();
    Optional<Film> actual = target.deserialize(jsonElement, Film.class, null);

    assertThat(actual.isPresent(), equalTo(true));
    Film actualFilm = actual.get();
    AssertFilm.assertEquals(actualFilm,
        Sender.SWR,
        theme,
        title,
        dateTime,
        duration,
        description,
        website,
        new GeoLocations[0],
        smallUrl,
        normalUrl,
        hdUrl,
        subtitleUrl
    );
  }
}