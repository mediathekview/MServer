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
public class GeoLocationGuesserArdTest extends TestCase {

  private final String url;
  private final GeoLocations expectedGeoLocation;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "https://odgeomdr-a.akamaihd.net/mp4dyn2/c/FCMS-c20d56bc-be5f-4eb0-928a-0177777823ba-730aae549c28_c2.mp4",
            GeoLocations.GEO_DE
          },
          {
            "https://cdn-storage.br.de/geo/b7/2021-05/30/e6772ebc-c0db-11eb-81f0-02420a00057c_C.mp4",
            GeoLocations.GEO_DE
          },
          {
            "https://mediandr-a.akamaihd.net/progressive_geo/2018/0616/TV-20180616-1136-2500.hq.mp4",
            GeoLocations.GEO_DE
          },
          {
            "http://media.ndr.de/progressive_geo/2016/0716/TV-20160716-1300-3600.hq.mp4",
            GeoLocations.GEO_DE
          },
          {
            "https://pdodswr-a.akamaihd.net/swrfernsehen/geo/de/geldmachtliebe/1469376.l.mp4",
            GeoLocations.GEO_DE
          },
          {
            "https://rbbmediapmdp-a.akamaihd.net/content-de/d2/7d/d27d4fc4-af2f-48bd-b821-993af741785f/d27d4fc4-af2f-48bd-b821-993af741785f_hd1080-1800k.mp4",
            GeoLocations.GEO_DE
          },
          {
            "https://pdvideosdaserste-a.akamaihd.net/de/2021/05/13/16d7c4ca-507d-4560-bfdd-a1c1778f2942/960-1_900223.mp4",
            GeoLocations.GEO_DE
          },
          {
            "http://wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk0/227/2271942/2271942_29483837.mp4",
            GeoLocations.GEO_DE
          },
          {
            "https://hrardmediathek-a.akamaihd.net/video/as/geoblocking/2021_06/hrLogo_210605093605_0215219_960x540-50p-1800kbit.mp4",
            GeoLocations.GEO_DE
          },
          {
            "https://pdvideosdaserste-a.akamaihd.net/dach/2021/02/05/bcc5d307-7b30-4386-b037-1c6dcd6ab428/960-1_838479.mp4",
            GeoLocations.GEO_DE_AT_CH
          },
          {
            "https://wdrmedien-a.akamaihd.net/medp/ondemand/deChAt/fsk0/244/2442535/2442535_35333716.mp4",
            GeoLocations.GEO_DE_AT_CH
          },
          {
            "http://cdn-storage.br.de/MUJIuUOVBwQIbtC2uKJDM6OhuLnC_2rH_K1S/_-JS/_AvH_AbG_71S/192b42c4-d7ba-4b46-81ef-3c03452b762a_C.mp4",
            GeoLocations.GEO_NONE
          },
          {
            "http://hrardmediathek-a.akamaihd.net/video/as/allewetter/2020_10/hrLogo_201030194106_L383725_960x540-50p-1800kbit.mp4",
            GeoLocations.GEO_NONE
          },
          {
            "http://media.ndr.de/progressive/2015/0416/TV-20150416-2256-2542.hq.mp4",
            GeoLocations.GEO_NONE
          },
          {
            "http://mediandr-a.akamaihd.net/progressive/2019/0222/TV-20190222-1418-1100.hq.mp4",
            GeoLocations.GEO_NONE
          },
          {
            "http://ondemand.mdr.de/mp4dyn/f/FCMS-f6eea84a-a8fd-4a0a-b892-627e72e35593-c7cca1d51b4b_f6.mp4",
            GeoLocations.GEO_NONE
          },
          {
            "http://pd-videos.daserste.de/de/2017/05/11/b8417c03-d979-4c33-9f5d-1185afb269a8/960-1.mp4",
            GeoLocations.GEO_DE
          },
          {
            "http://pd-videos.daserste.de/int/2012/08/16/03ffc1df-d39a-4c72-acc5-49788a6656d2/960-1_1.mp4",
            GeoLocations.GEO_NONE
          },
          {
            "http://rbprogressivedl-a.akamaihd.net/clips/025/025626/025626_00093234_video_540p.mp4",
            GeoLocations.GEO_NONE
          },
          {
            "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/148/1480533/1480533_16978374.mp4",
            GeoLocations.GEO_NONE
          },
          {
            "https://arte-ard-mediathek.akamaized.net/am/mp4/084000/084600/084657-000-A_EQ_1_VOA_05911351_MP4-1500_AMM-IPTV-ARD_1ZbnIfgtFZ.mp4",
            GeoLocations.GEO_DE_FR
          }
        });
  }

  public GeoLocationGuesserArdTest(final String url, final GeoLocations expectedGeoLocation) {

    this.url = url;
    this.expectedGeoLocation = expectedGeoLocation;
  }

  @Test
  public void geoLocations() {

    final Collection<GeoLocations> actualGeoLocations =
        GeoLocationGuesser.getGeoLocations(Sender.ARD, url);

    assertThat(actualGeoLocations, Matchers.containsInAnyOrder(expectedGeoLocation));
  }
}
