package de.mediathekview.mserver.crawler.orfon;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import de.mediathekview.mserver.crawler.orfon.task.OrfOnHistoryChildrenTask;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

public class OrfOnHistoryChildrenTaskTest extends WireMockTestBase {

  @Test
  public void test() {
    setupSuccessfulJsonResponse("/children", "/orfOn/children_1.json");
    Set<OrfOnBreadCrumsUrlDTO> result = executeTask("/children");
    assertIterableEquals(result, generateExpectedResult());
  }
  
  private Set<OrfOnBreadCrumsUrlDTO> executeTask(String... requestUrl) {
    final Queue<OrfOnBreadCrumsUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new OrfOnBreadCrumsUrlDTO("",getWireMockBaseUrlSafe() + url));
    }
    return new OrfOnHistoryChildrenTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
  }

  private List<OrfOnBreadCrumsUrlDTO> generateExpectedResult() {
    ArrayList<OrfOnBreadCrumsUrlDTO> expectedResult = new ArrayList<>(asList(
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/13558011/videoitems?limit=200"),
        new OrfOnBreadCrumsUrlDTO("","https://api-tvthek.orf.at/api/v4.3/history/13558009/videoitems?limit=200")
        ));
    return expectedResult;
  }

}
