package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.arte.tasks.ArteVideoDetailDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

@RunWith(Parameterized.class)
public class ArteVideoDetailsDeserializerTest {

  private final String jsonFile;
    private final String expectedUrlSmall;
    private final String expectedUrlNormal;
    private final String expectedUrlHd;

    public ArteVideoDetailsDeserializerTest(
            final String jsonFile,
            final String expectedUrlSmall,
            final String expectedUrlNormal,
            final String expectedUrlHd) {

        this.jsonFile = jsonFile;
        this.expectedUrlSmall = expectedUrlSmall;
        this.expectedUrlNormal = expectedUrlNormal;
        this.expectedUrlHd = expectedUrlHd;
    }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
            new Object[][]{
                    {
                            "/arte/arte_film_video1.json",
                            "https://arteptweb-a.akamaihd.net/am/ptweb/084000/084700/084733-002-A_HQ_0_VA-STA_04065289_MP4-800_AMM-PTWEB_1588D7RrM3.mp4",
                            "https://arteptweb-a.akamaihd.net/am/ptweb/084000/084700/084733-002-A_EQ_0_VA-STA_04065295_MP4-1500_AMM-PTWEB_1586Q7RqlB.mp4",
                            "https://arteptweb-a.akamaihd.net/am/ptweb/084000/084700/084733-002-A_SQ_0_VA-STA_04065288_MP4-2200_AMM-PTWEB_1588w7RraU.mp4"
                    }
        });
  }

  @Test
  public void testDeserialize() {
      final JsonElement jsonObject = JsonFileReader.readJson(jsonFile);

      final ArteVideoDetailsDeserializer target = new ArteVideoDetailsDeserializer();
      final Optional<ArteVideoDetailDTO> actual = target.deserialize(jsonObject, null, null);

    assertThat(actual.isPresent(), equalTo(true));
      final ArteVideoDetailDTO actualDetail = actual.get();

    assertFilmUrl(actualDetail.get(Resolution.SMALL), expectedUrlSmall);
    assertFilmUrl(actualDetail.get(Resolution.NORMAL), expectedUrlNormal);
    assertFilmUrl(actualDetail.get(Resolution.HD), expectedUrlHd);
  }

    private void assertFilmUrl(final String actualUrl, final String expectedUrl) {
    if (expectedUrl.isEmpty()) {
      assertThat(actualUrl, nullValue());
    } else {
      assertThat(actualUrl, equalTo(expectedUrl));
    }
  }
}
