package de.mediathekview.mserver.crawler.orfon;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;


import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import de.mediathekview.mserver.crawler.orfon.task.OrfOnEpisodesTask;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

public class OrfOnEpisodesTaskTest extends WireMockTestBase {

  @Test
  public void test() {
    setupSuccessfulJsonResponse("/episodes", "/orfOn/episodes_3.json");
    Set<OrfOnBreadCrumsUrlDTO> result = executeTask("/episodes");
    List<OrfOnBreadCrumsUrlDTO> expectedResult = generateExpectedResult();
    assertIterableEquals(result, expectedResult);
  }
  
  private Set<OrfOnBreadCrumsUrlDTO> executeTask(String... requestUrl) {
    final Queue<OrfOnBreadCrumsUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new OrfOnBreadCrumsUrlDTO("",getWireMockBaseUrlSafe() + url));
    }
    return new OrfOnEpisodesTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
  }
  
  private List<OrfOnBreadCrumsUrlDTO> generateExpectedResult() {
    ArrayList<OrfOnBreadCrumsUrlDTO> expectedResult = new ArrayList<>(asList(
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14201133"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14202095"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14202094")));
    return expectedResult;
  }
  

}
