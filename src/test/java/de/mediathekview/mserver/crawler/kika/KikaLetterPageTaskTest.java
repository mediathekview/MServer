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
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import de.mediathekview.mserver.crawler.kika.tasks.KikaLetterPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


class KikaLetterPageTaskTest extends WireMockTestBase {
  
  @Test
  void testAssetExtraction() {
    setupSuccessfulJsonResponse("/KikaLetter", "/kika/KikaLetter.json");
    Set<KikaEntityDto> result = executeTask("/KikaLetter");
    List<KikaEntityDto> expectedResult = generateExpectedResult();
    assertEquals(expectedResult.size(), result.size(), "Number of extracted entities per letter");
    assertIterableEquals(expectedResult, result, "Url link does not match");
  }
  
  private Set<KikaEntityDto> executeTask(String... requestUrl) {
    final Queue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new CrawlerUrlDTO(getWireMockBaseUrlSafe() + url));
    }
    return new KikaLetterPageTask(createCrawler(), input, 9999).invoke();
  }
  
  private List<KikaEntityDto> generateExpectedResult() {
    return new ArrayList<>(asList(
        new KikaEntityDto(
            Optional.of("broadcastSeries"),
            Optional.of("geronimo-stilton-100"),
            Optional.of("f5e9644c-752f-4637-94d5-f3c18a213eff"),
            Optional.of("content-989"),
            Optional.of("/geronimo-stilton"),
            Optional.of("geronimo-stilton-100"),
            Optional.of("https://www.kika.de/ackley/v1/brands/geronimo-stilton-100"),
            Optional.of("2026-05-18T17:32:11.201+02:00")),

        new KikaEntityDto(
            Optional.of("broadcastSeries"),
            Optional.of("goat-girl-100"),
            Optional.of("59a321ca-378b-4477-a843-253507b11fc5"),
            Optional.of("content-87003"),
            Optional.of("/goat-girl"),
            Optional.of("goat-girl-100"),
            Optional.of("https://www.kika.de/ackley/v1/brands/goat-girl-100"),
            Optional.of("2026-06-11T12:18:38.917+02:00")),

        new KikaEntityDto(
            Optional.of("broadcastSeries"),
            Optional.of("gluecksbaerchis-willkommen-im-wolkenland-100"),
            Optional.of("386d2d8a-caeb-41c9-8b45-b391c21443bc"),
            Optional.of("content-48318"),
            Optional.of("/gluecksbaerchis-willkommen-im-wolkenland"),
            Optional.of("gluecksbaerchis-willkommen-im-wolkenland-100"),
            Optional.of("https://www.kika.de/ackley/v1/brands/gluecksbaerchis-willkommen-im-wolkenland-100"),
            Optional.of("2026-05-04T20:48:23.988+02:00")),

        new KikaEntityDto(
            Optional.of("broadcastSeries"),
            Optional.of("gong-mein-spektrakulaeres-leben-100"),
            Optional.of("c9587bcb-f6e9-4f1a-b4ca-ef4dfa8ede97"),
            Optional.of("content-76493"),
            Optional.of("/gong-mein-spektrakulaeres-leben"),
            Optional.of("gong-mein-spektrakulaeres-leben-100"),
            Optional.of("https://www.kika.de/ackley/v1/brands/gong-mein-spektrakulaeres-leben-100"),
            Optional.of("2026-04-20T12:42:28.204+02:00")),

        new KikaEntityDto(
            Optional.of("broadcastSeries"),
            Optional.of("geschichten-von-ueberall-100"),
            Optional.of("f0442cf4-8a82-4950-bae4-e1d16dee036f"),
            Optional.of("content-45"),
            Optional.of("/geschichten-von-ueberall"),
            Optional.of("geschichten-von-ueberall-100"),
            Optional.of("https://www.kika.de/ackley/v1/brands/geschichten-von-ueberall-100"),
            Optional.of("2026-05-04T21:03:12.255+02:00")),

        new KikaEntityDto(
            Optional.of("broadcastSeries"),
            Optional.of("the-garfield-show-100"),
            Optional.of("e723a5f3-5435-4c59-b561-6b223710664d"),
            Optional.of("content-765"),
            Optional.of("/garfield"),
            Optional.of("the-garfield-show-100"),
            Optional.of("https://www.kika.de/ackley/v1/brands/the-garfield-show-100"),
            Optional.of("2026-04-16T15:07:03.103+02:00")),

        new KikaEntityDto(
            Optional.of("broadcastSeries"),
            Optional.of("grisu-der-kleine-drache-100"),
            Optional.of("186fde0d-de23-4454-9e97-7d889cd0b739"),
            Optional.of("content-77580"),
            Optional.of("/grisu"),
            Optional.of("grisu-der-kleine-drache-100"),
            Optional.of("https://www.kika.de/ackley/v1/brands/grisu-der-kleine-drache-100"),
            Optional.of("2026-04-14T11:18:49.995+02:00"))
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
