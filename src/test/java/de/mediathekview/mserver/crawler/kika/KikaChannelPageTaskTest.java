package de.mediathekview.mserver.crawler.kika;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.kika.tasks.KikaChannelPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


class KikaChannelPageTaskTest extends WireMockTestBase {
  
  @Test
  void testAssetExtraction() {
    setupSuccessfulJsonResponse("/KikaChannel", "/kika/KikaChannel.json");
    Set<KikaEntityDto> result = executeTask("/KikaChannel");
    List<KikaEntityDto> expectedResult = generateExpectedResult();
    assertEquals(expectedResult.size(), result.size(), "Number of extracted films per channel");
    assertIterableEquals(expectedResult, result, "Url link does not match");
  }
  
  private Set<KikaEntityDto> executeTask(String... requestUrl) {
    final Queue<KikaEntityDto> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new KikaEntityDto(
          Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),
          Optional.of(getWireMockBaseUrlSafe() + url),
          Optional.empty()
      ));
    }
    return new KikaChannelPageTask(createCrawler(), input, 9999).invoke();
  }
  
  private List<KikaEntityDto> generateExpectedResult() {
    return new ArrayList<>(asList(
        new KikaEntityDto(
            Optional.of("videoSubchannel"),
            Optional.of("alle-folgen-934"),
            Optional.of("9933fa02-e74f-4553-a7f9-b3dbbb517281"),
            Optional.of("alle-folgen-934"),
            Optional.of("/schloss-einstein/schloss-einstein/videos"),
            Optional.of("schloss-einstein-staffel-achtundzwanzig-100"),
            Optional.of("https://www.kika.de/ackley/v1/videosubchannels/alle-folgen-934"),
            Optional.of("2026-04-15T12:11:10.500+02:00"))
    ));
  }
  
  protected KikaCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    final MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");
    return new KikaCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
