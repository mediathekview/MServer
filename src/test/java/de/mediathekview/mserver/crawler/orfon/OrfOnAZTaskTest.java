package de.mediathekview.mserver.crawler.orfon;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.mediathekview.mserver.crawler.orfon.task.OrfOnAZTask;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


class OrfOnAZTaskTest extends WireMockTestBase {
  
  @Test
  void test() {
    setupSuccessfulJsonResponse("/azTask", "/orfOn/letter_1.json");
    Set<OrfOnBreadCrumsUrlDTO> result = executeTask("/azTask");
    List<OrfOnBreadCrumsUrlDTO> expectedResult = generateExpectedResult();
    assertEquals(9, result.size());
    assertIterableEquals(result, expectedResult);
  }
  
  private Set<OrfOnBreadCrumsUrlDTO> executeTask(String... requestUrl) {
    final Queue<OrfOnBreadCrumsUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new OrfOnBreadCrumsUrlDTO("",getWireMockBaseUrlSafe() + url));
    }
    return new OrfOnAZTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
  }
  
  private List<OrfOnBreadCrumsUrlDTO> generateExpectedResult() {
    ArrayList<OrfOnBreadCrumsUrlDTO> expectedResult = new ArrayList<>(asList(
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/profile/13892414/episodes?limit=200"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/profile/8850620/episodes?limit=200"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/profile/13895942/episodes?limit=200"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/profile/8850612/episodes?limit=200"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/profile/13890803/episodes?limit=200"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/profile/13894465/episodes?limit=200"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/profile/13732047/episodes?limit=200"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/profile/13895899/episodes?limit=200"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/profile/13887359/episodes?limit=200")));
    return expectedResult;
  }
}
