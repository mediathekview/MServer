package de.mediathekview.mserver.crawler.zdf.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.zdf.ZdfEntryDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Arrays;
import java.util.Collection;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ZdfDayPageDeserializerTest {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "/zdf/zdf_day_page_single.json",
            new ZdfEntryDto[]{
                new ZdfEntryDto("https://api.zdf.de/content/documents/olympia-im-technikwahn-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_technologien_neu_spo"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/gestrandet-102.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/tivi/150908_meerjungfrau_folge11_ham"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/im-dialog-vom-23022018-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180223_phx_dialog"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/augstein--blome-vom-23022018-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180223_phx_bib_augstein"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/menschen---das-magazin-vom-24-februar-2018-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_sendung_mdm"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/die-orakel-krake-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/tivi/150908_meerjungfrau_folge10_ham"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/siegerehrung-maenner-staffel-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_siegerehrung_staffel_spo"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/siegerehrung-vom-parallelslalom-der-frauen-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_sieger_snowboard_spo")
            }
        }
    });
  }

  private final ZdfDayPageDeserializer target;

  private final String jsonFile;
  private final ZdfEntryDto[] expectedEntries;

  public ZdfDayPageDeserializerTest(final String aJsonFile, final ZdfEntryDto[] aExpectedEntries) {
    target = new ZdfDayPageDeserializer();

    jsonFile = aJsonFile;
    expectedEntries = aExpectedEntries;
  }

  @Test
  public void deserializeTest() {
    final JsonObject json = JsonFileReader.readJson(jsonFile);

    Collection<ZdfEntryDto> actual = target.deserialize(json, ZdfEntryDto.class, null);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expectedEntries.length));
    assertThat(actual, Matchers.containsInAnyOrder(expectedEntries));
  }
}
