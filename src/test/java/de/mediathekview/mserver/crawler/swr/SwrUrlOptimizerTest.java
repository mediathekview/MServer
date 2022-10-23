package de.mediathekview.mserver.crawler.swr;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.crawler.ard.ArdUrlOptimizer;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBaseJUnit5;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;

class SwrUrlOptimizerTest extends WireMockTestBaseJUnit5 {

  @Test
  void optimizeHdUrlTestFullHdExists() {
    final String url = getWireMockBaseUrlSafe() + "/845421.xl.mp4";
    final String expectedUrl = getWireMockBaseUrlSafe() + "/845421.xxl.mp4";
    setupHeadResponse("/845421.xxl.mp4", 200);

    final ArdUrlOptimizer target = new ArdUrlOptimizer(createCrawler());
    final String actualUrl = target.optimizeHdUrl(url);

    assertThat(actualUrl).isEqualTo(expectedUrl);
  }

  @Test
  void optimizeHdUrlTestFullHdDoesNotExists() {
    final String url = getWireMockBaseUrlSafe() + "/845421.xl.mp4";
    setupHeadResponse("/845421.xxl.mp4", 404);

    final ArdUrlOptimizer target = new ArdUrlOptimizer(createCrawler());
    final String actualUrl = target.optimizeHdUrl(url);

    assertThat(actualUrl).isEqualTo(url);
  }

  @Test
  void optimizeHdUrlTestNoUrlToOptimize() {
    final String url = getWireMockBaseUrlSafe() + "/78946584.l.mp4";

    final ArdUrlOptimizer target = new ArdUrlOptimizer(createCrawler());
    final String actualUrl = target.optimizeHdUrl(url);

    assertThat(actualUrl).isEqualTo(url);
  }

  protected ArdCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ArdCrawler(
        forkJoinPool,
        nachrichten,
        fortschritte,
        new MServerConfigManager("MServer-JUnit-Config.yaml"));
  }
}
