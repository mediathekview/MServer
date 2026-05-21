package de.mediathekview.mserver.crawler.zdf.json;

import com.google.gson.JsonObject;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ZdfDownloadDtoDeserializerTest {

  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/zdf/zdf_video_details1.json",
            "http://localhost:8589/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_436k_p9v12.mp4",
            "http://localhost:8589/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_3328k_p36v12.mp4",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            Optional.empty(),
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
            "",
            "",
            "",
            Optional.of("https://utstreaming.zdf.de/mtt/zdf/18/03/180302_fr_lot/2/logo_020318.xml"),
            Optional.empty(),
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
            "",
            "",
            "",
            Optional.empty(),
            Optional.empty(),
            Optional.of(GeoLocations.GEO_DE_AT_CH)
          },
          {
            "/zdf/zdf_video_details_english.json",
            "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a1a2_476k_p9v13.mp4",
            "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a1a2_1496k_p13v13.mp4",
            "",
            "",
            "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a3a4_476k_p9v13.mp4",
            "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a3a4_1496k_p13v13.mp4",
            "",
            "",
            "",
            "",
            "",
            "",
            Optional.of(
                "https://utstreaming.zdf.de/mtt/zdf/18/04/180416_2215_sendung_hsn/7/Hard_Sun_Teil1_OmU.xml"),
            Optional.empty(),
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
            "",
            "",
            "",
            Optional.empty(),
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
            "",
            "",
            "http://localhost:8589/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a3a4_776k_p11v14.mp4",
            "http://localhost:8589/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a3a4_1496k_p13v14.mp4",
            "",
            "",
            Optional.of(
                "https://utstreaming.zdf.de/mtt/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/F1021200_hoh_deu_Schatz_nimm_du_sie_150719.xml"),
            Optional.empty(),
            Optional.of(GeoLocations.GEO_DE)
          },
          {
            "/zdf/zdf_video_details_uhd.json",
            "http://localhost:8589/none/zdf/23/01/230101_2015_sendung_trs/3/230101_2015_sendung_trs_a1a2_808k_p11v15.mp4",
//            "http://localhost:8589/none/zdf/23/01/230101_2015_sendung_trs/3/230101_2015_sendung_trs_a1a2_1628k_p13v15.mp4",
            "http://localhost:8589/none/zdf/23/01/230101_2015_sendung_trs/3/230101_2015_sendung_trs_a1a2_3328k_p15v15.mp4",
  "",          "http://localhost:8589/none/zdf/23/01/230101_2015_sendung_trs/3/230101_2015_sendung_trs_a1a2_4692k_p72v16.mp4",
            "",
            "",
            "",
            "",
            "http://localhost:8589/none/zdf/23/01/230101_2015_sendung_trs/3/230101_2015_sendung_trs_a3a4_808k_p11v15.mp4",
            "http://localhost:8589/none/zdf/23/01/230101_2015_sendung_trs/3/230101_2015_sendung_trs_a3a4_3328k_p15v15.mp4",
            "",
            "http://localhost:8589/none/zdf/23/01/230101_2015_sendung_trs/3/230101_2015_sendung_trs_a3a4_4692k_p72v16.mp4",
            Optional.of(
                "https://utstreaming.zdf.de/mtt/zdf/23/01/230101_2015_sendung_trs/6/F1037067_hoh_deu_Das_Traumschiff_Bahamas_Karibik_final_010123.xml"),
            Optional.empty(),
            Optional.of(GeoLocations.GEO_NONE)
          },
          {
            "/zdf/zdf_video_details_fhd.json",
            "http://localhost:8589/dach/zdf/20/12/201222_schwarm_meerestroemung_tex/3/201222_schwarm_meerestroemung_tex_808k_p11v17.mp4",
            "http://localhost:8589/dach/zdf/20/12/201222_schwarm_meerestroemung_tex/3/201222_schwarm_meerestroemung_tex_3328k_p15v17.mp4",
            "http://localhost:8589/dach/zdf/20/12/201222_schwarm_meerestroemung_tex/3/201222_schwarm_meerestroemung_tex_6628k_p61v17.mp4",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            Optional.empty(),
            Optional.empty(),
            Optional.of(GeoLocations.GEO_DE_AT_CH)
          },
          {
            "/zdf/zdf_video_details_eng_ut.json",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a1a2_808k_p11v17.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a1a2_3328k_p15v17.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a1a2_6628k_p61v17.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a1a2_4692k_p72v16.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a3a4_808k_p11v17.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a3a4_3328k_p15v17.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a3a4_6628k_p61v17.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a3a4_4692k_p72v16.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a5a6_808k_p11v17.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a5a6_3328k_p15v17.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a5a6_6628k_p61v17.mp4",
            "http://localhost:8589/de/zdf/23/02/230222_1001_sendung_swm/7/230222_1001_sendung_swm_a5a6_4692k_p72v16.mp4",
            Optional.of(
                "https://utstreaming.zdf.de/mtt/zdf/23/02/230222_1001_sendung_swm/11/F1038617_hoh_deu_Der_Schwarm_101_Mediathek_220223.xml"),
            Optional.of(
                "https://utstreaming.zdf.de/mtt/zdf/23/02/230222_1001_sendung_swm/11/The_Swarm_Folge1_Mediathek_EN_UT_neu_.xml"),
            Optional.of(GeoLocations.GEO_DE)
          }
        });
  }

  @MethodSource("data")
  @ParameterizedTest
  void test(final String jsonFile, final String expectedUrlSmall, final String expectedUrlNormal, final String expectedUrlHd, final String expectedUrlUhd, final String expectedUrlSmallEnglish, final String expectedUrlNormalEnglish, final String expectedUrlHdEnglish, final String expectedUrlUhdEnglish, final String expectedUrlSmallAd, final String expectedUrlNormalAd, final String expectedUrlHdAd, final String expectedUrlUhdAd, final Optional<String> expectedSubtitle, final Optional<String> expectedSubtitleEnglish, final Optional<GeoLocations> expectedGeo) {

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
    AssertFilm.assertUrl(expectedUrlUhd, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN, Resolution.UHD));
    AssertFilm.assertUrl(
        expectedUrlSmallEnglish, dto.getUrl(ZdfConstants.LANGUAGE_ENGLISH, Resolution.SMALL));
    AssertFilm.assertUrl(
        expectedUrlNormalEnglish, dto.getUrl(ZdfConstants.LANGUAGE_ENGLISH, Resolution.NORMAL));
    AssertFilm.assertUrl(
        expectedUrlHdEnglish, dto.getUrl(ZdfConstants.LANGUAGE_ENGLISH, Resolution.HD));
    AssertFilm.assertUrl(
        expectedUrlUhdEnglish, dto.getUrl(ZdfConstants.LANGUAGE_ENGLISH, Resolution.UHD));
    AssertFilm.assertUrl(
        expectedUrlSmallAd, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN_AD, Resolution.SMALL));
    AssertFilm.assertUrl(
        expectedUrlNormalAd, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN_AD, Resolution.NORMAL));
    AssertFilm.assertUrl(
        expectedUrlHdAd, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN_AD, Resolution.HD));
    AssertFilm.assertUrl(
        expectedUrlUhdAd, dto.getUrl(ZdfConstants.LANGUAGE_GERMAN_AD, Resolution.UHD));
    assertThat(dto.getSubTitleUrl(ZdfConstants.LANGUAGE_GERMAN), equalTo(expectedSubtitle));
    assertThat(dto.getSubTitleUrl(ZdfConstants.LANGUAGE_ENGLISH), equalTo(expectedSubtitleEnglish));
    assertThat(dto.getGeoLocation(), equalTo(expectedGeo));
  }
}
