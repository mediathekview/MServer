package mServer.crawler.sender.hr;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import mServer.test.TestFileReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class HrSendungDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{

            {
                    "/hr/hr_sendung_detail1.html",
                    "maintower kriminalreport",
                    "https://www.hr-fernsehen.de/sendungen-a-z/maintower-kriminalreport/sendungen/maintower-kriminalreport,sendung-62594.html",
                    "https://hr-a.akamaihd.net/video/as/kriminalreport/2019_06/hrLogo_190602161651_0202702_512x288-25p-500kbit.vtt",
                    "86|512x288-25p-500kbit.mp4",
                    "86|1280x720-50p-5000kbit.mp4",
                    new DatenFilm(
                            Const.HR,
                            "maintower kriminalreport",
                            "https://www.hr-fernsehen.de/sendungen-a-z/maintower-kriminalreport/sendungen/maintower-kriminalreport,sendung-62594.html",
                            "Missbrauch an der eigenen Tochter – Fahndung geht weiter",
                            "https://hr-a.akamaihd.net/video/as/kriminalreport/2019_06/hrLogo_190602161651_0202702_960x540-50p-1800kbit.mp4",
                            "",
                            "02.06.2019",
                            "19:00:00",
                            1769,
                            "Ein Mann prahlt im Internet mit dem Missbrauch einer 12-Jährigen und bietet das Mädchen an. Wie wahr ist diese Geschichte wirklich? Die weiteren Themen: Tödlicher Ausraster in Bad Soden | Ein Mörder wird gesucht | Wenn der Betrüger zweimal klingelt | Der Kleingarten als Tatort |Überfall auf Getränkemarkt in Gelnhausen-Haitz | Kleinkrieg am Gartenzaun"
                    )
            },
            {
                    "/hr/hr_sendung_detail2.html",
                    "herrliches hessen",
                    "https://www.hr-fernsehen.de/sendungen-a-z/herrliches-hessen/sendungen/herrliches-hessen---unterwegs-in-und-um-eschenburg,sendung-37222.html",
                    "",
                    "",
                    "",
                    new DatenFilm(
                            Const.HR,
                            "herrliches hessen",
                            "https://www.hr-fernsehen.de/sendungen-a-z/herrliches-hessen/sendungen/herrliches-hessen---unterwegs-in-und-um-eschenburg,sendung-37222.html",
                            "Unterwegs in und um Eschenburg",
                            "https://hr-a.akamaihd.net/video/as/herrlicheshessen/2018_07/hrLogo_180703103341_0198041_512x288-25p-500kbit.mp4",
                            "",
                            "22.06.2019",
                            "17:15:00",
                            1767,
                            "Moderator Dieter Voss ist diesmal unterwegs im Lahn-Dill-Bergland – genauer gesagt in der Gemeinde Eschenburg. Die überwiegend ländlich geprägte Landschaft rund um Eschenburg ist ein echtes Paradies für Wander- und Naturfreunde."
                            )
            }
    });
  }

  private final String htmlFile;
  private final String theme;
  private final String url;
  private final DatenFilm expectedFilm;

  public HrSendungDeserializerTest(String aHtmlFile, String aTheme, String aUrl, String aExpectedSubtitle, String aExpectedSmallUrl, String aExpectedHdUrl, DatenFilm aFilm) {
    htmlFile = aHtmlFile;
    theme = aTheme;
    url = aUrl;
    expectedFilm = aFilm;
    expectedFilm.arr[DatenFilm.FILM_URL_SUBTITLE] = aExpectedSubtitle;
    expectedFilm.arr[DatenFilm.FILM_URL_HD] = aExpectedHdUrl;
    expectedFilm.arr[DatenFilm.FILM_URL_KLEIN] =aExpectedSmallUrl;
  }

  @Test
  public void deserializeTestWithVideo() {
    String html = TestFileReader.readFile(htmlFile);
    Document document = Jsoup.parse(html);

    HrSendungDeserializer target = new HrSendungDeserializer();
    DatenFilm actual = target.deserialize(theme, url, document);

    assertThat(actual, notNullValue());
    for (int i = 0; i < actual.arr.length; i++) {
      assertThat(actual.arr[i], equalTo(expectedFilm.arr[i]));
    }
  }

  @Test
  public void deserializeTestWithoutVideo() {
    String html = TestFileReader.readFile("/hr/hr_sendung_detail_without_video.html");
    Document document = Jsoup.parse(html);

    HrSendungDeserializer target = new HrSendungDeserializer();
    DatenFilm actual = target.deserialize("heimspiel!", "http://www.hr-fernsehen.de/sendungen-a-z/heimspiel/sendungen/sobiech-und-stein-im-heimspiel,sendung-13662.html", document);

    assertThat(actual, nullValue());
  }
}
