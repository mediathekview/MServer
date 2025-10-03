package de.mediathekview.mserver.filmlisten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.FilmUrl;
import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.filmlisten.reader.FilmlistOldFormatReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FilmlistOldFormatReaderTest {

  @Test
  void readFilmlistOldFormatIncludingBrokenRecords()
      throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    final Path testFilePath = new File(classLoader.getResource("mlib/TestFilmlistOldFormatReader.json").getFile()).toPath();
    Optional<Filmlist> resultingList = new FilmlistOldFormatReader().read(new FileInputStream(testFilePath.toString()));
    assertTrue(resultingList.isPresent());
    //
    ArrayList<Film> expectedFilms = expectedFilmlist();
    resultingList.get().getFilms().values().forEach( f -> {
      assertTrue(expectedFilms.contains(f));
      Film expectedFilm = expectedFilms.get(expectedFilms.indexOf(f));
      assertEquals(f.getSender(), expectedFilm.getSender());
      assertEquals(f.getTitel(), expectedFilm.getTitel());
      assertEquals(f.getThema(), expectedFilm.getThema());
      assertEquals(f.getTime(), expectedFilm.getTime());
      assertEquals(f.getDuration(), expectedFilm.getDuration());
      assertEquals(f.getBeschreibung(), expectedFilm.getBeschreibung());
      assertEquals(f.getWebsite(), expectedFilm.getWebsite());
      assertEquals(f.getTime(), expectedFilm.getTime());
      assertEquals(f.getTime(), expectedFilm.getTime());
      assertIterableEquals(f.getSubtitles(), expectedFilm.getSubtitles());
      if (expectedFilm.getUrl(Resolution.SMALL) != null)
        assertEquals(f.getUrl(Resolution.SMALL).toString(), expectedFilm.getUrl(Resolution.SMALL).toString());
      if (expectedFilm.getUrl(Resolution.NORMAL) != null)
        assertEquals(f.getUrl(Resolution.NORMAL).toString(), expectedFilm.getUrl(Resolution.NORMAL).toString());
      if (expectedFilm.getUrl(Resolution.HD) != null)
        assertEquals(f.getUrl(Resolution.HD).toString(), expectedFilm.getUrl(Resolution.HD).toString());

      
      
    });
  }
  
  private ArrayList<Film> expectedFilmlist() {
    ArrayList<Film> expected = new ArrayList<Film>();
    try {
      // 1 element
      Film completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37° Totgerast (Audiodeskription)");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(45));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 30, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 405L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      // 2 element
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Ein Gen verändert unser Leben (Audiodeskription)");      
      completeMatchExpected.setBeschreibung("description \"37°\" with quotes");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(41));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 16, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 405L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      // 3,4,5 element > broken
      // 
      // 6 element
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 1");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(42));
      completeMatchExpected.setTime(LocalDateTime.of(2022, 10, 11, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 416L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      // 6 element DATE
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 2");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(42));
      completeMatchExpected.setTime(LocalDateTime.of(1970, 1, 1, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 416L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      // 7 element TIME
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 3");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(42));
      completeMatchExpected.setTime(LocalDateTime.of(2022, 10, 11, 0, 0, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 416L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      // 8 element DURATIN
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 4");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ZERO);
      completeMatchExpected.setTime(LocalDateTime.of(2022, 10, 11, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 416L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected); 
      // 9 element SIZE
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 5");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(42));
      completeMatchExpected.setTime(LocalDateTime.of(2022, 10, 11, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      // 10 element broken normal url
      // 11 element website
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 7");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(42));
      completeMatchExpected.setTime(LocalDateTime.of(2022, 10, 11, 22, 15, 0));
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      // 12 element subtitel
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 8");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(42));
      completeMatchExpected.setTime(LocalDateTime.of(2022, 10, 11, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      expected.add(completeMatchExpected);
      // 13 element small
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 9");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(42));
      completeMatchExpected.setTime(LocalDateTime.of(2022, 10, 11, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      // 14 element hd
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 10");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(42));
      completeMatchExpected.setTime(LocalDateTime.of(2022, 10, 11, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      // 15 element geo
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 11");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(42));
      completeMatchExpected.setTime(LocalDateTime.of(2022, 10, 11, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      // 16 element neu
      completeMatchExpected = new Film();
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Mein Tanz, mein Battle 12");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(42));
      completeMatchExpected.setTime(LocalDateTime.of(2022, 10, 11, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expected.add(completeMatchExpected);
      //
    } catch (Exception e) {
      // no exception
    }
    return expected;
    
  }
  
}
