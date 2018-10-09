package mServer.crawler.sender.hr;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import java.util.Arrays;
import java.util.Collection;
import mServer.test.TestFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class HrSendungDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {
        "/hr/hr_sendung_detail1.html",
        "service: reisen",
        "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen,sendung-13268.html",
        "",
        new DatenFilm(
        Const.HR,
        "service: reisen",
        "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen,sendung-13268.html",
        "Von Marienbad nach Karlsbad",
        "http://www.hr.gl-systemhaus.de/video/as/servicereisen/2017_09/hrLogo_170919120157_0193742_512x288-25p-500kbit.mp4",
        "",
        "19.09.2017",
        "18:50:00",
        1506,
        "\"service: reisen\" begibt sich auf eine ganz besondere Trinkkur durch Böhmen und entdeckt dabei nicht nur bemerkenswerte Heilquellen, sondern auch modernere Therapie-Formen wie das Bierwellness."
        )},
      {"/hr/hr_sendung_hessenschau_detail.html", "hessenschau", "http://www.hessenschau.de/tv-sendung/video-43192.html", "",
        new DatenFilm(
        Const.HR,
        "hessenschau",
        "http://www.hessenschau.de/tv-sendung/video-43192.html",
        "hessenschau - ganze Sendung",
        "http://www.hr.gl-systemhaus.de/video/as/hessenschau/2017_09/hrLogo_170928200235_L273051_512x288-25p-500kbit.mp4",
        "",
        "28.09.2017",
        "19:30:00",
        1653,
        "Polizei hofft auf Hinweise zu totem Säugling / Ryanair lässt rund 18.000 Flüge ausfallen / Wahl-Nachlese zur AfD / Wem nutzt das Gesetz zur Wahlkreis-Neuordnung? / Zucker-Branche steht vor historischer Reform / Aids-Hilfe-Loveball in Frankfurt am Main / Hirschbrunft in der Alten Fasanerie Hanau"
        )},
      {
        "/hr/hr_sendung_detail2.html",
        "hr-katzen",
        "http://www.hr-fernsehen.de/sendungen-a-z/hr-katzen/index.html",
        "",
        new DatenFilm(
        Const.HR,
        "hr-katzen",
        "http://www.hr-fernsehen.de/sendungen-a-z/hr-katzen/index.html",
        "Die neuen Pausenkatzen",
        "http://www.hr.gl-systemhaus.de/video/fs/allgemein/2017_07/170705102721_hr-Fernsehen_Hessischer_Rundfunk_hr-online.de.mp4",
        "",
        "05.07.2017",
        "00:00:00",
        145,
        "Sie sind Kult: die Pausenkatzen des Hessischen Rundfunks. Erfunden in den 70er Jahren klettern und tollen sie heute wieder in Randzeiten durchs Programm des hr-fernsehens. Ende 2015 hat der hr die Pausenkatzen neu produziert: flauschig und in ultrascharfer HD-Qualität."
        )},
      {
        "/hr/hr_sendung_detail3.html",
        "strassen stars",
        "https://www.hr-fernsehen.de/sendungen-a-z/strassen-stars/sendungen/strassen-stars,sendung-32932.html",
        "https://hr-a.akamaihd.net/video/as/strassenstars/2018_05/hrLogo_180513185139_0196460_512x288-25p-500kbit.vtt",
        new DatenFilm(
        Const.HR,
        "strassen stars",
        "https://www.hr-fernsehen.de/sendungen-a-z/strassen-stars/sendungen/strassen-stars,sendung-32932.html",
        "Comedy-Quiz rund um Menschenkenntnis zum Mitraten mit Roberto Cappelluti",
        "https://hr-a.akamaihd.net/video/as/strassenstars/2018_05/hrLogo_180513185139_0196460_512x288-25p-500kbit.mp4",
        "",
        "13.05.2018",
        "23:30:00",
        1800,
        "Rateteam: Susanne Fröhlich, Jörg Thadeusz, Hadnet Tesfai"
        )}

    });
  }

  private final String htmlFile;
  private final String theme;
  private final String url;
  private final DatenFilm expectedFilm;

  public HrSendungDeserializerTest(String aHtmlFile, String aTheme, String aUrl, String aExpectedSubtitle, DatenFilm aFilm) {
    htmlFile = aHtmlFile;
    theme = aTheme;
    url = aUrl;
    expectedFilm = aFilm;
    expectedFilm.arr[DatenFilm.FILM_URL_SUBTITLE] = aExpectedSubtitle;
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
