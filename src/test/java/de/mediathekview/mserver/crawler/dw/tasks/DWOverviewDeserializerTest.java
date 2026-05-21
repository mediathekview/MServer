package de.mediathekview.mserver.crawler.dw.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.dw.parser.DWSendungOverviewDeserializer;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DWOverviewDeserializerTest extends DwTaskTestBase {

  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"/dw/dw_overview_end.json", false, "", 1},
          {
            "/dw/dw_overview_next.json",
            true,
            "https://api.dw.com/api/list/mediacenter/1?pageIndex=178",
            2
          }
        });
  }

  @MethodSource("data")
  @ParameterizedTest
  void test(
      final String responseAsFile,
      final boolean hasNext,
      final String hasNextPage,
      final int noElements) {
    final JsonElement jsonElement = JsonFileReader.readJson(responseAsFile);
    final DWSendungOverviewDeserializer target = new DWSendungOverviewDeserializer();
    final Optional<PagedElementListDTO<TopicUrlDTO>> actual =
        target.deserialize(jsonElement, null, null);
    //
    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get().getNextPage().isPresent(), equalTo(hasNext));
    assertThat(actual.get().getNextPage().orElse(""), equalTo(hasNextPage));
    assertThat(actual.get().getElements().size(), equalTo(noElements));
    //

  }
}
