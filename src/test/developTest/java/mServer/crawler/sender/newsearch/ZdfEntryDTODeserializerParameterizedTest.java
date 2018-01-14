package mServer.crawler.sender.newsearch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.google.gson.JsonObject;
import mServer.test.JsonFileReader;

@RunWith(Parameterized.class)
public class ZdfEntryDTODeserializerParameterizedTest {

  private final String jsonFile;

  private final String generalUrl;
  private final String downloadUrl;

  public ZdfEntryDTODeserializerParameterizedTest(final String jsonFile, final String generalUrl,
      final String downloadUrl) {
    this.jsonFile = jsonFile;
    this.generalUrl = generalUrl;
    this.downloadUrl = downloadUrl;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {{"/zdf/zdf_search_page_entry_sample1.json",
        "https://api.zdf.de/content/documents/zdf/serien/soko-leipzig/videos/trailer-chefsache-100.json",
        "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/170224_sendung_tr_sok4"},
        {"/zdf/zdf_search_page_entry_sample_fullurls.json",
            "https://api.zdf.de/content/documents/zdf/serien/soko-leipzig/videos/trailer-chefsache-100.json",
            "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/170224_sendung_tr_sok4"},});
  }

  @Test
  public void testDeserialize() {
    final JsonObject jsonObject = JsonFileReader.readJson(jsonFile);

    final ZDFEntryDTODeserializer target = new ZDFEntryDTODeserializer();
    final ZDFEntryDTO actual = target.deserialize(jsonObject, ZDFEntryDTO.class, null);

    assertThat(actual, notNullValue());
    assertThat(actual.getEntryGeneralInformationUrl(), equalTo(generalUrl));
    assertThat(actual.getEntryDownloadInformationUrl(), equalTo(downloadUrl));
  }
}
