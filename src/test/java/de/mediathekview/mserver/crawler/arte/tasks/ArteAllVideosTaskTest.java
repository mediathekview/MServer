package de.mediathekview.mserver.crawler.arte.tasks;

import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.junit.Test;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class ArteAllVideosTaskTest extends ArteTaskTestBase {
  @Test
  public void testLastChanceVideos() {
    final String requestUrl =
        "/api/rproxy/emac/v3/de/web/data/VIDEO_LISTING/?imageFormats=landscape&authorizedAreas=DE_FR,EUR_DE_FR,SAT,ALL&videoType=LAST_CHANCE&imageWithText=true&page=1&limit=100";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_last_chance1.json");
    final Set<ArteFilmUrlDto> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(10));
  }

  @Test
  public void testLastChanceVideosMultiplePages() {
    final String requestUrl1 =
            "/api/rproxy/emac/v3/de/web/data/VIDEO_LISTING/?imageFormats=landscape&authorizedAreas=DE_FR,EUR_DE_FR,SAT,ALL&videoType=LAST_CHANCE&imageWithText=true&page=1&limit=100";
    final String requestUrl2 =
            "/api/rproxy/emac/v3/de/web/data/VIDEO_LISTING/?imageFormats=landscape&authorizedAreas=DE_FR,EUR_DE_FR,SAT,ALL&videoType=LAST_CHANCE&imageWithText=true&page=2&limit=100";
    final String requestUrl3 =
            "/api/rproxy/emac/v3/de/web/data/VIDEO_LISTING/?imageFormats=landscape&authorizedAreas=DE_FR,EUR_DE_FR,SAT,ALL&videoType=LAST_CHANCE&imageWithText=true&page=3&limit=100";
    setupSuccessfulJsonResponse(requestUrl1, "/arte/arte_last_chance1.json");
    setupSuccessfulJsonResponse(requestUrl2, "/arte/arte_last_chance2.json");
    setupSuccessfulJsonResponse(requestUrl3, "/arte/arte_last_chance3.json");
    final Set<ArteFilmUrlDto> actual = executeTask(requestUrl1, requestUrl2);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(20));
  }

  private Set<ArteFilmUrlDto> executeTask(String... requestUrl) {
    final Queue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new CrawlerUrlDTO(getWireMockBaseUrlSafe() + url));
    }
    return new ArteAllVideosTask(createCrawler(), input, ArteLanguage.DE).invoke();
  }
}
