package de.mediathekview.mserver.crawler.zdf.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
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
public class ZdfFilmDetailDeserializerTest {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "/zdf/zdf_film_details1.json",
            "Das Duo",
            "Echte Kerle",
            LocalDateTime.of(2017, 2, 1, 20, 15, 0),
            Duration.ofHours(1).plusMinutes(27).plusSeconds(35),
            "Der Mord an Studienrat Lampert führt \"Das Duo\" an eine Schule, an der Täter und Opfer sich vermutlich begegnet sind. In deren Umfeld suchen Clara Hertz und Marion Ahrens auch das Motiv.",
            "https://www.zdf.de/filme/das-duo/das-duo-echte-kerle-102.html"
        },
        {
          "/zdf/zdf_film_details2.json",
            "logo!",
            "logo! am Freitagabend",
            LocalDateTime.of(2018,2,23,19,25,0),
            Duration.ofMinutes(8).plusSeconds(14),
            "Schaut euch hier die logo!-Sendung von Freitagabend noch einmal an! Die Sendungen sind eine Woche online.",
            "https://www.zdf.de/kinder/logo/logo-am-freitagabend-104.html"
        },
        {
            "/zdf/zdf_film_details3.json",
            "JoNaLu",
            "Tanz auf dem Seil - Folge 25",
            LocalDateTime.of(2018, 3, 11, 9, 50, 0),
            Duration.ofMinutes(24).plusSeconds(55),
            "Naya verliert beim Seiltanz ihre Glücksblume und alles geht schief. Kann ein anderer Glücksbringer helfen? Glühwürmchen Minou hat eine \"leuchtende\" Idee.",
            "https://www.zdf.de/kinder/jonalu/tanz-auf-dem-seil-102.html"
        }
    });
  }

  private final String jsonFile;
  private final String expectedTopic;
  private final String expectedTitle;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedWebsite;

  public ZdfFilmDetailDeserializerTest(final String aJsonFile, final String aExpectedTopic, final String aExpectedTitle,
      final LocalDateTime aExpectedTime, final Duration aExpectedDuration, final String aExpectedDescription,
      final String aExpectedWebsite) {
    jsonFile = aJsonFile;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedWebsite = aExpectedWebsite;
  }

  @Test
  public void test() {
    final JsonObject json = JsonFileReader.readJson(jsonFile);
    final ZdfFilmDetailDeserializer target = new ZdfFilmDetailDeserializer();

    final Optional<Film> actual = target.deserialize(json, Film.class, null);

    assertThat(actual.isPresent(), equalTo(true));

    AssertFilm.assertEquals(actual.get(), Sender.ZDF, expectedTopic, expectedTitle, expectedTime, expectedDuration, expectedDescription,
        expectedWebsite);
  }
}
