package de.mediathekview.mserver.filmlisten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.FilmUrl;
import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.filmlisten.reader.FilmlistOldFormatReader;
import de.mediathekview.mserver.filmlisten.writer.FilmlistOldFormatWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FilmlistOldFormatWriterTest {
  @TempDir
  Path tempDir;
  
  @Test
  void readFilmlistOldFormatIncludingBrokenRecords()
      throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    final Path testFilePath = new File(classLoader.getResource("mlib/TestFilmlistOldFormatWriter.json").getFile()).toPath();
    Optional<Filmlist> testFilmlist = new FilmlistOldFormatReader().read(new FileInputStream(testFilePath.toString()));
    assertTrue( testFilmlist.isPresent());
    //
    Path tempFile = Files.createTempFile(tempDir, "TestFilmlistOldFormatWriter", ".out");
    new FilmlistOldFormatWriter().write(testFilmlist.get(), tempFile);
    
    assertTrue( Files.exists(tempFile));
    //
    String actualData = Files.readString(tempFile, StandardCharsets.UTF_16).substring(100);
    String expectedData = Files.readString(testFilePath, StandardCharsets.UTF_16).substring(100);
    assertEquals(expectedData, actualData, "Filmlisten stimmen überein.");
    //
    Files.deleteIfExists(tempFile);

  }
  
  @Test
  void readWriteFilmlistAndCheckAllUrlTypesDoPersist()
      throws IOException {
    //
    Filmlist input = inputFilmlist();
    Filmlist expected = expectedFilmlist();
    //
    Path tempFile = Files.createTempFile(tempDir, "TestFilmlistOldFormatWriter", ".out");
    new FilmlistOldFormatWriter().write(input, tempFile);
    assertTrue( Files.exists(tempFile));
    //
    Optional<Filmlist> afterWriteAndRead = new FilmlistOldFormatReader().read(new FileInputStream(tempFile.toString()));
    Files.deleteIfExists(tempFile);
    //
    assertEquals(expected.getFilms().size(), afterWriteAndRead.get().getFilms().size());
    // 
    ArrayList<Film> aFilmlist = new ArrayList<>(expected.getFilms().values());
    ArrayList<Film> bFilmlist = new ArrayList<>(afterWriteAndRead.get().getFilms().values());
    //
    aFilmlist.forEach( expectedFilm -> {
      int index = bFilmlist.indexOf(expectedFilm);
      assertTrue(index > -1);
      compareFilmIncludingShifterUrls(expectedFilm, bFilmlist.get(index));
    });
    
  }
  
  private void compareFilmIncludingShifterUrls(Film aFilm, Film bFilm) {
    assertEquals(aFilm.getSenderName() , bFilm.getSenderName());
    assertEquals(aFilm.getTitel() , bFilm.getTitel());
    assertEquals(aFilm.getThema() , bFilm.getThema());
    assertEquals(aFilm.getDuration() , bFilm.getDuration());
    assertEquals(aFilm.getWebsite() , bFilm.getWebsite());
    assertEquals(aFilm.getBeschreibung(), bFilm.getBeschreibung());
    assertEquals(aFilm.getGeoLocations(), bFilm.getGeoLocations());
    assertEquals(aFilm.getSubtitles(), bFilm.getSubtitles());
    assertEquals(aFilm.getTime(), bFilm.getTime());
    // after write read all videos will be urls (not audiodescriptions etc)
    bFilm.getUrls().forEach( (resolution, url) -> {
      if (aFilm.getUrl(resolution) != null) {
        assertEquals(aFilm.getUrl(resolution).toString(), url.toString());
      }
    });
  }
  
  
  private Filmlist expectedFilmlist() {
    Filmlist expectedFilmlist = new Filmlist();
    try {
      Film completeMatchExpected = new Film();
      //
      completeMatchExpected.setUuid(UUID.randomUUID());
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Ein Gen verändert unser Leben");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(45));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 30, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 405L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expectedFilmlist.add(completeMatchExpected);
      //
      completeMatchExpected = new Film();
      completeMatchExpected.setUuid(UUID.randomUUID());
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Ein Gen verändert unser Leben (Audiodeskription)");      
      completeMatchExpected.setBeschreibung("description \"37°\" with quotes");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(41));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 16, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addAudioDescription(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionNormal.mp4").toURL(), 405L));
      completeMatchExpected.addAudioDescription(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionSmall.mp4").toURL(), 0L));
      completeMatchExpected.addAudioDescription(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionHd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expectedFilmlist.add(completeMatchExpected);
      //
      completeMatchExpected = new Film();
      completeMatchExpected.setUuid(UUID.randomUUID());
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Ein Gen verändert unser Leben (Gebärdensprache)");      
      completeMatchExpected.setBeschreibung("description \"37°\" with quotes");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(41));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 16, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addSignLanguage(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/SignLanguageNormal.mp4").toURL(), 405L));
      completeMatchExpected.addSignLanguage(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/SignLanguageSmall.mp4").toURL(), 0L));
      completeMatchExpected.addSignLanguage(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/SignLanguageHd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expectedFilmlist.add(completeMatchExpected);
      //
      completeMatchExpected = new Film();
      completeMatchExpected.setUuid(UUID.randomUUID());
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Everything at once");      
      completeMatchExpected.setBeschreibung("description \"37°\" with quotes");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(41));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 16, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 405L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expectedFilmlist.add(completeMatchExpected);
      //
      completeMatchExpected = new Film();
      completeMatchExpected.setUuid(UUID.randomUUID());
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Everything at once (Audiodeskription)");      
      completeMatchExpected.setBeschreibung("description \"37°\" with quotes");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(41));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 16, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionNormal.mp4").toURL(), 405L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionSmall.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionHd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expectedFilmlist.add(completeMatchExpected);
      //
      completeMatchExpected = new Film();
      completeMatchExpected.setUuid(UUID.randomUUID());
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Everything at once (Gebärdensprache)");      
      completeMatchExpected.setBeschreibung("description \"37°\" with quotes");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(41));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 16, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/SignLanguageNormal.mp4").toURL(), 405L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/SignLanguageSmall.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/SignLanguageHd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expectedFilmlist.add(completeMatchExpected);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    return expectedFilmlist;
  }
  
  private Filmlist inputFilmlist() {
    Filmlist expectedFilmlist = new Filmlist();
    try {
      Film completeMatchExpected = new Film();
      //
      completeMatchExpected.setUuid(UUID.randomUUID());
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Ein Gen verändert unser Leben");      
      completeMatchExpected.setBeschreibung("description");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(45));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 30, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 405L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expectedFilmlist.add(completeMatchExpected);
      //
      completeMatchExpected = new Film();
      completeMatchExpected.setUuid(UUID.randomUUID());
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Ein Gen verändert unser Leben (Audiodeskription)");      
      completeMatchExpected.setBeschreibung("description \"37°\" with quotes");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(41));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 16, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addAudioDescription(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionNormal.mp4").toURL(), 405L));
      completeMatchExpected.addAudioDescription(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionSmall.mp4").toURL(), 0L));
      completeMatchExpected.addAudioDescription(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionHd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expectedFilmlist.add(completeMatchExpected);
      //
      completeMatchExpected = new Film();
      completeMatchExpected.setUuid(UUID.randomUUID());
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Ein Gen verändert unser Leben (Gebärdensprache)");      
      completeMatchExpected.setBeschreibung("description \"37°\" with quotes");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(41));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 16, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addSignLanguage(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/SignLanguageNormal.mp4").toURL(), 405L));
      completeMatchExpected.addSignLanguage(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/SignLanguageSmall.mp4").toURL(), 0L));
      completeMatchExpected.addSignLanguage(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/SignLanguageHd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expectedFilmlist.add(completeMatchExpected);
      //
      completeMatchExpected = new Film();
      completeMatchExpected.setUuid(UUID.randomUUID());
      completeMatchExpected.setSender(Sender.DREISAT);
      completeMatchExpected.setThema("37 Grad");
      completeMatchExpected.setTitelRaw("37°: Everything at once");      
      completeMatchExpected.setBeschreibung("description \"37°\" with quotes");
      completeMatchExpected.setDuration(Duration.ofMinutes(28).plusSeconds(41));
      completeMatchExpected.setTime(LocalDateTime.of(2023, 5, 16, 22, 15, 0));
      completeMatchExpected.setWebsite(URI.create("https://host.de/something/website.html").toURL());
      completeMatchExpected.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/normal.mp4").toURL(), 405L));
      completeMatchExpected.addUrl(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/small.mp4").toURL(), 0L));
      completeMatchExpected.addUrl(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/hd.mp4").toURL(), 0L));
      completeMatchExpected.addAudioDescription(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionNormal.mp4").toURL(), 405L));
      completeMatchExpected.addAudioDescription(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionSmall.mp4").toURL(), 0L));
      completeMatchExpected.addAudioDescription(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/AudioDescriptionHd.mp4").toURL(), 0L));
      completeMatchExpected.addSignLanguage(Resolution.NORMAL, new FilmUrl(URI.create("https://some.host.de/SignLanguageNormal.mp4").toURL(), 405L));
      completeMatchExpected.addSignLanguage(Resolution.SMALL, new FilmUrl(URI.create("https://some.host.de/SignLanguageSmall.mp4").toURL(), 0L));
      completeMatchExpected.addSignLanguage(Resolution.HD, new FilmUrl(URI.create("https://some.host.de/SignLanguageHd.mp4").toURL(), 0L));
      completeMatchExpected.addGeolocation(GeoLocations.GEO_DE_AT_CH);
      completeMatchExpected.addSubtitle(URI.create("https://host.de/23/05/230502_2215_sendung_37g/4/subtitle.xml").toURL());
      expectedFilmlist.add(completeMatchExpected);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return expectedFilmlist;
  }
  
}
