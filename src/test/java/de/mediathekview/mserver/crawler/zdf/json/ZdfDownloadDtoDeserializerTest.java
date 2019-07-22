package de.mediathekview.mserver.crawler.zdf.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
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

  private final String jsonFile;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedUrlSmallEnglish;
  private final String expectedUrlNormalEnglish;
  private final String expectedUrlHdEnglish;
  private final String expectedUrlSmallAd;
  private final String expectedUrlNormalAd;
  private final String expectedUrlHdAd;
  private final Optional<String> expectedSubtitle;
  private final Optional<GeoLocations> expectedGeo;

  public ZdfDownloadDtoDeserializerTest(
      final String aJsonFile,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedUrlSmallEnglish,
      final String aExpectedUrlNormalEnglish,
      final String aExpectedUrlHdEnglish,
      final String aExpectedUrlSmallAd,
      final String aExpectedUrlNormalAd,
      final String aExpectedUrlHdAd,
      final Optional<String> aExpectedSubtitle,
      final Optional<GeoLocations> aExpectedGeo) {
    jsonFile = aJsonFile;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedUrlSmallEnglish = aExpectedUrlSmallEnglish;
    expectedUrlNormalEnglish = aExpectedUrlNormalEnglish;
    expectedUrlHdEnglish = aExpectedUrlHdEnglish;
    this.expectedUrlSmallAd = aExpectedUrlSmallAd;
    this.expectedUrlNormalAd = aExpectedUrlNormalAd;
    this.expectedUrlHdAd = aExpectedUrlHdAd;
    expectedSubtitle = aExpectedSubtitle;
    expectedGeo = aExpectedGeo;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/zdf/zdf_video_details1.json",
            "http://localhost:8589/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_436k_p9v12.mp4",
            "http://localhost:8589/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_1456k_p13v12.mp4",
            "http://localhost:8589/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_3328k_p36v12.mp4",
            "",
            "",
            "",
            "",
            "",
            "",
            Optional.empty(),
            Optional.of(GeoLocations.GEO_NONE)
          },
          {
            "/zdf/zdf_video_details2.json",
            "http://localhost:8589/none/zdf/18/03/180302_fr_lot/2/180302_fr_lot_476k_p9v13.mp4",
            "http://localhost:8589/none/zdf/18/03/180302_fr_lot/2/180302_fr_lot_1496k_p13v13.mp4",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            Optional.of("https://utstreaming.zdf.de/mtt/zdf/18/03/180302_fr_lot/2/logo_020318.xml"),
            Optional.of(GeoLocations.GEO_DE_AT_CH_EU)
          },
          {
            "/zdf/zdf_video_details3.json",
            "http://localhost:8589/dach/tivi/16/03/160301_folge25_tanzaufdemseil_jon/5/160301_folge25_tanzaufdemseil_jon_436k_p9v12.mp4",
            "http://localhost:8589/dach/tivi/16/03/160301_folge25_tanzaufdemseil_jon/5/160301_folge25_tanzaufdemseil_jon_1456k_p13v12.mp4",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            Optional.empty(),
            Optional.of(GeoLocations.GEO_DE_AT_CH)
          },
          {
            "/zdf/zdf_video_details_english.json",
            "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a1a2_476k_p9v13.mp4",
            "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a1a2_1496k_p13v13.mp4",
            "",
            "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a3a4_476k_p9v13.mp4",
            "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a3a4_1496k_p13v13.mp4",
            "",
            "",
            "",
            "",
            Optional.of(
                "https://utstreaming.zdf.de/mtt/zdf/18/04/180416_2215_sendung_hsn/7/Hard_Sun_Teil1_OmU.xml"),
            Optional.of(GeoLocations.GEO_DE)
          },
          {
            "/zdf/zdf_video_details_3sat.json",
            "https://rodlzdf-a.akamaihd.net/none/3sat/18/10/181027_lina_online/1/181027_lina_online_776k_p11v13.mp4",
            "https://rodlzdf-a.akamaihd.net/none/3sat/18/10/181027_lina_online/1/181027_lina_online_1496k_p13v13.mp4",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            Optional.empty(),
            Optional.of(GeoLocations.GEO_NONE)
          },
          {
            "/zdf/zdf_video_details_with_audiodescription.json",
            "http://localhost:8589/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a1a2_776k_p11v14.mp4",
            "http://localhost:8589/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a1a2_1496k_p13v14.mp4",
            "",
            "",
            "",
            "",
            "http://localhost:8589/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a3a4_776k_p11v14.mp4",
            "http://localhost:8589/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a3a4_1496k_p13v14.mp4",
            "",
            Optional.of(
                "https://utstreaming.zdf.de/mtt/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/F1021200_hoh_deu_Schatz_nimm_du_sie_150719.xml"),
            Optional.of(GeoLocations.GEO_DE)
          }
        });
  }

  @Test
  public void test() {

    final JsonObject json = JsonFileReader.readJson(jsonFile);

    final ZdfDownloadDtoDeserializer target = new ZdfDownloadDtoDeserializer();

    final Optional<DownloadDto> actual = target.deserialize(json, DownloadDto.class, null);

    assertThat(actual.isPresent(), equalTo(true));
    final DownloadDto dto = actual.get();

    AssertFilm.assertUrl(
        expectedUrlSmall, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN, Resolution.SMALL));
    AssertFilm.assertUrl(
        expectedUrlNormal, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN, Resolution.NORMAL));
    AssertFilm.assertUrl(expectedUrlHd, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN, Resolution.HD));
    AssertFilm.assertUrl(
        expectedUrlSmallEnglish, dto.getUrl(ZdfConstants.LANGUAGE_ENGLISH, Resolution.SMALL));
    AssertFilm.assertUrl(
        expectedUrlNormalEnglish, dto.getUrl(ZdfConstants.LANGUAGE_ENGLISH, Resolution.NORMAL));
    AssertFilm.assertUrl(
        expectedUrlHdEnglish, dto.getUrl(ZdfConstants.LANGUAGE_ENGLISH, Resolution.HD));
    AssertFilm.assertUrl(
        expectedUrlSmallAd, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN_AD, Resolution.SMALL));
    AssertFilm.assertUrl(
        expectedUrlNormalAd, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN_AD, Resolution.NORMAL));
    AssertFilm.assertUrl(
        expectedUrlHdAd, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN_AD, Resolution.HD));
    assertThat(dto.getSubTitleUrl(), equalTo(expectedSubtitle));
    assertThat(dto.getGeoLocation(), equalTo(expectedGeo));
  }
}
