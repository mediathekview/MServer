package de.mediathekview.mserver.crawler.orfon;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import de.mediathekview.mserver.crawler.orfon.task.OrfOnHistoryTask;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

public class OrfOnHistoryTaskTest extends WireMockTestBase {
    

  
  @Test
  public void test() {
    setupSuccessfulJsonResponse("/history", "/orfOn/history.json");
    Set<OrfOnBreadCrumsUrlDTO> result = executeTask("/history");
    assertIterableEquals(result, generateExpectedResult());
  }
  
  private Set<OrfOnBreadCrumsUrlDTO> executeTask(String... requestUrl) {
    final Queue<OrfOnBreadCrumsUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new OrfOnBreadCrumsUrlDTO("",getWireMockBaseUrlSafe() + url));
    }
    return new OrfOnHistoryTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
  }
  
  private List<OrfOnBreadCrumsUrlDTO> generateExpectedResult() {
    ArrayList<OrfOnBreadCrumsUrlDTO> expectedResult = new ArrayList<>(asList(
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/13557964/children"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/13557948/children"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/7874678/children"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/13557914/children"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/13557911/children"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/13557913/children"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/13557916/children"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/13558010/children"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/13557912/children")
        ));
    return expectedResult;
  }
}
