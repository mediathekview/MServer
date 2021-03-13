package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.zdf.ZdfConfiguration;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class ZdfIndexPageTaskTest {

  private final String htmlFile;
  private final Optional<String> expectedBearerSearch;
  private final Optional<String> expectedBearerVideo;
  private final String htmlFileSubpage;
  private final String urlSubpage;

  @Mock JsoupConnection jsoupConnection;

  @Mock ZdfCrawler crawler;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

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
    when(crawler.getCrawlerConfig())
        .thenReturn(MServerConfigManager.getInstance("MServer-JUnit-Config.yaml").getSenderConfig(Sender.ZDF));

    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(ZdfConstants.URL_BASE, htmlFile);
    if (!urlSubpage.isEmpty()) {
      urlMapping.put(ZdfConstants.URL_BASE + urlSubpage, htmlFileSubpage);
    }
    urlMapping.forEach(
        (url, fileName) -> {
          try {
            final Document document = JsoupMock.getFileDocument(url, fileName);
            when(jsoupConnection.requestBodyAsHtmlDocument(eq(url))).thenReturn(document);
            when(crawler.requestBodyAsHtmlDocument(eq(url))).thenReturn(document);
          } catch (final IOException iox) {
            fail();
          }
        });
    when(crawler.getConnection()).thenReturn(jsoupConnection);

    final ZdfIndexPageTask target = new ZdfIndexPageTask(crawler, ZdfConstants.URL_BASE);

    final ZdfConfiguration actual = target.call();

    assertThat(actual, notNullValue());
    assertThat(actual.getSearchAuthKey(), equalTo(expectedBearerSearch));
    assertThat(actual.getVideoAuthKey(), equalTo(expectedBearerVideo));
  }
}
