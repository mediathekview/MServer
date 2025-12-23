package de.mediathekview.mserver.crawler.arte.tasks;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import de.mediathekview.mserver.crawler.arte.json.ArteVideoInfoDto;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

public class ArteVideoInfoTaskTest extends WireMockTestBase {

  @Test
  public void test() {
    setupSuccessfulJsonResponse("/arte_videos1", "/arte/arte_videos_1.json");
    Set<ArteVideoInfoDto> result = executeTask("/arte_videos1");
    List<List<Optional<String>>> expectedResult = generateExpectedResult();
    for(List<Optional<String>> entry : expectedResult) {
      Optional<ArteVideoInfoDto> act = result.stream()
          .filter(item -> item.getId().equalsIgnoreCase(entry.get(0).get()))
          .findFirst();
      assertTrue(act.isPresent());
      assertEntry(act.get(), entry.toArray(new Optional[0]));
    }

  }
  
  private void assertEntry(ArteVideoInfoDto act, Optional<String>[] expected) {
    assertEquals(act.getId(), expected[0]);
    assertEquals(act.getKind(), expected[1]);
    assertEquals(act.getTitle(), expected[2]);
    assertEquals(act.getSubtitle(), expected[3]);
    assertEquals(act.getCategoryName(), expected[4]);
    assertEquals(act.getSubcategoryName(), expected[5]);
    assertEquals(act.getDurationSeconds(), expected[6]);
    assertEquals(act.getGeoblockingZone(), expected[7]);
    assertEquals(act.getWebsite(), expected[8]);
    assertEquals(act.getShortDescription(), expected[9]);
    assertEquals(act.getBroadcastBeginRounded(), expected[10]);
    assertEquals(act.getBroadcastBegin(), expected[11]);
    assertEquals(act.getFirstBroadcastDate(), expected[12]);
    assertEquals(act.getCreationDate(), expected[13]);
    
    
  }
  
  private Set<ArteVideoInfoDto> executeTask(String... requestUrl) {
    final Queue<TopicUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new TopicUrlDTO("",getWireMockBaseUrlSafe() + url));
    }
    return new ArteVideoInfoTask(ArteTaskTestBase.createCrawler(), input, 1).invoke();
  }
  
  private List<List<Optional<String>>> generateExpectedResult() {
    return List.of(
      List.of(
        Optional.of("121420-025-A_SHOW_ARTE_NEXT_DE_de"), //id
        Optional.of("SHOW"), // kind
        Optional.of("Merveille"), // title
        Optional.of("ARTE Dans le Club"), //  subtitle
        Optional.of("ARTE Concert"), // category
        Optional.of("Hip-Hop"), // subcategory
        Optional.of("1247"), // duration
        Optional.of("ALL"), // geoblocking
        Optional.of("https://www.arte.tv/de/videos/121420-025-A/merveille/"), // website
        Optional.of("2024 verhalf Tiakola der Sängerin Merveille zu einem ersten vielbeachteten Auftritt auf der Bühne von ARTE Dans le Club. Ein paar Monate später ist der einstige Newcomer der Star!"), // description
        Optional.empty(), // getBroadcastBeginRounded
        Optional.empty(), // getBroadcastBegin
        Optional.empty(), // getFirstBroadcastDate
        Optional.of("2025-06-13T13:03:44Z") // getCreationDate
      ),
      List.of(
        Optional.of("127525-000-A_BONUS_ARTE_NEXT_DE_de"), //id
        Optional.of("BONUS"), // kind
        Optional.of("Iran: Reaktionen vor Ort"), // title
        Optional.empty(), //  subtitle
        Optional.of("Aktuelles und Gesellschaft"), // category
        Optional.of("Aktuelles"), // subcategory
        Optional.of("113"), // duration
        Optional.of("ALL"), // geoblocking
        Optional.of("https://www.arte.tv/de/videos/127525-000-A/iran-reaktionen-vor-ort/"), // website
        Optional.of("Der israelische Gegenschlag auf die iranischen Luftangriffe ist massiv und dauert an. Er richtet sich in erster Linie gegen Atomanlagen und ranghohe Militäre des des Mullah-Regimes. Aber die Angriffe forderten auch zivile Opfer: Fast 100 Tote und mehr als 300 Verletzte. Wie reagieren regimetreue und regimekritische Iranerinnen und Iraner auf die israelischen Angriffe?"), // description
        Optional.empty(), // getBroadcastBeginRounded
        Optional.empty(), // getBroadcastBegin
        Optional.empty(), // getFirstBroadcastDate
        Optional.of("2025-06-14T17:59:14Z") // getCreationDate
      )
    );
    
  }
    
}
