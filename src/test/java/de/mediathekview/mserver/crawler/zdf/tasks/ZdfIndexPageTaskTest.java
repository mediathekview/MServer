package de.mediathekview.mserver.crawler.zdf.tasks;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import de.mediathekview.mserver.crawler.zdf.ZdfConfiguration;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore(value= {"javax.net.ssl.*", "javax.*", "com.sun.*", "org.apache.logging.log4j.core.config.xml.*"})
@PowerMockRunnerDelegate(Parameterized.class)
public class ZdfIndexPageTaskTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "/zdf/zdf_index_page_with_bearer.html",
            "/zdf/zdf_subpage_with_token.html",
            "/serien/parfum/ambra-parfum-100.html",
            Optional.of("c4aa601db94912547f29ba036fbc96165cb18ee7"),
            Optional.of("d984c7d728b6a3912b41b70e715c7ba26cbf4872")
        }
    });
  }

  private final String htmlFile;
  private String htmlFileSubpage;
  private String urlSubpage;
  private final Optional<String> expectedBearerSearch;
  private final Optional<String> expectedBearerVideo;

  private final ZdfIndexPageTask target;

  public ZdfIndexPageTaskTest(final String aHtmlFile,
      final String aHtmlFileSubpage,
      final String aUrlSubpage,
      final Optional<String> aExpectedBearerSearch,
      final Optional<String> aExpectedBearerVideo) {
    htmlFile = aHtmlFile;
    htmlFileSubpage = aHtmlFileSubpage;
    urlSubpage = aUrlSubpage;
    expectedBearerSearch = aExpectedBearerSearch;
    expectedBearerVideo = aExpectedBearerVideo;

    target = new ZdfIndexPageTask();
  }

  @Test
  public void test() throws Exception {
    Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(ZdfConstants.URL_BASE, htmlFile);
    if (!urlSubpage.isEmpty()) {
      urlMapping.put(ZdfConstants.URL_BASE + urlSubpage, htmlFileSubpage);
    }
    JsoupMock.mock(urlMapping);

    ZdfConfiguration actual = target.call();

    assertThat(actual, notNullValue());
    assertThat(actual.getSearchAuthKey(), equalTo(expectedBearerSearch));
    assertThat(actual.getVideoAuthKey(), equalTo(expectedBearerVideo));
  }
}
