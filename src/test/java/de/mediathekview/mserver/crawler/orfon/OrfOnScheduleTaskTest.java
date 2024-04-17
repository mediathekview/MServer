package de.mediathekview.mserver.crawler.orfon;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import de.mediathekview.mserver.crawler.orfon.task.OrfOnScheduleTask;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

public class OrfOnScheduleTaskTest extends WireMockTestBase {

  @Test
  public void test() {
    setupSuccessfulJsonResponse("/scheduleTask", "/orfOn/schedule.json");
    Set<OrfOnBreadCrumsUrlDTO> result = executeTask("/scheduleTask");
    assertIterableEquals(result, generateExpectedResult());
  }
  
  private Set<OrfOnBreadCrumsUrlDTO> executeTask(String... requestUrl) {
    final Queue<OrfOnBreadCrumsUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new OrfOnBreadCrumsUrlDTO("",getWireMockBaseUrlSafe() + url));
    }
    return new OrfOnScheduleTask(OrfOnEpisodeTaskTest.createCrawler(), input).invoke();
  }

  private List<OrfOnBreadCrumsUrlDTO> generateExpectedResult() {
    ArrayList<OrfOnBreadCrumsUrlDTO> expectedResult = new ArrayList<>(asList(
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213726"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213913"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213924"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213740"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213748"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213759"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213751"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213737"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213696"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213946"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213935"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14214038"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14214041"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213705"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213763"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213716"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213749"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213741"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213752"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213925"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213730"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213914"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213738"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213727"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213697"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213947"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213936"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14214040"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213742"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213728"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213720"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213717"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213926"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213915"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213731"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213706"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213753"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213739"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213698"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213940"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213937"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14214043"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213707"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213718"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213754"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213743"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213729"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213732"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213831"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213927"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213916"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213710"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213721"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213644"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213699"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213941"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213930"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213938"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14214039"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14214042"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213733"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213722"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213711"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213708"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213755"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213744"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213920"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213928"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213917"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213719"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213700"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213692"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213939"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213931"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213918"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213709"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213929"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213921"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213723"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213734"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213745"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213756"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213712"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213701"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213693"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213932"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213943"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14214044"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213746"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213724"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213757"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213900"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213735"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213911"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213922"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213919"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213713"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213702"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213672"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213694"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213933"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213944"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213750"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213714"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213901"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213912"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213923"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213725"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213736"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213747"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213758"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213703"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213695"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213934"),
        new OrfOnBreadCrumsUrlDTO("", "https://api-tvthek.orf.at/api/v4.3/episode/14213945")
        ));
    return expectedResult;
  }
}
