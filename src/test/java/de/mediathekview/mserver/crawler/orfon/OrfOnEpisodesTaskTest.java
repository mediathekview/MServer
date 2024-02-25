package de.mediathekview.mserver.crawler.orfon;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnEpisodesTask;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

public class OrfOnEpisodesTaskTest extends WireMockTestBase {

  @Test
  public void test() {
    setupSuccessfulJsonResponse("/episodes", "/orfOn/episodes_3.json");
    setupSuccessfulJsonResponse("/api/v4.3/subtitle/868782", "/orfOn/subtitle_868782.json");
    Set<OrfOnVideoInfoDTO> result = executeTask("/episodes");
    Map<String, OrfOnVideoInfoDTO> expectedResult = generateExpectedResult();
    assertTrue(result.size() == 3);
    for (OrfOnVideoInfoDTO actual : result) {
      OrfOnVideoInfoDTO expected = expectedResult.get(actual.getId().get());
      assertNotNull(expected);
      OrfOnEpisodeTaskTest.assertVideoInfoDto(expected, actual);
    }
  }
  
  private Set<OrfOnVideoInfoDTO> executeTask(String... requestUrl) {
    final Queue<OrfOnBreadCrumsUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new OrfOnBreadCrumsUrlDTO("",getWireMockBaseUrlSafe() + url));
    }
    return new OrfOnEpisodesTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
  }
  
  private Map<String, OrfOnVideoInfoDTO> generateExpectedResult() {
    try {
      Map<String,OrfOnVideoInfoDTO> expectedResult = Map.of(
          "14201133", new OrfOnVideoInfoDTO(
            Optional.of("14201133"),
            Optional.of("ORF 1"),
            Optional.of("Wischen ist Macht: Alles für den Hugo"),
            Optional.of("Wischen ist Macht: Alles für den Hugo vom 14.11.2023 um 00:33 Uhr"),
            Optional.of("Wischen ist Macht"),
            Optional.of("Serie"),
            Optional.of(LocalDateTime.of(2023,11,14,00,33,03)),
            Optional.of(java.time.Duration.parse("PT24M50S")),
            Optional.of("Spezialauftrag für \"Dreck.Weg.Sendracek\": Die russische Botschafterin lädt zu einer Gala und"),
            Optional.of(new URL("https://tvthek.orf.at/profile/Wischen-ist-Macht/13891227/Wischen-ist-Macht-Alles-fuer-den-Hugo/14201133")),
            Optional.of(List.of(GeoLocations.GEO_AT)),
            Optional.of(new URL("https://api-tvthek.orf.at/api/v4.3/subtitle/868782")),
            Optional.of(Map.of(Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2023-11-14_0033_in_01_Wischen-ist-Mac_____14201133__o__1340615864__s15523109_QXB.mp4/playlist.m3u8", 0L))),
            Optional.of(Set.of(
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/a1ab1313ca03ba35c75d39e08c59840bc97aba76.ttml"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/6cd8a1899f5b56f919a6d809fe001ce3acaf4ce8.smi"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/79738b7eb874c151cde04ec6367a444bd5999db1.srt"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/a0467e2fdfb637c00fe6a450f003cedcf70128fa.xml"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/4b1c81a007271ee90224a9555494148e3116a00e.vtt")
                ))
        ), 
        "14202094", new OrfOnVideoInfoDTO(
            Optional.of("14202094"),
            Optional.of("ORF 1"),
            Optional.of("Wischen ist Macht: Shit Happens"),
            Optional.of("Wischen ist Macht: Shit Happens vom 21.11.2023 um 00:04 Uhr"),
            Optional.of("Wischen ist Macht"),
            Optional.of("Wischen ist Macht"),
            Optional.of(LocalDateTime.of(2023,11,21,00,04,21)),
            Optional.of(java.time.Duration.parse("PT22M12S")),
            Optional.of("Dass Ex-Rockstar Johnny Woody gerade seinen Abgang ins Jenseits plant, passt Michelle gar nicht in den Kram - hat er ihr doch immer noch nicht die ausstehenden Honorare überwiesen. Johnny fängt sich jedoch wieder und \"Dreck:Weg.Sendracek\" treten ihren Dienst an. Bei der Arbeit findet Michelle heraus, dass Johnny eine 10.000 Dollar-Gitarre besitzt und er vielleicht doch nicht so knapp bei Kasse ist, woraufhin sie einen Plan schmiedet. Dann aber wird Michelle in die Beziehungskrise zwischen Johnny und seine Freundin Pamela hineingezogen, während seine Frau Janis gewohnt alkoholisiert durchs Haus geistert.\r\n"
                + "Mit Ursula Strauss (Michelle Sendracek), Stefano Bernardin (Fernando Pablo Rigoberto Sanchez de la Luz), Zeynep Buyrac (Mira Petrenko), Manuel Sefciuc (Valentin Gradischnig), Lilian Jane Gartner (Zoe), Wolfram Berger (Johnny), Eva Maria Marold (Janis), Doris Hindinger (Pamela), Helmut Bohatsch (Bertram) u.a.\r\n"
                + "Bildquelle: ORF/Fabio Eppensteiner"),
            Optional.of(new URL("https://tvthek.orf.at/profile/Wischen-ist-Macht/13891227/Wischen-ist-Macht-Shit-Happens/14202094")),
            Optional.of(List.of(GeoLocations.GEO_AT)),
            Optional.of(new URL("https://api-tvthek.orf.at/api/v4.3/subtitle/868782")),
            Optional.of(Map.of(Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2023-11-21_0004_in_01_Wischen-ist-Mac_____14202094__o__1166314613__s15511282_QXB.mp4/playlist.m3u8", 0L))),
            Optional.of(Set.of(
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/a1ab1313ca03ba35c75d39e08c59840bc97aba76.ttml"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/6cd8a1899f5b56f919a6d809fe001ce3acaf4ce8.smi"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/79738b7eb874c151cde04ec6367a444bd5999db1.srt"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/a0467e2fdfb637c00fe6a450f003cedcf70128fa.xml"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/4b1c81a007271ee90224a9555494148e3116a00e.vtt")
                ))
        ),
        "14202095", new OrfOnVideoInfoDTO(
            Optional.of("14202095"),
            Optional.of("ORF 1"),
            Optional.of("Wischen ist Macht: Fußball ist meine Religion"),
            Optional.of("Wischen ist Macht: Fußball ist meine Religion vom 21.11.2023 um 00:27 Uhr"),
            Optional.of("Wischen ist Macht"),
            Optional.of("Serie"),
            Optional.of(LocalDateTime.of(2023,11,21,00,27,17)),
            Optional.of(java.time.Duration.parse("PT25M49S")),
            Optional.of("Michelle und ihr Team rücken im Fußballstadion an"),
            Optional.of(new URL("https://tvthek.orf.at/profile/Wischen-ist-Macht/13891227/Wischen-ist-Macht-Fussball-ist-meine-Religion/14202095")),
            Optional.of(List.of(GeoLocations.GEO_AT)),
            Optional.of(new URL("https://api-tvthek.orf.at/api/v4.3/subtitle/868782")),
            Optional.of(Map.of(Resolution.NORMAL, new FilmUrl("https://apasfiis.sf.apa.at/ipad/cms-austria/2023-11-21_0027_in_01_Wischen-ist-Mac_____14202095__o__7468851165__s15511283_QXB.mp4/playlist.m3u8", 0L))),
            Optional.of(Set.of(
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/a1ab1313ca03ba35c75d39e08c59840bc97aba76.ttml"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/6cd8a1899f5b56f919a6d809fe001ce3acaf4ce8.smi"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/79738b7eb874c151cde04ec6367a444bd5999db1.srt"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/a0467e2fdfb637c00fe6a450f003cedcf70128fa.xml"),
                new URL("https://api-tvthek.orf.at/assets/subtitles/0162/100/4b1c81a007271ee90224a9555494148e3116a00e.vtt")
                ))
        ));
      return expectedResult;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }
  

}
