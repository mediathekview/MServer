package de.mediathekview.mserver.crawler.zdf;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import de.mediathekview.mserver.crawler.zdf.tasks.ZdfIndexPageTask;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.util.Arrays;
import java.util.Collection;
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
@PowerMockIgnore("javax.net.ssl.*")
@PowerMockRunnerDelegate(Parameterized.class)
public class ZdfIndexPageTaskTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "/zdf/zdf_index_page_with_bearer.html",
            Optional.of("e62473eacae5ebe85c01c05e81add8e4840a34f5"),
            Optional.of("c437ed7687255699958e0d451ce803c98c560c80")
        },
        {
            "/zdf/zdf_index_page_without_bearer.html",
            Optional.empty(),
            Optional.empty()
        }
    });
  }

  private final String htmlFile;
  private final Optional<String> expectedBearerSearch;
  private final Optional<String> expectedBearerVideo;

  private final ZdfIndexPageTask target;

  public ZdfIndexPageTaskTest(final String aHtmlFile,
      final Optional<String> aExpectedBearerSearch,
      final Optional<String> aExpectedBearerVideo) {
    htmlFile = aHtmlFile;
    expectedBearerSearch = aExpectedBearerSearch;
    expectedBearerVideo = aExpectedBearerVideo;

    target = new ZdfIndexPageTask();
  }

  @Test
  public void test() throws Exception {
    JsoupMock.mock(ZdfConstants.URL_BASE, htmlFile);

    ZdfConfiguration actual = target.call();

    assertThat(actual, notNullValue());
    assertThat(actual.getSearchAuthKey(), equalTo(expectedBearerSearch));
    assertThat(actual.getVideoAuthKey(), equalTo(expectedBearerVideo));
  }
}
