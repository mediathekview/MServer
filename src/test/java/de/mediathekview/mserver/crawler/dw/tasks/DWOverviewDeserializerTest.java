package de.mediathekview.mserver.crawler.dw.tasks;


import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.dw.parser.DWSendungOverviewDeserializer;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.JsonElement;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@RunWith(Parameterized.class)
public class DWOverviewDeserializerTest extends DwTaskTestBase {

  private final String responseAsFile;
  private final boolean hasNext;
  private final String hasNextPage;
  private final int noElements;

  public DWOverviewDeserializerTest(
      final String responseAsFile,
      final boolean hasNext,
      final String hasNextPage,
      final int noElements
      ) {
    this.responseAsFile = responseAsFile;
    this.hasNext = hasNext;
    this.hasNextPage = hasNextPage;
    this.noElements = noElements;

  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/dw/dw_overview_end.json",
            false,
            "",
            1
          },
          {
            "/dw/dw_overview_next.json",
            true,
            "https://api.dw.com/api/list/mediacenter/1?pageIndex=178",
            2
          }
        });
  }

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() throws IOException {
    final JsonElement jsonElement = JsonFileReader.readJson(responseAsFile);
    final DWSendungOverviewDeserializer target = new DWSendungOverviewDeserializer();
    final Optional<PagedElementListDTO<TopicUrlDTO>> actual = target.deserialize(jsonElement, null, null);
    //
    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get().getNextPage().isPresent(), equalTo(hasNext));
    assertThat(actual.get().getNextPage().orElse(""), equalTo(hasNextPage));
    assertThat(actual.get().getElements().size(), equalTo(noElements));
    //
    
  }

}
