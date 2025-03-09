package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class ArdVideoInfoJsonDeserializerTest extends WireMockTestBase {

  private final String jsonFile;
  private final String m3u8Url;
  private final String m3u8File;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;

  public ArdVideoInfoJsonDeserializerTest(
      final String aJsonFile,
      final String aM3u8Url,
      final String aM3u8File,
      final String aUrlSmall,
      final String aUrlNormal,
      final String aUrlHd) {
    jsonFile = aJsonFile;
    m3u8Url = aM3u8Url;
    m3u8File = aM3u8File;
    expectedUrlSmall = aUrlSmall;
    expectedUrlNormal = aUrlNormal;
    expectedUrlHd = aUrlHd;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/ard/ard_video_without_hd.json",
            "",
            "",
            "https://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/147/1471174/1471174_16874993.mp4",
            "https://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/147/1471174/1471174_16874995.mp4",
            null
          },
          {
            "/ard/ard_video_with_hd.json",
            "",
            "",
            "http://ondemand.mdr.de/mp4dyn/1/FCMS-14ec1b13-c7f6-4cc4-8b72-681cd22da39a-e9ebd6e42ce1_14.mp4",
            "http://ondemand.mdr.de/mp4dyn/1/FCMS-14ec1b13-c7f6-4cc4-8b72-681cd22da39a-c7cca1d51b4b_14.mp4",
            "http://ondemand.mdr.de/mp4dyn/1/FCMS-14ec1b13-c7f6-4cc4-8b72-681cd22da39a-15e7604ea4a4_14.mp4"
          },
          {
            "/ard/ard_video_normal_use_last.json",
            "",
            "",
            "https://mediastorage01.sr-online.de/Video/UD/DOKU/1505155201_20170911_KANDIDATENCHECK_LUKSIC_M.mp4",
            "https://srstorage01-a.akamaihd.net/Video/UD/DOKU/1505155201_20170911_KANDIDATENCHECK_LUKSIC_L.mp4",
            "https://srstorage01-a.akamaihd.net/Video/UD/DOKU/1505155201_20170911_KANDIDATENCHECK_LUKSIC_H.mp4"
          },
          {
            "/ard/ard_video_use_http_url.json",
            "",
            "",
            "http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bf/_-0S/_Abg5-xg5U1S/0f131ba9-c8e1-4368-be7b-799a75df221f_2.mp3",
            null,
            null
          },
          {
            "/ard/ard_video_with_quality_3_no_hd.json",
            "",
            "",
            "http://pd-videos.daserste.de/int/2017/09/08/7f35d2d7-9854-4187-b406-c56b9292de79/512-1.mp4",
            "http://pd-videos.daserste.de/int/2017/09/08/7f35d2d7-9854-4187-b406-c56b9292de79/960-1.mp4",
            null
          },
          {
            "/ard/ard_video_ndr_with_hd.json",
            "",
            "",
            "https://mediandr-a.akamaihd.net/progressive/2017/0915/TV-20170915-1645-5500.hi.mp4",
            "https://mediandr-a.akamaihd.net/progressive/2017/0915/TV-20170915-1645-5500.hq.mp4",
            "https://mediandr-a.akamaihd.net/progressive/2017/0915/TV-20170915-1645-5500.hd.mp4"
          },
          {
            "/ard/ard_video_swr_with_hd.json",
            "",
            "",
            "https://pdodswr-a.akamaihd.net/swr/swr-fernsehen/lust-auf-backen/985691.m.mp4",
            "https://pdodswr-a.akamaihd.net/swr/swr-fernsehen/lust-auf-backen/985691.l.mp4",
            "https://pdodswr-a.akamaihd.net/swr/swr-fernsehen/lust-auf-backen/985691.xl.mp4"
          },
          {
            "/ard/ard_video_ard_with_hd.json",
            "",
            "",
            "https://media.tagesschau.de/video/2017/1229/TV-20171229-1006-5301.webm.h264.mp4",
            "https://media.tagesschau.de/video/2017/1229/TV-20171229-1006-5301.webl.h264.mp4",
            "https://media.tagesschau.de/video/2017/1229/TV-20171229-1006-5301.webxl.h264.mp4"
          },
          {
            "/ard/ard_video_hr_with_hd.json",
            "",
            "",
            "https://hrardmediathek-a.akamaihd.net/video/as/allewetter/2017_12/hrLogo_171228193505_L279621_512x288-25p-500kbit.mp4",
            "https://hrardmediathek-a.akamaihd.net/video/as/allewetter/2017_12/hrLogo_171228193505_L279621_960x540-50p-1800kbit.mp4",
            "https://hrardmediathek-a.akamaihd.net/video/as/allewetter/2017_12/hrLogo_171228193505_L279621_1280x720-50p-5000kbit.mp4"
          },
          {
            "/ard/ard_video_mdr_with_hd.json",
            "",
            "",
            "https://odgeomdr-a.akamaihd.net/mp4dyn/7/FCMS-74bf126c-63dc-490d-a256-6c90aa6a21a6-e9ebd6e42ce1_74.mp4",
            "https://odgeomdr-a.akamaihd.net/mp4dyn/7/FCMS-74bf126c-63dc-490d-a256-6c90aa6a21a6-c7cca1d51b4b_74.mp4",
            "https://odgeomdr-a.akamaihd.net/mp4dyn/7/FCMS-74bf126c-63dc-490d-a256-6c90aa6a21a6-15e7604ea4a4_74.mp4"
          },
          {
            "/rbb/rbb_film_with_subtitle.json",
            "",
            "",
            "https://rbbmediapmdp-a.akamaihd.net/content/a0/93/a093d994-0ab0-498a-ae83-8e7647daa5db/3ccbfc08-3235-418f-9406-6c56cb89bb92_512k.mp4",
            "https://rbbmediapmdp-a.akamaihd.net/content/a0/93/a093d994-0ab0-498a-ae83-8e7647daa5db/3ccbfc08-3235-418f-9406-6c56cb89bb92_1800k.mp4",
            null
          },
          {
            "/sr/sr_film_video_details1.json",
            "",
            "",
            "https://srstorage01-a.akamaihd.net/Video/FS/SA/sportarena_20190815_184401_M.mp4",
            "https://srstorage01-a.akamaihd.net/Video/FS/SA/sportarena_20190815_184401_L.mp4",
            "https://srstorage01-a.akamaihd.net/Video/FS/SA/sportarena_20190815_184401_H.mp4"
          },
          {
            "/ndr/ndr_film_detail_m3u8.json",
            "/i/ndrfs_nds@430233/master.m3u8",
            "/ndr/ndr_film_detail_m3u8.m3u8",
            "https://ndrfs-lh.akamaihd.net/i/ndrfs_nds@430233/index_1216_av-b.m3u8",
            "https://ndrfs-lh.akamaihd.net/i/ndrfs_nds@430233/index_3776_av-b.m3u8",
            null
          },
          {
            "/ard/ard_video_alpha_centauri.json",
            "/i/mir-live/bw1XsLzS/bLQH/bLOliLioMXZhiKT1/uLoXb69zbX06/MUJIuUOVBwQIb71S/bLWCMUJIuUOVBwQIb71S/_2rp9U1S/_-JS/_-Fp_H1S/d6b48cc8-60f3-4625-a56a-fba68c0841c7_,0,A,B,E,C,.mp4.csmil/master.m3u8",
            "/ard/ard_video_alpha_centauri.m3u8",
            "http://br-i.akamaihd.net/i/mir-live/bw1XsLzS/bLQH/bLOliLioMXZhiKT1/uLoXb69zbX06/MUJIuUOVBwQIb71S/bLWCMUJIuUOVBwQIb71S/_2rp9U1S/_-JS/_-Fp_H1S/d6b48cc8-60f3-4625-a56a-fba68c0841c7_,0,A,B,E,C,.mp4.csmil/index_4_av.m3u8",
            null,
            null
          },
          {
            "/ard/ard_video_with_fullhd.json",
            "",
            "",
            "https://pdvideosdaserste-a.akamaihd.net/int/2019/12/16/24638a16-7006-4ca8-b3ad-4c5ef0dbc179/512-1_574972.mp4",
            "https://pdvideosdaserste-a.akamaihd.net/int/2019/12/16/24638a16-7006-4ca8-b3ad-4c5ef0dbc179/960-1_574972.mp4",
            "https://pdvideosdaserste-a.akamaihd.net/int/2019/12/16/24638a16-7006-4ca8-b3ad-4c5ef0dbc179/1920-1_574972.mp4"
          },
          {
            "/ard/ard_video_funk_m3u8.json",
            "/22679/files/21/12/22/4418917/22679-qhFBn6dpPtxVW9K.ism/manifest.m3u8.m3u8",
            "/ard/ard_video_funk.m3u8",
            "http://localhost:8589/22679/files/21/12/22/4418917/22679-qhFBn6dpPtxVW9K.ism/22679-qhFBn6dpPtxVW9K-audio=152000-video=748000.m3u8",
            "http://localhost:8589/22679/files/21/12/22/4418917/22679-qhFBn6dpPtxVW9K.ism/22679-qhFBn6dpPtxVW9K-audio=152000-video=2771000.m3u8",
            "http://localhost:8589/22679/files/21/12/22/4418917/22679-qhFBn6dpPtxVW9K.ism/22679-qhFBn6dpPtxVW9K-audio=152000-video=3838000.m3u8",
          }
        });
  }

  @Test
  public void deserializeTest() {

    final JsonElement jsonElement =
        JsonFileReader.readJsonWithTextModification(jsonFile, this::fixupAllWireMockUrls);

    if (!m3u8Url.isEmpty()) {
      setupSuccessfulResponse(m3u8Url, m3u8File);
    }

    final ArdVideoInfoJsonDeserializer target = new ArdVideoInfoJsonDeserializer(createCrawler());
    final ArdVideoInfoDto actual = target.deserialize(jsonElement, ArdVideoInfoDto.class, null);

    assertThat(actual, notNullValue());

    assertThat(
        actual.getVideoUrls().get(Resolution.SMALL),
        equalTo(fixupAllWireMockUrls(expectedUrlSmall)));
    assertThat(
        actual.getVideoUrls().get(Resolution.NORMAL),
        equalTo(fixupAllWireMockUrls(expectedUrlNormal)));
    assertThat(
        actual.getVideoUrls().get(Resolution.HD), equalTo(fixupAllWireMockUrls(expectedUrlHd)));
  }

  protected ArdCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ArdCrawler(
        forkJoinPool,
        nachrichten,
        fortschritte,
        new MServerConfigManager("MServer-JUnit-Config.yaml"));
  }
}
