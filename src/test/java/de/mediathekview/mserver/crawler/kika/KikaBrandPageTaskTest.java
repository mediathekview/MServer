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
import de.mediathekview.mserver.crawler.kika.tasks.KikaBrandPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


class KikaBrandPageTaskTest extends WireMockTestBase {
  
  @Test
  void testAssetExtraction() {
    setupSuccessfulJsonResponse("/KikaBrandPageTask", "/kika/KikaBrand.json");
    Set<KikaEntityDto> result = executeTask("/KikaBrandPageTask");
    List<KikaEntityDto> expectedResult = generateExpectedResult();

    assertEquals(1, result.size(), "Added one brand");
    assertEquals(expectedResult.size(), result.size(), "Brand link must be identical");

    KikaEntityDto actual = result.stream().findFirst()
        .orElseThrow(() -> new AssertionError("Expected exactly one result but found none"));
    KikaEntityDto expected = expectedResult.get(0);

    assertEquals(expected.getDocType(), actual.getDocType(), "docType must match");
    assertEquals(expected.getId(), actual.getId(), "id must match");
    assertEquals(expected.getUuid(), actual.getUuid(), "uuid must match");
    assertEquals(expected.getExternalId(), actual.getExternalId(), "externalId must match");
    assertEquals(expected.getUrlPath(), actual.getUrlPath(), "urlPath must match");
    assertEquals(expected.getApiId(), actual.getApiId(), "apiId must match");
    assertEquals(expected.getUrl(), actual.getUrl(), "url must match");
    assertEquals(expected.getModificationDate(), actual.getModificationDate(), "modificationDate must match");
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
    return new KikaBrandPageTask(createCrawler(), input).invoke();
  }
  
  private List<KikaEntityDto> generateExpectedResult() {
    return new ArrayList<>(asList(
        new KikaEntityDto(
            Optional.of("broadcastSeries"),                        // docType
            Optional.of("abgetaucht-meine-falschen-ferien-100"),   // id
            Optional.of("5d71dd74-5502-4c35-b4f2-5253e6845de4"),   // uuid
            Optional.of("content-70906"),                          // externalId
            Optional.of("/abgetaucht-meine-falschen-ferien"),      // urlPath
            Optional.of("alle-folgen-894"),                        // apiId
            Optional.of("https://www.kika.de/ackley/v1/videosubchannels/alle-folgen-894/videos?page=0&videoType=mainContent&platform=kikade"), // url
            Optional.of("2026-06-23T14:35:11.001+02:00"))          // modificationDate
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
