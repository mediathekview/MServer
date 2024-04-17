package de.mediathekview.mserver.crawler.orfon;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.mediathekview.mlib.daten.*;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnEpisodeTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.AssertFilm;
import java.net.URL;
import java.time.Duration;
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

public class OrfOnEpisodeTaskTest extends OrfOnEpisodesTaskTest {
  
  protected static OrfOnCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new OrfOnCrawler(forkJoinPool, nachrichten, fortschritte, new MServerConfigManager("MServer-JUnit-Config.yaml"));
  }
  
  @Test
  public void testNormal_1() {
    setupSuccessfulJsonResponse("/episode1", "/orfOn/episode_1.json");
    Set<Film> result = executeTask("/episode1");
    assertTrue(result.size() == 1);
    Film actual = result.toArray(new Film[1])[0];
    //
    try {
    assertEquals("Servus Kasperl: Kasperl & Strolchi: Koko und Maximilian",actual.getTitel());
    assertEquals("Servus Kasperl",actual.getThema());
    assertEquals(LocalDateTime.of(2024,01,04,07,07,38),actual.getTime());
    assertEquals("Kasperl und Strolchi besuchen den Zirkusdirektor des Zirkus Kindleroni. Dieser ist verzweifelt, weil sein Stallbursche krank ist und er die ganze Arbeit alleine kaum schaffen kann. Doch da kommt Hilfe durch Maximilian, der Arbeit sucht. Sofort darf Maximilian die Stelle als Stallbursche antreten. Eine seiner Aufgaben ist es auch auf Koko, den Papagei des Direktors, aufzupassen. Dieser ist in Gefah",actual.getBeschreibung().substring(0,400));
    assertEquals(Duration.parse("PT21M56S"),actual.getDuration());
    assertEquals(Optional.of(new URL("https://tvthek.orf.at/profile/Servus-Kasperl/3272601/Servus-Kasperl-Kasperl-Strolchi-Koko-und-Maximilian/14207792")),actual.getWebsite());
    assertTrue(List.of(GeoLocations.GEO_NONE).containsAll(actual.getGeoLocations()));
    assertTrue(Set.of(
        new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/07aead27b4c0b09b36750db54b8ce15ff9b8499c.ttml"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/4dd6932d7cf6ceaad90a536c3e03981267e32941.vtt"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/09477f02c5e0392ecd2f717461ed136237d70050.srt"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/808e2453d38fd8a5e4fab4794cc034ee89fcfd9c.xml"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/8547597f31429d87a5418918ae471b44fcf9ab55.smi")
        ).containsAll(actual.getSubtitles()));
    assertEquals(Map.of(
        Resolution.HD, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2024-01-04_0707_tl_01_Servus-Kasperl-_____14207792__o__6332192865__s15543049_9__ORF1HD_07081012P_07300711P_Q8C.mp4/playlist.m3u8", 0L),
        Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2024-01-04_0707_tl_01_Servus-Kasperl-_____14207792__o__6332192865__s15543049_9__ORF1HD_07081012P_07300711P_Q6A.mp4/playlist.m3u8", 0L),
        Resolution.SMALL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2024-01-04_0707_tl_01_Servus-Kasperl-_____14207792__o__6332192865__s15543049_9__ORF1HD_07081012P_07300711P_Q4A.mp4/playlist.m3u8", 0L),
        Resolution.VERY_SMALL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2024-01-04_0707_tl_01_Servus-Kasperl-_____14207792__o__6332192865__s15543049_9__ORF1HD_07081012P_07300711P_Q1A.3gp/playlist.m3u8", 0L)
        ), actual.getUrls());
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  public void testNormal_2() {
    setupSuccessfulJsonResponse("/episode2", "/orfOn/episode_2.json");
    Set<Film> result = executeTask("/episode2");
    assertTrue(result.size() == 1);
    Film actual = result.toArray(new Film[1])[0];
    //
    try {
    assertEquals("ABC Bär",actual.getTitel());
    assertEquals("ABC-Bär",actual.getThema());
    assertEquals(LocalDateTime.of(2024,01,04,06,45),actual.getTime());
    assertEquals("Der ABC Bär und seine Tierfreunde reisen mit ihrem lustigen Baumhaus durch das Land, um ihre Zahl- und Buchstabenspiele aufzuführen und erleben dabei jede Menge spannender Geschichten.  Bildquelle: ORF",actual.getBeschreibung());
    assertEquals(Duration.parse("PT13M29S"),actual.getDuration());
    assertEquals(Optional.of(new URL("https://tvthek.orf.at/profile/ABC-Baer/4611813/ABC-Baer/14207790")),actual.getWebsite());
    assertTrue(List.of(GeoLocations.GEO_AT).containsAll(actual.getGeoLocations()));
    assertTrue(Set.of(
        new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/b682365a2a3fd1d45a2f029a597735a9df5b7524.ttml"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/20c53ed98f58a5045da663191516bc7fbf09e3d2.srt"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/c50d4ed5c4f912e8d7f7b5e94cd9155bd31b247d.vtt"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/1f178ebeae4aea09e03697e85082701de6df436c.xml"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0166/92/7096926d900006d0d196e1dab952a62200118c2c.smi")
        ).containsAll(actual.getSubtitles()));
    assertEquals(Map.of(
        Resolution.HD, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2024-01-04_0645_tl_00_ABC-Baer_____14207790__o__4346842346__s15542921_1__KIDS1_06363007P_06500003P_Q8C.mp4/playlist.m3u8", 0L),
        Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2024-01-04_0645_tl_00_ABC-Baer_____14207790__o__4346842346__s15542921_1__KIDS1_06363007P_06500003P_Q6A.mp4/playlist.m3u8", 0L),
        Resolution.SMALL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2024-01-04_0645_tl_00_ABC-Baer_____14207790__o__4346842346__s15542921_1__KIDS1_06363007P_06500003P_Q4A.mp4/playlist.m3u8", 0L),
        Resolution.VERY_SMALL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2024-01-04_0645_tl_00_ABC-Baer_____14207790__o__4346842346__s15542921_1__KIDS1_06363007P_06500003P_Q1A.3gp/playlist.m3u8", 0L)
        ), actual.getUrls());
    } catch (Exception e) {
      assertTrue(false);
    }
  }
  
  @Test
  public void testZib() {
    setupSuccessfulJsonResponse("/zib", "/orfOn/episode_zib.json");
    Set<Film> result = executeTask("/zib");
    assertTrue(result.size() == 1);
    Film actual = result.toArray(new Film[1])[0];

    AssertFilm.assertEquals(
        actual,
        Sender.ORF,
        "ZIB 13:00",
        "ZIB 13:00 vom 20.03.2024",
        LocalDateTime.of(2024, 3, 20, 13, 0, 0),
        Duration.ofSeconds(1177),
        "Wohnbaupaket passiert Nationalrat | ORF-Analyse: Details zum Wohnbaupaket | Neue Lehrerausbildung kommt ein Jahr später | Agrarprodukte aus Ukraine werden wieder verzollt | ORF-Analyse: Zölle auf Landwirtschaftsgüter aus Ukraine | London: Zweiter Anlauf für \"Ruanda-Plan\" | Vorschau: GB stimmt über \"Ruanda-Plan\"  ab | 2023: Über 1.300 Vorfälle von Rassismus in Österreich | Rekordhoch bei Insolvenze\n.....",
        "https://tvthek.orf.at/profile/ZIB-1300/71280/ZIB-1300-vom-20-03-2024/14218665",
        new GeoLocations[] { GeoLocations.GEO_NONE},
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/14218665_0017_Q4A.mp4/playlist.m3u8",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/14218665_0017_Q6A.mp4/playlist.m3u8",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/14218665_0017_Q8C.mp4/playlist.m3u8",
        "https://api-tvthek.orf.at/assets/subtitles/0171/59/69a0deabec546a7fb5fabc7ebb44e55a031987ac.ttml");
  }
  
  @Test
  public void testAD() {
    setupSuccessfulJsonResponse("/episodeAD", "/orfOn/episode_ad.json");
    Set<Film> result = executeTask("/episodeAD");
    assertTrue(result.size() == 1);
    Film actual = result.toArray(new Film[1])[0];
    //
    try {
    assertEquals("Vorstadtweiber: Folge 18 (Audiodeskription)",actual.getTitel());
    assertEquals("Vorstadtweiber Staffel 2",actual.getThema());
    assertEquals(LocalDateTime.of(2023,12,06,23,07,52),actual.getTime());
    assertEquals("Chaos wegen der Entführung von Waltrauds Baby. Die betrunkene Sylvia sagt, sie habe Simon vor der Türe gesehen. Waltraud ist sofort klar, dass nur Simon der Entführer sein kann. Den trifft sie zuhause, wo er gerade packt, um abzuhauen. Er beteuert, das Baby nicht entführt zu haben. Währenddessen rast Vanessa mit ihrem Wagen durch die Stadt, verheult, überdreht, ein Baby am Rücksitz. Daheim angekom",actual.getBeschreibung().substring(0,400));
    assertEquals(Duration.parse("PT46M32S"),actual.getDuration());
    assertEquals(Optional.of(new URL("https://tvthek.orf.at/profile/AD-Vorstadtweiber-Staffel-2/13895877/AD-Vorstadtweiber-Folge-18/14204417")),actual.getWebsite());
    assertTrue(List.of(GeoLocations.GEO_AT).containsAll(actual.getGeoLocations()));
    assertTrue(Set.of(
        new URL("https://api-tvthek.orf.at/assets/subtitles/0164/35/3c283187a44f9759f83a6731220549dc485d332d.srt"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0164/35/d7726579282af4a4eb7f13bcd0716b0ddd769e78.vtt"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0164/35/6146c97d9eb3437f89e05640188093453ad3e1af.smi"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0164/35/cae06428d2dd0a03f6c0a81dbd7695f0b096187d.ttml"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0164/35/275b919125cabcf1bbb0a3f1b38ec0c460367ae6.xml")
        ).containsAll(actual.getSubtitles()));
    // [, , , , ]
    assertEquals(Map.of(
        Resolution.HD, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2023-12-06_2307_sd_01_AD---Vorstadtwe_____14204417__o__8792736916__s15524266_6__ORF1ADHD_23092909P_23560117P_Q8C.mp4/playlist.m3u8", 0L),
        Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2023-12-06_2307_sd_01_AD---Vorstadtwe_____14204417__o__8792736916__s15524266_6__ORF1ADHD_23092909P_23560117P_Q6A.mp4/playlist.m3u8", 0L),
        Resolution.SMALL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2023-12-06_2307_sd_01_AD---Vorstadtwe_____14204417__o__8792736916__s15524266_6__ORF1ADHD_23092909P_23560117P_Q4A.mp4/playlist.m3u8", 0L),
        Resolution.VERY_SMALL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2023-12-06_2307_sd_01_AD---Vorstadtwe_____14204417__o__8792736916__s15524266_6__ORF1ADHD_23092909P_23560117P_Q1A.3gp/playlist.m3u8", 0L)
        ), actual.getUrls());
    // {HD=, NORMAL=, SMALL=, VERY_SMALL=}
    } catch (Exception e) {
      assertTrue(false);
    }
  }
  
  @Test
  public void testArchive() {
    setupSuccessfulJsonResponse("/episodeArchive", "/orfOn/episode_archive.json");
    Set<Film> result = executeTask("/episodeArchive");
    assertTrue(result.size() == 1);
    Film actual = result.toArray(new Film[1])[0];
    //
    try {
    assertEquals("ORF-Mitarbeiter in Quarantäne",actual.getTitel());
    assertEquals("Best of \"ZIB 2\"-Interviews",actual.getThema());
    assertEquals(LocalDateTime.of(2020,03,25,13,10),actual.getTime());
    assertEquals("Der ORF hat während der Coronakrise 2020 eine besondere Vorsichtsmaßnahme getroffen, um den Betrieb sicherzustellen. Einige Mitarbeiter und Moderatoren des Senders sind vorübergehend in das ORF-Zentrum am Küniglberg gezogen. Dort wird in \"Sperrzonen\" gearbeitet und der Sendebetrieb aufrecht erhalten.     Sendung: Mittag in Österreich  Gestaltung: Stefan Schlager",actual.getBeschreibung());
    assertEquals(Duration.parse("PT3M30S"),actual.getDuration());
    assertEquals(Optional.of(new URL("https://tvthek.orf.at/profile/Archiv/7648449/ORF-Mitarbeiter-in-Quarantaene/14046198")),actual.getWebsite());
    assertTrue(List.of(GeoLocations.GEO_NONE).containsAll(actual.getGeoLocations()));
    assertTrue(Set.of(
        new URL("https://api-tvthek.orf.at/assets/subtitles/0149/07/169001a65997f4d747fff2e4390a1f48789f388e.smi"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0149/07/fcaa2c529b8a6524f2fe58820e734dba44aab0ba.ttml"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0149/07/dacc52722cffd5bc466a231c66aeb2c01033a24d.vtt"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0149/07/1fc835e5ea31f903e9e5aa136085d4835bb8f81f.srt"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0149/07/f198c7d0c39a12039bd3a7f20a371acc0b7e8168.xml")
        ).containsAll(actual.getSubtitles()));
    assertEquals(Map.of(
        Resolution.HD, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2020-03-26_1310_sd_02_ORF-Mitarbeiter_____14046198__o__8302694905__s14668903_3__ORF3HD_13420114P_13453123P_Q8C.mp4/playlist.m3u8", 0L),
        Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2020-03-26_1310_sd_02_ORF-Mitarbeiter_____14046198__o__8302694905__s14668903_3__ORF3HD_13420114P_13453123P_Q6A.mp4/playlist.m3u8", 0L),
        Resolution.SMALL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2020-03-26_1310_sd_02_ORF-Mitarbeiter_____14046198__o__8302694905__s14668903_3__ORF3HD_13420114P_13453123P_Q4A.mp4/playlist.m3u8", 0L),
        Resolution.VERY_SMALL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-worldwide/2020-03-26_1310_sd_02_ORF-Mitarbeiter_____14046198__o__8302694905__s14668903_3__ORF3HD_13420114P_13453123P_Q1A.3gp/playlist.m3u8", 0L)
        ), actual.getUrls());
    } catch (Exception e) {
      assertTrue(false);
    }
  }
  
  @Test
  public void testDummyUrls() {
    setupSuccessfulJsonResponse("/episodeDummyUrl", "/orfOn/episode_noDrm.json");
    setupSuccessfulJsonResponse("/cms-austria/online/6b2d672267c81e196472b564abf8c8fe/1713132000/2024-03-13_2349_in_01_Spektakulaere-R_____14216629__o__1333685799__s15595990_Q8C.mp4", "/orfOn/episode_noDrm.json");
    setupSuccessfulJsonResponse("/cms-austria/online/de9bd8775f46ea293a9db4b0711d4de5/1713132000/2024-03-13_2349_in_01_Spektakulaere-R_____14216629__o__1333685799__s15595990_Q6A.mp4", "/orfOn/episode_noDrm.json");
    setupSuccessfulJsonResponse("/cms-austria/online/a96476a0eab40b11ef517feefe0d2973/1713132000/2024-03-13_2349_in_01_Spektakulaere-R_____14216629__o__1333685799__s15595990_Q4A.mp4", "/orfOn/episode_noDrm.json");
    setupSuccessfulJsonResponse("/cms-austria/online/440102b9f68434fbb577d17114dd9182/1713132000/2024-03-13_2349_in_01_Spektakulaere-R_____14216629__o__1333685799__s15595990_Q1A.3gp", "/orfOn/episode_noDrm.json");
    setupHeadRequestForFileSize();
    //
    Set<Film> result = executeTask("/episodeDummyUrl");
    assertTrue(result.size() == 1);
    Film actual = result.toArray(new Film[1])[0];
    //
    try {
    assertEquals("Spektakuläre Raubüberfälle mit Pierce Brosnan: Bankeinbruch in Kalifornien",actual.getTitel());
    assertEquals("Spektakuläre Raubüberfälle mit Pierce Brosnan",actual.getThema());
    assertEquals(LocalDateTime.of(2024,03,13,23,49,50),actual.getTime());
    assertEquals("Pierce Brosnan präsentiert in der spannenden Krimi-Doku-Reihe die spektakulärsten Raubüberfälle der Geschichte.",actual.getBeschreibung());
    assertEquals(Duration.parse("PT40M46S"),actual.getDuration());
    assertEquals(Optional.of(new URL("https://tvthek.orf.at/profile/Spektakulaere-Raubueberfaelle-mit-Pierce-Brosnan/13896153/Spektakulaere-Raubueberfaelle-mit-Pierce-Brosnan-Bankeinbruch-in-Kalifornien/14216629")),actual.getWebsite());
    assertTrue(List.of(GeoLocations.GEO_AT).containsAll(actual.getGeoLocations()));
    assertTrue(Set.of(
        new URL("https://api-tvthek.orf.at/assets/subtitles/0171/15/e83f6eabbdbcf49e894d1a58d77fcf3a9b951f3c.smi"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0171/15/c95472a931fe3ea8407734f95df242bce2f06b09.ttml"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0171/15/13759cf0d408fe11965ff16d03a564bbabbf5bc1.vtt"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0171/15/e2536ac2b80a5152e54b047073ab327c223579ec.srt"),
        new URL("https://api-tvthek.orf.at/assets/subtitles/0171/15/be922c3765a25ec3d7fa13e9265dabad7f986b75.xml")
        ).containsAll(actual.getSubtitles()));
    assertEquals(Map.of(
        Resolution.HD, new FilmUrl(getWireMockBaseUrlSafe()+"/cms-austria/online/6b2d672267c81e196472b564abf8c8fe/1713132000/2024-03-13_2349_in_01_Spektakulaere-R_____14216629__o__1333685799__s15595990_Q8C.mp4", 0L),
        Resolution.NORMAL, new FilmUrl(getWireMockBaseUrlSafe()+"/cms-austria/online/de9bd8775f46ea293a9db4b0711d4de5/1713132000/2024-03-13_2349_in_01_Spektakulaere-R_____14216629__o__1333685799__s15595990_Q6A.mp4", 0L),
        Resolution.SMALL, new FilmUrl(getWireMockBaseUrlSafe()+"/cms-austria/online/a96476a0eab40b11ef517feefe0d2973/1713132000/2024-03-13_2349_in_01_Spektakulaere-R_____14216629__o__1333685799__s15595990_Q4A.mp4", 0L),
        Resolution.VERY_SMALL, new FilmUrl(getWireMockBaseUrlSafe()+"/cms-austria/online/440102b9f68434fbb577d17114dd9182/1713132000/2024-03-13_2349_in_01_Spektakulaere-R_____14216629__o__1333685799__s15595990_Q1A.3gp", 0L)
        ), actual.getUrls());
    } catch (Exception e) {
      assertTrue(false);
    }
    
    
    
  }
  
  private Set<Film> executeTask(String... requestUrl) {
    final Queue<OrfOnBreadCrumsUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new OrfOnBreadCrumsUrlDTO("",getWireMockBaseUrlSafe() + url));
    }
    return new OrfOnEpisodeTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
  }
  
  
  
}
