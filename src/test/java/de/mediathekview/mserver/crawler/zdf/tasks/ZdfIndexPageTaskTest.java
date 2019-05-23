package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.zdf.ZdfConfiguration;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.*;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore(
    value = {
      "javax.net.ssl.*",
      "javax.*",
      "com.sun.*",
      "org.apache.logging.log4j.core.config.xml.*"
    })
@PowerMockRunnerDelegate(Parameterized.class)
public class ZdfIndexPageTaskTest {

  private final String htmlFile;
  private final Optional<String> expectedBearerSearch;
  private final Optional<String> expectedBearerVideo;
  private final ZdfIndexPageTask target;
  private final String htmlFileSubpage;
  private final String urlSubpage;

  public ZdfIndexPageTaskTest(
      final String aHtmlFile,
      final String aHtmlFileSubpage,
      final String aUrlSubpage,
      final Optional<String> aExpectedBearerSearch,
      final Optional<String> aExpectedBearerVideo) {
    htmlFile = aHtmlFile;
    htmlFileSubpage = aHtmlFileSubpage;
    urlSubpage = aUrlSubpage;
    expectedBearerSearch = aExpectedBearerSearch;
    expectedBearerVideo = aExpectedBearerVideo;

    final ZdfCrawler crawler = Mockito.mock(ZdfCrawler.class);
    target = new ZdfIndexPageTask(crawler, ZdfConstants.URL_BASE);
    Mockito.when(crawler.getCrawlerConfig())
        .thenReturn(MServerConfigManager.getInstance().getSenderConfig(Sender.ZDF));
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/zdf/zdf_index_page_with_bearer.html",
            "/zdf/zdf_subpage_with_token.html",
            "/serien/parfum/ambra-parfum-100.html",
            Optional.of("c4aa601db94912547f29ba036fbc96165cb18ee7"),
            Optional.of("d984c7d728b6a3912b41b70e715c7ba26cbf4872")
          }
        });
  }

  @Test
  public void test() throws Exception {
    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(ZdfConstants.URL_BASE, htmlFile);
    if (!urlSubpage.isEmpty()) {
      urlMapping.put(ZdfConstants.URL_BASE + urlSubpage, htmlFileSubpage);
    }
    JsoupMock.mock(urlMapping);

    final ZdfConfiguration actual = target.call();

    assertThat(actual, notNullValue());
    assertThat(actual.getSearchAuthKey(), equalTo(expectedBearerSearch));
    assertThat(actual.getVideoAuthKey(), equalTo(expectedBearerVideo));
  }
}
