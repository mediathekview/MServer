package de.mediathekview.mserver.crawler.orfon;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import de.mediathekview.mserver.crawler.orfon.task.OrfOnHistoryVideoItemTask;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

public class OrfOnHistoryVideoItemTaskTest extends WireMockTestBase {

  @Test
  public void test() {
    setupSuccessfulJsonResponse("/videoItems", "/orfOn/videoItems_1.json");
    Set<OrfOnBreadCrumsUrlDTO> result = executeTask("/videoItems");
    assertIterableEquals(result, generateExpectedResult());
  }
  
  private Set<OrfOnBreadCrumsUrlDTO> executeTask(String... requestUrl) {
    final Queue<OrfOnBreadCrumsUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new OrfOnBreadCrumsUrlDTO("",getWireMockBaseUrlSafe() + url));
    }
    return new OrfOnHistoryVideoItemTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
  }
  
  private List<OrfOnBreadCrumsUrlDTO> generateExpectedResult() {
    ArrayList<OrfOnBreadCrumsUrlDTO> expectedResult = new ArrayList<>(asList(
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/episode/14070240"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/episode/5074881"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/episode/5068699"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/episode/13984127"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/episode/13984230"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/episode/13984132"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/episode/14003539"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/episode/13984130"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/episode/13984128")
        ));
    return expectedResult;
  }
}
