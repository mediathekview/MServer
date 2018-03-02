package de.mediathekview.mserver.crawler.zdf.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ZdfDownloadDtoDeserializerTest {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "/zdf/zdf_video_details1.json",
            "https://rodlzdf-a.akamaihd.net/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_436k_p9v12.mp4",
            "https://rodlzdf-a.akamaihd.net/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_1456k_p13v12.mp4",
            "https://rodlzdf-a.akamaihd.net/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_3328k_p36v12.mp4",
            Optional.empty(),
            Optional.of(GeoLocations.GEO_NONE)
        },
        {
            "/zdf/zdf_video_details2.json",
            "https://rodlzdf-a.akamaihd.net/none/zdf/18/03/180302_fr_lot/2/180302_fr_lot_476k_p9v13.mp4",
            "https://rodlzdf-a.akamaihd.net/none/zdf/18/03/180302_fr_lot/2/180302_fr_lot_1496k_p13v13.mp4",
            "",
            Optional.of("https://utstreaming.zdf.de/mtt/zdf/18/03/180302_fr_lot/2/logo_020318.xml"),
            Optional.of(GeoLocations.GEO_NONE)
        },
        {
            "/zdf/zdf_video_details3.json",
            "https://rodlzdf-a.akamaihd.net/dach/tivi/16/03/160301_folge25_tanzaufdemseil_jon/5/160301_folge25_tanzaufdemseil_jon_436k_p9v12.mp4",
            "https://rodlzdf-a.akamaihd.net/dach/tivi/16/03/160301_folge25_tanzaufdemseil_jon/5/160301_folge25_tanzaufdemseil_jon_1456k_p13v12.mp4",
            "",
            Optional.empty(),
            Optional.of(GeoLocations.GEO_DE_AT_CH)
        }
    });
  }

  private final String jsonFile;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final Optional<String> expectedSubtitle;
  private final Optional<GeoLocations> expectedGeo;

  public ZdfDownloadDtoDeserializerTest(final String aJsonFile, final String aExpectedUrlSmall, final String aExpectedUrlNormal,
      final String aExpectedUrlHd, final Optional<String> aExpectedSubtitle, final Optional<GeoLocations> aExpectedGeo) {
    jsonFile = aJsonFile;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedSubtitle = aExpectedSubtitle;
    expectedGeo = aExpectedGeo;
  }

  @Test
  public void test() {

    final JsonObject json = JsonFileReader.readJson(jsonFile);

    ZdfDownloadDtoDeserializer target = new ZdfDownloadDtoDeserializer();

    final Optional<DownloadDto> actual = target.deserialize(json, DownloadDto.class, null);

    assertThat(actual.isPresent(), equalTo(true));
    DownloadDto dto = actual.get();

    AssertFilm.assertUrl(expectedUrlSmall, dto.getUrl(Resolution.SMALL));
    AssertFilm.assertUrl(expectedUrlNormal, dto.getUrl(Resolution.NORMAL));
    AssertFilm.assertUrl(expectedUrlHd, dto.getUrl(Resolution.HD));
    assertThat(dto.getSubTitleUrl(), equalTo(expectedSubtitle));
    assertThat(dto.getGeoLocation(), equalTo(expectedGeo));
  }
}
