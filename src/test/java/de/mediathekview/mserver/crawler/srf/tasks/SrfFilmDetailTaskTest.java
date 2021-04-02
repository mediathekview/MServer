package de.mediathekview.mserver.crawler.srf.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.testhelper.FileReader;
import org.junit.Test;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class SrfFilmDetailTaskTest extends SrfTaskTestBase {

  @Test
  public void testSingleUrl() {
    final String requestUrl =
        "/integrationlayer/2.0/srf/mediaComposition/video/22b9dd2c-d1fd-463b-91de-d804eda74889.json";

    final String jsonBody =
        FileReader.readFile("/srf/srf_film_page1.json", getWireMockBaseUrlSafe());
    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jsonBody)));

    final String m3u8Body =
        FileReader.readFile("/srf/srf_film_page1.m3u8", getWireMockBaseUrlSafe());
    wireMockServer.stubFor(
        get(urlEqualTo(
                "/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/master.m3u8?start=0.0&end=3305.1"))
            .willReturn(aResponse().withStatus(200).withBody(m3u8Body)));

    final Set<Film> actual =
        new SrfFilmDetailTask(createCrawler(), createCrawlerUrlDto(requestUrl)).invoke();

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));
  }

  @Test
  public void testFilmUrlNotFound() {
    final String requestUrl =
        "/integrationlayer/2.0/srf/mediaComposition/video/22b9dd2c-d1fd-463b-91de-d804eda74889.json";

    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(404).withBody("Not Found")));

    final Set<Film> actual =
        new SrfFilmDetailTask(createCrawler(), createCrawlerUrlDto(requestUrl)).invoke();

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }
}
