package de.mediathekview.mserver.crawler.orfon;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

import org.junit.Test;

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnEpisodeTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;


public class OrfOnEpisodeTaskTest extends OrfOnEpisodesTaskTest {
  
  @Test
  public void test() {
    setupSuccessfulJsonResponse("/episode1", "/orfOn/episode_1.json");
    setupSuccessfulJsonResponse("/episode2", "/orfOn/episode_2.json");
    Set<OrfOnVideoInfoDTO> result = executeTask("/episode1", "/episode2");
    Map<String, OrfOnVideoInfoDTO> expectedResult = generateExpectedResult();
    assertTrue(result.size() == 2);
    for (OrfOnVideoInfoDTO actual : result) {
      OrfOnVideoInfoDTO expected = expectedResult.get(actual.getId().get());
      assertNotNull(expected);
      assertVideoInfoDto(expected, actual);
    }
  }
  
  private Set<OrfOnVideoInfoDTO> executeTask(String... requestUrl) {
    final Queue<OrfOnBreadCrumsUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new OrfOnBreadCrumsUrlDTO("",getWireMockBaseUrlSafe() + url));
    }
    return new OrfOnEpisodeTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
  }
  
  private Map<String, OrfOnVideoInfoDTO> generateExpectedResult() {
    try {
      Map<String,OrfOnVideoInfoDTO> expectedResult = Map.of(
          "14207792", new OrfOnVideoInfoDTO(
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
                ))),
            "14207790", new OrfOnVideoInfoDTO(
                Optional.of("14207790"),
                Optional.of("ORF Kids"),
                Optional.of("ABC Bär"),
                Optional.of("ABC Bär vom 04.01.2024 um 06:45 Uhr"),
                Optional.of("ABC-Bär"),
                Optional.of("ORF Kids | Lernen mit Spaß"),
                Optional.of(LocalDateTime.of(2024,01,04,06,45)),
                Optional.of(java.time.Duration.parse("PT13M29S")),
                Optional.of("Der ABC Bär und seine Tierfreunde reisen mit ihrem lustigen Baumhaus durch das Land, um ihre Zahl- und Buchstabenspiele aufzuführen und erleben dabei jede Menge spannender Geschichten.\r\n"
                    + "Bildquelle: ORF"),
                Optional.of(new URL("https://tvthek.orf.at/profile/ABC-Baer/4611813/ABC-Baer/14207790")),
                Optional.of(List.of(GeoLocations.GEO_AT)),
                Optional.of(new URL("https://api-tvthek.orf.at/api/v4.3/subtitle/885332")),
                Optional.of(Map.of(Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2024-01-04_0645_tl_00_ABC-Baer_____14207790__o__4346842346__s15542921_1__KIDS1_06363007P_06500003P_QXB.mp4/playlist.m3u8", 0L))),
                Optional.of(Set.of(
                    new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/b682365a2a3fd1d45a2f029a597735a9df5b7524.ttml"),
                    new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/20c53ed98f58a5045da663191516bc7fbf09e3d2.srt"),
                    new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/c50d4ed5c4f912e8d7f7b5e94cd9155bd31b247d.vtt"),
                    new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/1f178ebeae4aea09e03697e85082701de6df436c.xml"),
                    new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/7096926d900006d0d196e1dab952a62200118c2c.smi")
                    )))
        );
      return expectedResult;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  protected static void assertVideoInfoDto(OrfOnVideoInfoDTO expected, OrfOnVideoInfoDTO actual) {
    assertEquals(expected.getAired(), actual.getAired());
    assertEquals(expected.getChannel(), actual.getChannel());
    assertEquals(expected.getDescription(), actual.getDescription());
    assertEquals(expected.getDuration(), actual.getDuration());
    assertEquals(expected.getGeorestriction(), actual.getGeorestriction());
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getTitle(), actual.getTitle());
    assertEquals(expected.getTitleWithDate(), actual.getTitleWithDate());
    assertEquals(expected.getTopic(), actual.getTopic());
    assertEquals(expected.getTopicForArchive(), actual.getTopicForArchive());
    assertEquals(expected.getWebsite(), actual.getWebsite());
    assertEquals(expected.getVideoUrls().get(), actual.getVideoUrls().get());
    assertEquals(expected.getSubtitleUrls(), actual.getSubtitleUrls());
    if (expected.getSubtitleSource().isPresent()) {
      int subtitleSourceExpectedStringlength = expected.getSubtitleSource().get().toString().indexOf("subtitle");
      int subtitleSourceActStringlength = actual.getSubtitleSource().get().toString().indexOf("subtitle");
      assertTrue(subtitleSourceExpectedStringlength > 0 && subtitleSourceActStringlength > 0);
      assertEquals(expected.getSubtitleSource().get().toString().substring(subtitleSourceExpectedStringlength), actual.getSubtitleSource().get().toString().substring(subtitleSourceActStringlength)); // ignore localhost+port
    }
  }
  
  protected static OrfOnCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new OrfOnCrawler(forkJoinPool, nachrichten, fortschritte, new MServerConfigManager("MServer-JUnit-Config.yaml"));
  }
  
  
  
}
