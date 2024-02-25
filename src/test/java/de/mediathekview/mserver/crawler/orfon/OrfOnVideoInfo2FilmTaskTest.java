package de.mediathekview.mserver.crawler.orfon;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;

import de.mediathekview.mserver.crawler.orfon.task.OrfOnVideoInfo2FilmTask;

public class OrfOnVideoInfo2FilmTaskTest {
  
  
  @Test
  public void testNormal() throws MalformedURLException {
    OrfOnVideoInfoDTO inputVideoInfoDTO = new OrfOnVideoInfoDTO(
        Optional.of("14207792"),
        Optional.of("ORF 1"),
        Optional.of("Servus Kasperl: Kasperl & Strolchi: Koko und Maximilian"),
        Optional.of("Servus Kasperl: Kasperl und Strolchi: Koko und Maximilian vom 04.01.2024 um 07:07 Uhr"),
        Optional.of("Servus Kasperl"),
        Optional.of("ORF Kids | Servus Kasperl"),
        Optional.of(LocalDateTime.of(2024,01,04,07,07,38)),
        Optional.of(java.time.Duration.parse("PT21M56S")),
        Optional.of("Kasperl und Strolchi besuchen den Zirkusdirektor des Zirkus Kindleroni. Dieser ist verzweifelt, weil sein Stallbursche krank ist und er die ganze Arbeit alleine kaum schaffen kann. Doch da kommt Hilfe durch Maximilian, der Arbeit sucht. Sofort darf Maximilian die Stelle als Stallbursche antreten. Eine seiner Aufgaben ist es auch auf Koko, den Papagei des Direktors, aufzupassen. Dieser ist in Gefahr, weil ein Räuber vor hat ihn zu stehlen.\r\n"
            + "Bildquelle: ORF"),
        Optional.of(new URL("https://tvthek.orf.at/profile/Servus-Kasperl/3272601/Servus-Kasperl-Kasperl-Strolchi-Koko-und-Maximilian/14207792")),
        Optional.of(List.of(GeoLocations.GEO_NONE)),
        Optional.of(new URL("https://api-tvthek.orf.at/api/v4.3/subtitle/885340")),
        Optional.of(Map.of(Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2024-01-04_0707_tl_01_Servus-Kasperl-_____14207792__o__6332192865__s15543049_9__ORF1HD_07081012P_07300711P_QXB.mp4/playlist.m3u8", 0L))),
        Optional.of(Set.of(
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/07aead27b4c0b09b36750db54b8ce15ff9b8499c.ttml"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/4dd6932d7cf6ceaad90a536c3e03981267e32941.vtt"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/09477f02c5e0392ecd2f717461ed136237d70050.srt"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/808e2453d38fd8a5e4fab4794cc034ee89fcfd9c.xml"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/8547597f31429d87a5418918ae471b44fcf9ab55.smi")
            )));
    final Queue<OrfOnVideoInfoDTO> input = new ConcurrentLinkedQueue<>();
    input.add(inputVideoInfoDTO);
    Set<Film> resultSet = new OrfOnVideoInfo2FilmTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
    Film result = resultSet.toArray(new Film[0])[0];
    //
    
    assertEquals(inputVideoInfoDTO.getTitle().get(),result.getTitel());
    assertEquals(inputVideoInfoDTO.getTopic().get(),result.getThema());
    assertEquals(inputVideoInfoDTO.getAired().get(),result.getTime());
    if (inputVideoInfoDTO.getDescription().get().length() > 405) {
      assertEquals(inputVideoInfoDTO.getDescription().get().substring(0, 400),result.getBeschreibung().substring(0, 400));
    } else {
      assertEquals(inputVideoInfoDTO.getDescription().get(),result.getBeschreibung());
    }
    assertEquals(inputVideoInfoDTO.getDuration().get(),result.getDuration());
    assertEquals(inputVideoInfoDTO.getWebsite(),result.getWebsite());
    assertEquals(inputVideoInfoDTO.getVideoUrls().get(),result.getUrls());
    assertTrue(inputVideoInfoDTO.getSubtitleUrls().get().containsAll(result.getSubtitles()));
    assertTrue(inputVideoInfoDTO.getGeorestriction().get().containsAll(result.getGeoLocations()));
    
  }


  @Test
  public void testAD() throws MalformedURLException {
    OrfOnVideoInfoDTO inputVideoInfoDTO = new OrfOnVideoInfoDTO(
        Optional.of("14207792"),
        Optional.of("ORF 1"),
        Optional.of("AD | Der Bergdoktor: Im Schatten"),
        Optional.of("Servus Kasperl: Kasperl und Strolchi: Koko und Maximilian vom 04.01.2024 um 07:07 Uhr"),
        Optional.of("AD | Der Bergdoktor Staffel 17"),
        Optional.of("ORF Kids | Servus Kasperl"),
        Optional.of(LocalDateTime.of(2024,01,04,07,07,38)),
        Optional.of(java.time.Duration.parse("PT21M56S")),
        Optional.of("Kasperl und Strolchi besuchen den Zirkusdirektor des Zirkus Kindleroni. Dieser ist verzweifelt, weil sein Stallbursche krank ist und er die ganze Arbeit alleine kaum schaffen kann. Doch da kommt Hilfe durch Maximilian, der Arbeit sucht. Sofort darf Maximilian die Stelle als Stallbursche antreten. Eine seiner Aufgaben ist es auch auf Koko, den Papagei des Direktors, aufzupassen. Dieser ist in Gefahr, weil ein Räuber vor hat ihn zu stehlen.\r\n"
            + "Bildquelle: ORF"),
        Optional.of(new URL("https://tvthek.orf.at/profile/Servus-Kasperl/3272601/Servus-Kasperl-Kasperl-Strolchi-Koko-und-Maximilian/14207792")),
        Optional.of(List.of(GeoLocations.GEO_NONE)),
        Optional.of(new URL("https://api-tvthek.orf.at/api/v4.3/subtitle/885340")),
        Optional.of(Map.of(Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2024-01-04_0707_tl_01_Servus-Kasperl-_____14207792__o__6332192865__s15543049_9__ORF1HD_07081012P_07300711P_QXB.mp4/playlist.m3u8", 0L))),
        Optional.of(Set.of(
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/07aead27b4c0b09b36750db54b8ce15ff9b8499c.ttml"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/4dd6932d7cf6ceaad90a536c3e03981267e32941.vtt"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/09477f02c5e0392ecd2f717461ed136237d70050.srt"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/808e2453d38fd8a5e4fab4794cc034ee89fcfd9c.xml"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/8547597f31429d87a5418918ae471b44fcf9ab55.smi")
            )));
    final Queue<OrfOnVideoInfoDTO> input = new ConcurrentLinkedQueue<>();
    input.add(inputVideoInfoDTO);
    Set<Film> resultSet = new OrfOnVideoInfo2FilmTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
    Film result = resultSet.toArray(new Film[0])[0];
    //
    
    assertEquals("Der Bergdoktor: Im Schatten (Audiodeskription)",result.getTitel());
    assertEquals("Der Bergdoktor Staffel 17",result.getThema());
    assertEquals(inputVideoInfoDTO.getAired().get(),result.getTime());
    if (inputVideoInfoDTO.getDescription().get().length() > 405) {
      assertEquals(inputVideoInfoDTO.getDescription().get().substring(0, 400),result.getBeschreibung().substring(0, 400));
    } else {
      assertEquals(inputVideoInfoDTO.getDescription().get(),result.getBeschreibung());
    }
    assertEquals(inputVideoInfoDTO.getDuration().get(),result.getDuration());
    assertEquals(inputVideoInfoDTO.getWebsite(),result.getWebsite());
    assertEquals(inputVideoInfoDTO.getVideoUrls().get(),result.getUrls());
    assertTrue(inputVideoInfoDTO.getSubtitleUrls().get().containsAll(result.getSubtitles()));
    assertTrue(inputVideoInfoDTO.getGeorestriction().get().containsAll(result.getGeoLocations()));
    
  }
  
  @Test
  public void testArchive() throws MalformedURLException {
    OrfOnVideoInfoDTO inputVideoInfoDTO = new OrfOnVideoInfoDTO(
        Optional.of("14207792"),
        Optional.of("ORF 1"),
        Optional.of("Servus Kasperl: Kasperl & Strolchi: Koko und Maximilian"),
        Optional.of("Servus Kasperl: Kasperl und Strolchi: Koko und Maximilian vom 04.01.2024 um 07:07 Uhr"),
        Optional.of("Archiv"),
        Optional.of("Better Archive Title"),
        Optional.of(LocalDateTime.of(2024,01,04,07,07,38)),
        Optional.of(java.time.Duration.parse("PT21M56S")),
        Optional.of("Kasperl und Strolchi besuchen den Zirkusdirektor des Zirkus Kindleroni. Dieser ist verzweifelt, weil sein Stallbursche krank ist und er die ganze Arbeit alleine kaum schaffen kann. Doch da kommt Hilfe durch Maximilian, der Arbeit sucht. Sofort darf Maximilian die Stelle als Stallbursche antreten. Eine seiner Aufgaben ist es auch auf Koko, den Papagei des Direktors, aufzupassen. Dieser ist in Gefahr, weil ein Räuber vor hat ihn zu stehlen.\r\n"
            + "Bildquelle: ORF"),
        Optional.of(new URL("https://tvthek.orf.at/profile/Servus-Kasperl/3272601/Servus-Kasperl-Kasperl-Strolchi-Koko-und-Maximilian/14207792")),
        Optional.of(List.of(GeoLocations.GEO_NONE)),
        Optional.of(new URL("https://api-tvthek.orf.at/api/v4.3/subtitle/885340")),
        Optional.of(Map.of(Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2024-01-04_0707_tl_01_Servus-Kasperl-_____14207792__o__6332192865__s15543049_9__ORF1HD_07081012P_07300711P_QXB.mp4/playlist.m3u8", 0L))),
        Optional.of(Set.of(
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/07aead27b4c0b09b36750db54b8ce15ff9b8499c.ttml"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/4dd6932d7cf6ceaad90a536c3e03981267e32941.vtt"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/09477f02c5e0392ecd2f717461ed136237d70050.srt"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/808e2453d38fd8a5e4fab4794cc034ee89fcfd9c.xml"),
            new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/8547597f31429d87a5418918ae471b44fcf9ab55.smi")
            )));
    final Queue<OrfOnVideoInfoDTO> input = new ConcurrentLinkedQueue<>();
    input.add(inputVideoInfoDTO);
    Set<Film> resultSet = new OrfOnVideoInfo2FilmTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
    Film result = resultSet.toArray(new Film[0])[0];
    //
    assertEquals(inputVideoInfoDTO.getTitle().get(),result.getTitel());
    assertEquals(inputVideoInfoDTO.getTopicForArchive().get(),result.getThema()); // HERE
    assertEquals(inputVideoInfoDTO.getAired().get(),result.getTime());
    if (inputVideoInfoDTO.getDescription().get().length() > 405) {
      assertEquals(inputVideoInfoDTO.getDescription().get().substring(0, 400),result.getBeschreibung().substring(0, 400));
    } else {
      assertEquals(inputVideoInfoDTO.getDescription().get(),result.getBeschreibung());
    }
    assertEquals(inputVideoInfoDTO.getDuration().get(),result.getDuration());
    assertEquals(inputVideoInfoDTO.getWebsite(),result.getWebsite());
    assertEquals(inputVideoInfoDTO.getVideoUrls().get(),result.getUrls());
    assertTrue(inputVideoInfoDTO.getSubtitleUrls().get().containsAll(result.getSubtitles()));
    assertTrue(inputVideoInfoDTO.getGeorestriction().get().containsAll(result.getGeoLocations()));
    
  }
}
