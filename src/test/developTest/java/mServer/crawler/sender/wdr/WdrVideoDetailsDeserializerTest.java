package mServer.crawler.sender.wdr;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import java.util.Arrays;
import java.util.Collection;
import mServer.test.TestFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class WdrVideoDetailsDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {"/wdr/wdr_video_details2.html", "Ausgerechnet", "Schokolade", "Knapp 25 Prozent der Bevölkerung verzehren Schokolade mehrmals in der Woche. Hinzu kommt gut ein weiteres Viertel, das Schokolade etwa einmal pro Woche genießt. Frauen greifen hier generell häufiger zu als Männer.", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---schokolade-102.html", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_2_av.m3u8", "196|0_av.m3u8", "196|4_av.m3u8", "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/140/1407842/1407842_16348809.xml", "15.07.2017", "16:00:00", "00:43:35", "http://deviceids-medp.wdr.de/ondemand/140/1407842.js", "/wdr/wdr_video2.js", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/master.m3u8", "/wdr/wdr_video2.m3u8"},
      {"/wdr/wdr_video_details4.html", "Abenteuer Erde: Wildes Kanada", "Abenteuer Erde: Wildes Kanada", "Der Film beginnt dort, wo die Europäer erstmals kanadischen Boden betraten. Dieses \"neue gefundene Land\" heißt heute Neufundland. Eine auf den ersten Blick wilde und unberührte Gegend. Die aber war damals schon lange von Ureinwohnern geformt worden. Autor/-in: Jeff Turner", "https://www1.wdr.de/mediathek/video/sendungen/abenteuer-erde/video-abenteuer-erde-wildes-kanada--100.html", "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/161/1614191/,1614191_18613191,1614191_18613190,1614191_18613193,1614191_18613188,1614191_18613192,1614191_18613189,.mp4.csmil/index_2_av.m3u8", "197|0_av.m3u8", "197|4_av.m3u8", "", "03.04.2018", "20:15:00", "01:28:23", "http://deviceids-medp.wdr.de/ondemand/161/1614191.js", "/wdr/wdr_video4.js", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/161/1614191/,1614191_18613191,1614191_18613190,1614191_18613193,1614191_18613188,1614191_18613192,1614191_18613189,.mp4.csmil/master.m3u8", "/wdr/wdr_video4.m3u8"},
      {"/wdr/wdr_video_details5.html", "Henker & Richter", "Die Sau ist tot (2/16)", "Zwei Hasen, ein paar Fasane und ein toter Bauer", "https://www1.wdr.de/mediathek/video/sendungen/video-henker--richter---die-sau-ist-tot--100.html", "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/160/1607412/,1607412_18526624,1607412_18526625,1607412_18526622,1607412_18526621,1607412_18526626,1607412_18526623,.mp4.csmil/index_2_av.m3u8", "197|0_av.m3u8", "197|4_av.m3u8", "", "30.03.2018", "16:25:00", "00:46:47", "http://deviceids-medp.wdr.de/ondemand/160/1607412.js", "/wdr/wdr_video5.js", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/160/1607412/,1607412_18526624,1607412_18526625,1607412_18526622,1607412_18526621,1607412_18526626,1607412_18526623,.mp4.csmil/master.m3u8", "/wdr/wdr_video5.m3u8"},});

  }

  private final String htmlFile;
  private final String expectedTheme;
  private final String expectedTitle;
  private final String expectedDescription;
  private final String expectedWebsite;
  private final String expectedVideoUrlSmall;
  private final String expectedVideoUrlNormal;
  private final String expectedVideoUrlHd;
  private final String expectedSubtitle;
  private final String expectedDate;
  private final String expectedTime;
  private final String expectedDuration;

  private final WdrVideoDetailsDeserializer target;

  public WdrVideoDetailsDeserializerTest(String aHtmlFile, String aTheme, String aTitle, String aDescription, String aWebsite, String aVideoUrlNormal, String aVideoUrlSmall, String aVideoUrlHd, String aSubtitle, String aDate, String aTime, String aDuration, String aJsUrl, String aJsFile, String aM3u8Url, String aM3u8File) {
    htmlFile = aHtmlFile;
    expectedDate = aDate;
    expectedDescription = aDescription;
    expectedDuration = aDuration;
    expectedTheme = aTheme;
    expectedTime = aTime;
    expectedTitle = aTitle;
    expectedSubtitle = aSubtitle;
    expectedVideoUrlSmall = aVideoUrlSmall;
    expectedVideoUrlNormal = aVideoUrlNormal;
    expectedVideoUrlHd = aVideoUrlHd;
    expectedWebsite = aWebsite;

    WdrUrlLoaderMock urlLoader = new WdrUrlLoaderMock();
    urlLoader.setUp(aJsUrl, aJsFile);
    urlLoader.setUp(aM3u8Url, aM3u8File);

    target = new WdrVideoDetailsDeserializer(urlLoader.get());
  }

  @Test
  public void deserializeTestWithVideo() {
    String html = TestFileReader.readFile(htmlFile);
    Document document = Jsoup.parse(html);

    DatenFilm actual = target.deserialize(expectedTheme, document);

    assertThat(actual, notNullValue());
    assertThat(actual.arr[DatenFilm.FILM_SENDER], equalTo(Const.WDR));
    assertThat(actual.arr[DatenFilm.FILM_THEMA], equalTo(expectedTheme));
    assertThat(actual.arr[DatenFilm.FILM_TITEL], equalTo(expectedTitle));
    assertThat(actual.arr[DatenFilm.FILM_BESCHREIBUNG] + "|" + expectedDescription, actual.arr[DatenFilm.FILM_BESCHREIBUNG], equalTo(expectedDescription));
    assertThat(actual.arr[DatenFilm.FILM_WEBSEITE], equalTo(expectedWebsite));
    assertThat(actual.arr[DatenFilm.FILM_DATUM], equalTo(expectedDate));
    assertThat(actual.arr[DatenFilm.FILM_ZEIT], equalTo(expectedTime));
    assertThat(actual.arr[DatenFilm.FILM_DAUER], equalTo(expectedDuration));
    assertThat(actual.getUrl(), equalTo(expectedVideoUrlNormal));
    assertThat(actual.arr[DatenFilm.FILM_URL_KLEIN], equalTo(expectedVideoUrlSmall));
    assertThat(actual.arr[DatenFilm.FILM_URL_HD], equalTo(expectedVideoUrlHd));
    assertThat(actual.getUrlSubtitle(), equalTo(expectedSubtitle));
  }
}
