package de.mediathekview.mserver.crawler.br.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.br.BrClipQueryDto;
import de.mediathekview.mserver.crawler.br.BrConstants;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.crawler.br.BrQueryDto;
import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class BrBroadcastsTaskTest extends WireMockTestBase {

  protected MServerConfigManager rootConfig =
      MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  protected BrCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new BrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

  protected Queue<BrQueryDto> createQueryDto(final String requestUrl) {
    final Queue<BrQueryDto> input = new ConcurrentLinkedQueue<>();
    final LocalDate day = LocalDate.of(2021, 3, 15);
    input.add(
        new BrQueryDto(
            wireMockServer.baseUrl() + requestUrl,
            BrConstants.BROADCAST_SERVICE_BR,
            day,
            day,
            10,
            Optional.empty()));
    return input;
  }

  public Set<BrClipQueryDto> executeTask(String request) {
    return new BrBroadcastsTask(createCrawler(), createQueryDto(request)).invoke();
  }

  @Test
  public void noBroadcasts() {
    final String request = "/br/empty";
    setupSuccessfulJsonPostResponse(request, "/br/br_broadcast_empty.json");

    final Set<BrClipQueryDto> actual = executeTask(request);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }

  @Test
  public void singlePage() {
    final String request = "/br/single";
    setupSuccessfulJsonPostResponse(request, "/br/br_broadcast_single_page.json");

    BrClipQueryDto[] expected =
        new BrClipQueryDto[] {
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:6019326ea636b2001a16d491")),
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:601932287b541b001316ce4b")),
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:5e3aa3378583a30013890d58"))
        };

    final Set<BrClipQueryDto> actual = executeTask(request);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void multiplePages() {
    final String request = "/br/single";
    setupSuccessfulJsonPostResponse(
        request, "/br/br_broadcast_multiple_pages_1.json", "$programmeFilter)", null);
    setupSuccessfulJsonPostResponse(
        request, "/br/br_broadcast_multiple_pages_2.json", "$programmeFilter, after", null);

    BrClipQueryDto[] expected =
        new BrClipQueryDto[] {
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:6053887946ee90001a872665")),
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:6021371101483100133f889d")),
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:6019326ea636b2001a16d491")),
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:601932287b541b001316ce4b")),
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:5e3aa3378583a30013890d58")),
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:5d3875f52035ed001a6f3bb5")),
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:5d3831932bd8f200136c9242")),
          new BrClipQueryDto(
              BrConstants.GRAPHQL_API,
              new BrID(BrClipType.PROGRAMME, "av:5d24b2f44b36a5001a8e093e"))
        };

    final Set<BrClipQueryDto> actual = executeTask(request);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(8));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
