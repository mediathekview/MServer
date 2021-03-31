package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ZdfVideoUrlOptimizerTest extends WireMockTestBase {

  private ZdfVideoUrlOptimizer target;

  @Before
  public void setup() {
    target = new ZdfVideoUrlOptimizer(createCrawler());
  }

  @Test
  public void getOptimizedUrlNormalTestAlreadyBestQuality() {
    final String url = wireMockServer.baseUrl() + "/video_2328k_p35v11.mp4";

    assertGetOptimizedUrlNormal(url, url);
  }

  @Test
  public void getOptimizedUrlNormalTestBetterUrlExists() {
    final String url = wireMockServer.baseUrl() + "/video_1456k_p13v11.mp4";
    final String expectedUrl = wireMockServer.baseUrl() + "/video_2328k_p35v11.mp4";

    setupHeadResponse("/video_2328k_p35v11.mp4", 200);
    assertGetOptimizedUrlNormal(expectedUrl, url);
  }

  @Test
  public void getOptimizedUrlNormalTestBetterUrlExists2() {
    final String url = wireMockServer.baseUrl() + "/video_1628k_p13v15.mp4";
    final String expectedUrl = wireMockServer.baseUrl() + "/video_2360k_p35v15.mp4";

    setupHeadResponse("/video_2360k_p35v15.mp4", 200);
    assertGetOptimizedUrlNormal(expectedUrl, url);
  }

  @Test
  public void getOptimizedUrlNormalTestBetterUrlNotExists() {
    final String url = wireMockServer.baseUrl() + "/video_2256k_p14v12.mp4";

    setupHeadResponse("/video_2328k_p35v12.mp4", 404);
    assertGetOptimizedUrlNormal(url, url);
  }

  @Test
  public void determineUrlHdTestFirstUrlExists() {
    final String url = wireMockServer.baseUrl() + "/video_1456k_p13v12.mp4";
    final String expectedUrl = wireMockServer.baseUrl() + "/video_3328k_p36v12.mp4";

    setupHeadResponse("/video_3328k_p36v12.mp4", 200);
    assertDetermineUrlHd(Optional.of(expectedUrl), url);
  }

  @Test
  public void determineUrlHdTestFirstUrlExists2() {
    final String url = wireMockServer.baseUrl() + "/video_2360k_p35v15.mp4";
    final String expectedUrl = wireMockServer.baseUrl() + "/video_3360k_p36v15.mp4";

    setupHeadResponse("/video_3360k_p36v15.mp4", 200);
    assertDetermineUrlHd(Optional.of(expectedUrl), url);
  }

  @Test
  public void determineUrlHdTestFirstUrlExists3() {
    final String url = wireMockServer.baseUrl() + "/video_1628k_p13v15.mp4";
    final String expectedUrl = wireMockServer.baseUrl() + "/video_3360k_p36v15.mp4";

    setupHeadResponse("/video_3360k_p36v15.mp4", 200);
    assertDetermineUrlHd(Optional.of(expectedUrl), url);
  }

  @Test
  public void determineUrlHdTestSecondUrlExists() {
    final String url = wireMockServer.baseUrl() + "/video_1456k_p13v12.mp4";
    final String expectedUrl = wireMockServer.baseUrl() + "/video_3256k_p15v12.mp4";

    setupHeadResponse("/video_3328k_p36v12.mp4", 404);
    setupHeadResponse("/video_3256k_p15v12.mp4", 200);
    assertDetermineUrlHd(Optional.of(expectedUrl), url);
  }

  @Test
  public void determineUrlHdTestNoUrlExists() {
    final String url = wireMockServer.baseUrl() + "/video_1456k_p13v12.mp4";

    setupHeadResponse("/video_3328k_p36v12.mp4", 404);
    setupHeadResponse("/video_3256k_p15v12.mp4", 404);
    assertDetermineUrlHd(Optional.empty(), url);
  }

  @Test
  public void determineUrlHdTestBothUrlExists() {
    final String url = wireMockServer.baseUrl() + "/video_1496k_p13v13.mp4";
    final String expectedUrl = wireMockServer.baseUrl() + "/video_3328k_p36v13.mp4";

    setupHeadResponse("/video_3328k_p36v13.mp4", 200);
    setupHeadResponse("/video_3296k_p15v13.mp4", 200);
    assertDetermineUrlHd(Optional.of(expectedUrl), url);
  }

  @Test
  public void determineUrlHdTestNMappingExists() {
    final String url = wireMockServer.baseUrl() + "/video_1422k_p13v12.mp4";

    assertDetermineUrlHd(Optional.empty(), url);
  }

  private void assertGetOptimizedUrlNormal(final String aExpectedUrl, final String aUrlToCheck) {
    final String actual = target.getOptimizedUrlNormal(aUrlToCheck);
    assertThat(actual, equalTo(aExpectedUrl));
  }

  private void assertDetermineUrlHd(final Optional<String> aExpectedUrl, final String aUrlToCheck) {
    final Optional<String> actual = target.determineUrlHd(aUrlToCheck);
    assertThat(actual, equalTo(aExpectedUrl));
  }

  protected ZdfCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ZdfCrawler(forkJoinPool, nachrichten, fortschritte, MServerConfigManager.getInstance("MServer-JUnit-Config.yaml"));
  }
}
