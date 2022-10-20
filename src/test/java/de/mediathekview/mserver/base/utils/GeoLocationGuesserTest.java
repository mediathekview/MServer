package de.mediathekview.mserver.base.utils;

import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import junit.framework.TestCase;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class GeoLocationGuesserTest extends TestCase {

  private final Sender sender;
  private final String url;
  private final GeoLocations expectedGeoLocation;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            Sender.ARD,
            "https://odgeomdr-a.akamaihd.net/mp4dyn2/c/FCMS-c20d56bc-be5f-4eb0-928a-0177777823ba-730aae549c28_c2.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.ARD,
            "https://cdn-storage.br.de/geo/b7/2021-05/30/e6772ebc-c0db-11eb-81f0-02420a00057c_C.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.ARD,
            "https://mediandr-a.akamaihd.net/progressive_geo/2018/0616/TV-20180616-1136-2500.hq.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.ARD,
            "http://media.ndr.de/progressive_geo/2016/0716/TV-20160716-1300-3600.hq.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.ARD,
            "https://pdodswr-a.akamaihd.net/swrfernsehen/geo/de/geldmachtliebe/1469376.l.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.ARD,
            "https://rbbmediapmdp-a.akamaihd.net/content-de/d2/7d/d27d4fc4-af2f-48bd-b821-993af741785f/d27d4fc4-af2f-48bd-b821-993af741785f_hd1080-1800k.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.ARD,
            "https://pdvideosdaserste-a.akamaihd.net/de/2021/05/13/16d7c4ca-507d-4560-bfdd-a1c1778f2942/960-1_900223.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.ARD,
            "http://wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk0/227/2271942/2271942_29483837.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.ARD,
            "https://hrardmediathek-a.akamaihd.net/video/as/geoblocking/2021_06/hrLogo_210605093605_0215219_960x540-50p-1800kbit.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.ARD,
            "https://pdvideosdaserste-a.akamaihd.net/dach/2021/02/05/bcc5d307-7b30-4386-b037-1c6dcd6ab428/960-1_838479.mp4",
            GeoLocations.GEO_DE_AT_CH
          },
          {
            Sender.ARD,
            "https://wdrmedien-a.akamaihd.net/medp/ondemand/deChAt/fsk0/244/2442535/2442535_35333716.mp4",
            GeoLocations.GEO_DE_AT_CH
          },
          {
            Sender.ARD,
            "http://cdn-storage.br.de/MUJIuUOVBwQIbtC2uKJDM6OhuLnC_2rH_K1S/_-JS/_AvH_AbG_71S/192b42c4-d7ba-4b46-81ef-3c03452b762a_C.mp4",
            GeoLocations.GEO_NONE
          },
          {
            Sender.ARD,
            "http://hrardmediathek-a.akamaihd.net/video/as/allewetter/2020_10/hrLogo_201030194106_L383725_960x540-50p-1800kbit.mp4",
            GeoLocations.GEO_NONE
          },
          {
            Sender.ARD,
            "http://media.ndr.de/progressive/2015/0416/TV-20150416-2256-2542.hq.mp4",
            GeoLocations.GEO_NONE
          },
          {
            Sender.ARD,
            "http://mediandr-a.akamaihd.net/progressive/2019/0222/TV-20190222-1418-1100.hq.mp4",
            GeoLocations.GEO_NONE
          },
          {
            Sender.ARD,
            "http://ondemand.mdr.de/mp4dyn/f/FCMS-f6eea84a-a8fd-4a0a-b892-627e72e35593-c7cca1d51b4b_f6.mp4",
            GeoLocations.GEO_NONE
          },
          {
            Sender.ARD,
            "http://pd-videos.daserste.de/de/2017/05/11/b8417c03-d979-4c33-9f5d-1185afb269a8/960-1.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.ARD,
            "http://pd-videos.daserste.de/int/2012/08/16/03ffc1df-d39a-4c72-acc5-49788a6656d2/960-1_1.mp4",
            GeoLocations.GEO_NONE
          },
          {
            Sender.ARD,
            "http://rbprogressivedl-a.akamaihd.net/clips/025/025626/025626_00093234_video_540p.mp4",
            GeoLocations.GEO_NONE
          },
          {
            Sender.ARD,
            "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/148/1480533/1480533_16978374.mp4",
            GeoLocations.GEO_NONE
          },
          {
            Sender.ARD,
            "https://arte-ard-mediathek.akamaized.net/am/mp4/084000/084600/084657-000-A_EQ_1_VOA_05911351_MP4-1500_AMM-IPTV-ARD_1ZbnIfgtFZ.mp4",
            GeoLocations.GEO_DE_FR
          },
          {
            Sender.KIKA,
            "http://media.ndr.de/progressive_geo/2021/0326/TV-20210326-1211-0800.lp.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.KIKA,
            "https://cdn-storage.br.de/geo/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_2ES/_-9S/_AxG9AxP571S/afe20098-9335-46e0-a5b5-48b713c1cff3_C.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.KIKA,
            "https://pmdgeokika-a.akamaihd.net/mp4dyn/3/FCMS-3247fdf9-a184-40c2-8d15-12cddcc9550a-31e0be270130_32.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.KIKA,
            "https://pmdonlinekika-a.akamaihd.net/mp4dyn/2/FCMS-23cafc73-1ef6-4194-80e1-0c2e62a6ad8f-31e0be270130_23.mp4",
            GeoLocations.GEO_NONE
          },
          {
            Sender.KIKA,
            "http://tvdlzdf-a.akamaihd.net/dach/tivi/20/09/200908_folge35_schnecken_besuch_zaz/3/200908_folge35_schnecken_besuch_zaz_2360k_p35v15.mp4",
            GeoLocations.GEO_DE_AT_CH
          },
          {
            Sender.KIKA,
            "http://tvdlzdf-a.akamaihd.net/de/tivi/21/03/210323_folge17_die_prophezeiung_zoo/3/210323_folge17_die_prophezeiung_zoo_2360k_p35v15.mp4",
            GeoLocations.GEO_DE
          },
          {
            Sender.KIKA,
            "http://tvdlzdf-a.akamaihd.net/none/tivi/19/01/190108_mwg18_folge7_jub/4/190108_mwg18_folge7_jub_2328k_p35v14.mp4",
            GeoLocations.GEO_NONE
          },
          {
            Sender.ZDF,
            "http://nrodl.zdf.de/dach/zdf/14/02/140211_sendung_nano_2256k_p14v11.mp4",
            GeoLocations.GEO_DE_AT_CH
          },
          {
            Sender.ZDF,
            "http://nrodl.zdf.de/de/zdf/15/10/151020_aufgedeckt6_inf/10/151020_aufgedeckt6_inf_2256k_p14v11.mp4",
            GeoLocations.GEO_DE
          }
        });
  }

  public GeoLocationGuesserTest(
      final Sender sender, final String url, final GeoLocations expectedGeoLocation) {

    this.sender = sender;
    this.url = url;
    this.expectedGeoLocation = expectedGeoLocation;
  }

  @Test
  public void geoLocations() {

    final Collection<GeoLocations> actualGeoLocations =
        GeoLocationGuesser.getGeoLocations(sender, url);

    assertThat(actualGeoLocations, Matchers.containsInAnyOrder(expectedGeoLocation));
  }
}
