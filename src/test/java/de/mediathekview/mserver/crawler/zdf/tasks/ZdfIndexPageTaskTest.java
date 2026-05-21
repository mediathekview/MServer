package de.mediathekview.mserver.crawler.zdf.tasks;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import de.mediathekview.mserver.crawler.zdf.ZdfConfiguration;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.*;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ZdfIndexPageTaskTest {

  @Mock ZdfCrawler crawler;

  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/zdf/zdf_index_page_with_bearer.html",
            "/zdf/zdf_subpage_with_token.html",
            "/serien/parfum/ambra-parfum-100.html",
            Optional.of("c4aa601db94912547f29ba036fbc96165cb18ee7"),
            Optional.of("d984c7d728b6a3912b41b70e715c7ba26cbf4872")
          },
          {
            "/zdf/zdf_index_page_with_bearer2.html",
            "",
            "",
            Optional.of("5bb200097db507149612d7d983131d06c79706d5"),
            Optional.of("20c238b5345eb428d01ae5c748c5076f033dfcc7")
          }
        });
  }

  @MethodSource("data")
  @ParameterizedTest
  void test(
      final String htmlFile,
      final String htmlFileSubpage,
      final String urlSubpage,
      final Optional<String> expectedBearerSearch,
      final Optional<String> expectedBearerVideo) {
    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(ZdfConstants.URL_BASE, htmlFile);
    if (!urlSubpage.isEmpty()) {
      urlMapping.put(ZdfConstants.URL_BASE + urlSubpage, htmlFileSubpage);
    }
    urlMapping.forEach(
        (url, fileName) -> {
          try {
            final Document document = JsoupMock.getFileDocument(fileName);
            when(crawler.requestBodyAsHtmlDocument(url)).thenReturn(document);
          } catch (final IOException iox) {
            fail();
          }
        });

    final ZdfIndexPageTask target = new ZdfIndexPageTask(crawler, ZdfConstants.URL_BASE);

    final ZdfConfiguration actual = target.call();

    assertThat(actual, notNullValue());
    assertThat(actual.getSearchAuthKey(), equalTo(expectedBearerSearch));
    assertThat(actual.getVideoAuthKey(), equalTo(expectedBearerVideo));
  }
}
