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
    final String url = getWireMockBaseUrlSafe() + "/video_2328k_p35v11.mp4";

    assertGetOptimizedUrlNormal(url, url);
  }

  @Test
  public void getOptimizedUrlNormalTestBetterUrlExists() {
    final String url = getWireMockBaseUrlSafe() + "/video_1456k_p13v11.mp4";
    final String expectedUrl = getWireMockBaseUrlSafe() + "/video_2328k_p35v11.mp4";

    setupHeadResponse("/video_2328k_p35v11.mp4", 200);
    assertGetOptimizedUrlNormal(expectedUrl, url);
  }

  @Test
  public void getOptimizedUrlNormalTestBetterUrlExists2() {
    final String url = getWireMockBaseUrlSafe() + "/video_1628k_p13v15.mp4";
    final String expectedUrl = getWireMockBaseUrlSafe() + "/video_2360k_p35v15.mp4";

    setupHeadResponse("/video_2360k_p35v15.mp4", 200);
    assertGetOptimizedUrlNormal(expectedUrl, url);
  }

  @Test
  public void getOptimizedUrlNormalTestBetterUrlNotExists() {
    final String url = getWireMockBaseUrlSafe() + "/video_2256k_p14v12.mp4";

    setupHeadResponse("/video_2328k_p35v12.mp4", 404);
    assertGetOptimizedUrlNormal(url, url);
  }

  @Test
  public void getOptimizedUrlHdTestAlreadyBestQuality() {
    final String url = getWireMockBaseUrlSafe() + "/video_6660k_p37v17.mp4";

    assertGetOptimizedUrlHd(url, url);
  }

  @Test
  public void getOptimizedUrlHdTestBetterUrlExists() {
    final String url = getWireMockBaseUrlSafe() + "/video_6628k_p61v17.mp4";
    final String expectedUrl = getWireMockBaseUrlSafe() + "/video_6660k_p37v17.mp4";

    setupHeadResponse("/video_6660k_p37v17.mp4", 200);
    assertGetOptimizedUrlHd(expectedUrl, url);
  }

  @Test
  public void getOptimizedUrlHdTestBetterUrlNotExists() {
    final String url = getWireMockBaseUrlSafe() + "/video_3360k_p36v17.mp4";

    setupHeadResponse("/video_6660k_p37v17.mp4", 404);
    assertGetOptimizedUrlHd(url, url);
  }

  @Test
  public void determineUrlHdTestFirstUrlExists() {
    final String url = getWireMockBaseUrlSafe() + "/video_1628k_p13v17.mp4";
    final String expectedUrl = getWireMockBaseUrlSafe() + "/video_6660k_p37v17.mp4";

    setupHeadResponse("/video_6660k_p37v17.mp4", 200);
    assertDetermineUrlHd(Optional.of(expectedUrl), url);
  }

  @Test
  public void determineUrlHdTestFirstUrlExists2() {
    final String url = getWireMockBaseUrlSafe() + "/video_2360k_p35v17.mp4";
    final String expectedUrl = getWireMockBaseUrlSafe() + "/video_6660k_p37v17.mp4";

    setupHeadResponse("/video_6660k_p37v17.mp4", 200);
    assertDetermineUrlHd(Optional.of(expectedUrl), url);
  }

  @Test
  public void determineUrlHdTestFirstUrlExists3() {
    final String url = getWireMockBaseUrlSafe() + "/video_3328k_p15v17.mp4";
    final String expectedUrl = getWireMockBaseUrlSafe() + "/video_6660k_p37v17.mp4";

    setupHeadResponse("/video_6660k_p37v17.mp4", 200);
    assertDetermineUrlHd(Optional.of(expectedUrl), url);
  }

  @Test
  public void determineUrlHdTestSecondUrlExists() {
    final String url = getWireMockBaseUrlSafe() + "/video_1628k_p13v17.mp4";
    final String expectedUrl = getWireMockBaseUrlSafe() + "/video_6628k_p61v17.mp4";

    setupHeadResponse("/video_6660k_p37v17.mp4", 404);
    setupHeadResponse("/video_6628k_p61v17.mp4", 200);
    assertDetermineUrlHd(Optional.of(expectedUrl), url);
  }

  @Test
  public void determineUrlHdTestNoUrlExists() {
    final String url = getWireMockBaseUrlSafe() + "/video_1628k_p13v17.mp4";

    setupHeadResponse("/video_6660k_p37v17.mp4", 404);
    setupHeadResponse("/video_6628k_p61v17.mp4", 404);
    assertDetermineUrlHd(Optional.empty(), url);
  }

  @Test
  public void determineUrlHdTestBothUrlExists() {
    final String url = getWireMockBaseUrlSafe() + "/video_1628k_p13v17.mp4";
    final String expectedUrl = getWireMockBaseUrlSafe() + "/video_6660k_p37v17.mp4";

    setupHeadResponse("/video_6660k_p37v17.mp4", 200);
    setupHeadResponse("/video_6628k_p61v17.mp4", 200);
    assertDetermineUrlHd(Optional.of(expectedUrl), url);
  }

  @Test
  public void determineUrlHdTestNMappingExists() {
    final String url = getWireMockBaseUrlSafe() + "/video_1422k_p13v12.mp4";

    assertDetermineUrlHd(Optional.empty(), url);
  }

  private void assertGetOptimizedUrlNormal(final String aExpectedUrl, final String aUrlToCheck) {
    final String actual = target.getOptimizedUrlNormal(aUrlToCheck);
    assertThat(actual, equalTo(aExpectedUrl));
  }

  private void assertGetOptimizedUrlHd(final String aExpectedUrl, final String aUrlToCheck) {
    final String actual = target.getOptimizedUrlHd(aUrlToCheck);
    assertThat(actual, equalTo(aExpectedUrl));
  }

  private void assertDetermineUrlHd(final Optional<String> aExpectedUrl, final String aUrlToCheck) {
    final Optional<String> actual = target.determineUrlHd(aUrlToCheck);
    assertThat(actual, equalTo(aExpectedUrl));
  }

  protected ZdfCrawlerOld createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ZdfCrawlerOld(
        forkJoinPool,
        nachrichten,
        fortschritte,
        new MServerConfigManager("MServer-JUnit-Config.yaml"));
  }
}
