package de.mediathekview.mserver.crawler.arte;

import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigDTO;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.daten.Sender;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ArteCrawlTaskTest {
  @Test
  void deduplicatesAndFiltersBeforeResolvingWithAtMostFourWorkers() {
    var crawler = mock(AbstractCrawler.class);
    var config = new MServerBasicConfigDTO();
    config.setSocketTimeoutInSeconds(5);
    config.setMaximumSubpages(1);
    var runtime = mock(MServerConfigDTO.class);
    when(runtime.getMaximumCpuThreads()).thenReturn(12);
    when(crawler.getRuntimeConfig()).thenReturn(runtime);
    when(crawler.getCrawlerConfig()).thenReturn(config);
    when(crawler.getSender()).thenReturn(Sender.ARTE_DE);
    when(crawler.filterExistingFilms(anyCollection(), any())).thenAnswer(invocation -> {
      Collection<ArteCatalogue.Programme> programmes = invocation.getArgument(0);
      assertEquals(13, programmes.size());
      return new ArrayDeque<>(programmes.stream().filter(p -> !p.programmeId().equals("118267-000-A")).toList());
    });
    String entries = IntStream.range(0, 13).mapToObj(i ->
        "{\"id\":\"118267-%03d-A_de\"}".formatted(i)).collect(java.util.stream.Collectors.joining(","));
    String root = "{\"zones\":[{\"content\":{\"data\":[" + entries + "]}},{\"content\":{\"data\":[" + entries + "]}}]}";
    var active = new AtomicInteger();
    var maximum = new AtomicInteger();
    var calls = new ConcurrentLinkedQueue<String>();
    try (var transport = mockConstruction(ArteHttpClient.class, (http, _) -> {
      when(http.get(anyString())).thenAnswer(invocation -> {
        String url = invocation.getArgument(0);
        calls.add(url);
        if (url.endsWith("ALL_VIDEOS")) return root;
        int running = active.incrementAndGet();
        maximum.accumulateAndGet(running, Math::max);
        try {
          Thread.sleep(20);
          String id = url.substring(url.lastIndexOf('/') + 1);
          return """
              {"data":{"attributes":{"metadata":{"title":"%s"},"streams":[
              {"url":"https://cdn.example/%s.m3u8","versions":[{"audioLanguage":"de"}]}]}}}
              """.formatted(id, id);
        } finally {
          active.decrementAndGet();
        }
      });
    })) {
      var films = new ArteCrawlTask(crawler, "de").invoke();
      assertEquals(12, films.size());
      assertTrue(maximum.get() <= 4);
      assertEquals(13, calls.size());
      assertTrue(calls.stream().noneMatch(s -> s.endsWith("118267-000-A")));
      verify(crawler).getAndSetMaxCount(12);
      verify(crawler, times(12)).incrementAndGetActualCount();
      verify(crawler, never()).determineFileSizeInKB(anyString());
      verify(crawler, never()).incrementAndGetErrorCount();
      verify(transport.constructed().getFirst()).close();
    }
  }
  @Test
  void cancellationInterruptsResolutionAndClosesTransportWithoutReportingAnError() throws Exception {
    var crawler = mock(AbstractCrawler.class);
    var config = new MServerBasicConfigDTO();
    config.setSocketTimeoutInSeconds(5);
    config.setMaximumSubpages(1);
    var runtime = mock(MServerConfigDTO.class);
    when(runtime.getMaximumCpuThreads()).thenReturn(1);
    when(crawler.getRuntimeConfig()).thenReturn(runtime);
    when(crawler.getCrawlerConfig()).thenReturn(config);
    when(crawler.getSender()).thenReturn(Sender.ARTE_DE);
    when(crawler.filterExistingFilms(anyCollection(), any())).thenAnswer(invocation ->
        new ArrayDeque<>((Collection<?>) invocation.getArgument(0)));
    var resolving = new CountDownLatch(1);
    var interrupted = new AtomicBoolean();
    var task = new ArteCrawlTask(crawler, "de");
    try (var transport = mockConstruction(ArteHttpClient.class, (http, _) -> {
      when(http.get(anyString())).thenAnswer(invocation -> {
        String url = invocation.getArgument(0);
        if (url.endsWith("ALL_VIDEOS")) return """
            {"data":[{"id":"118267-006-A_de"}]}
            """;
        resolving.countDown();
        try {
          new CountDownLatch(1).await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
          interrupted.set(true);
          Thread.currentThread().interrupt();
          throw new CancellationException("worker interrupted");
        }
        return "";
      });
    })) {
      var canceller = Thread.ofPlatform().start(() -> {
        try {
          if (resolving.await(5, TimeUnit.SECONDS)) task.cancel(true);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
      try {
        assertThrows(CancellationException.class, task::invoke);
      } finally {
        canceller.join();
      }
      assertTrue(interrupted.get());
      verify(transport.constructed().getFirst(), atLeastOnce()).close();
      verify(crawler, never()).incrementAndGetErrorCount();
    }
  }

}
