package de.mediathekview.mserver.crawler.srf.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.testhelper.FileReader;
import java.util.Optional;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class SrfFilmDetailTaskTest extends SrfTaskTestBase {

  @Test
  public void testSingleUrl() {
    String requestUrl = "/integrationlayer/2.0/srf/mediaComposition/video/22b9dd2c-d1fd-463b-91de-d804eda74889.json";

    String jsonBody = FileReader.readFile("/srf/srf_film_page1.json");
    wireMockRule.stubFor(get(urlEqualTo(requestUrl))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jsonBody)));

    String m3u8Body = FileReader.readFile("/srf/srf_film_page1.m3u8");
    wireMockRule.stubFor(get(urlEqualTo("/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/master.m3u8?start=0.0&end=3305.1"))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(m3u8Body)));

    Set<Film> actual = new SrfFilmDetailTask(createCrawler(), createCrawlerUrlDto(requestUrl)).invoke();
    
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));
  }
  
  @Test
  public void testFilmUrlNotFound() {
    String requestUrl = "/integrationlayer/2.0/srf/mediaComposition/video/22b9dd2c-d1fd-463b-91de-d804eda74889.json";

    wireMockRule.stubFor(get(urlEqualTo(requestUrl))
            .willReturn(aResponse()
                    .withStatus(404)
                    .withBody("Not Found")));

    Set<Film> actual = new SrfFilmDetailTask(createCrawler(), createCrawlerUrlDto(requestUrl)).invoke();
    
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }
}
