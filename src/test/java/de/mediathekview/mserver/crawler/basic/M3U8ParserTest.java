package de.mediathekview.mserver.crawler.basic;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class M3U8ParserTest {

  private static M3U8Dto createDto(String aUrl, String[] keys, String[] values) {

    M3U8Dto dto = new M3U8Dto(aUrl);
    for (int i = 0; i < keys.length; i++) {
      dto.addMeta(keys[i], values[i]);
    }

    return dto;
  }

  @Test
  public void parseTestSrf() {

    M3U8Dto[] expected =
            new M3U8Dto[]{
                    createDto(
                            "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_av.m3u8",
                            new String[]{
                                    M3U8Constants.M3U8_PROGRAM_ID,
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_CLOSED_CAPTIONS
                            },
                            new String[]{"1", "118000", "\"avc1.66.30, mp4a.40.2\"", "NONE"}),
                    createDto(
                            "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_1_av.m3u8",
                            new String[]{
                                    M3U8Constants.M3U8_PROGRAM_ID,
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_CLOSED_CAPTIONS
                            },
                            new String[]{"1", "739000", "\"avc1.77.30, mp4a.40.2\"", "NONE"}),
                    createDto(
                            "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_2_av.m3u8",
                            new String[]{
                                    M3U8Constants.M3U8_PROGRAM_ID,
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_CLOSED_CAPTIONS
                            },
                            new String[]{"1", "1395000", "\"avc1.77.30, mp4a.40.2\"", "NONE"}),
                    createDto(
                            "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_a.m3u8",
                            new String[]{
                                    M3U8Constants.M3U8_PROGRAM_ID,
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_CLOSED_CAPTIONS
                            },
                            new String[]{"1", "23000", "\"mp4a.40.2\"", "NONE"}),
            };

    M3U8Parser target = new M3U8Parser();
    List<M3U8Dto> actual =
            target.parse(
                    "#EXTM3U\n"
                            + "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=118000,CODECS=\"avc1.66.30, mp4a.40.2\",CLOSED-CAPTIONS=NONE\n"
                            + "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_av.m3u8?start=0.0&end=3305.1\n"
                            + "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=739000,CODECS=\"avc1.77.30, mp4a.40.2\",CLOSED-CAPTIONS=NONE\n"
                            + "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_1_av.m3u8?start=0.0&end=3305.1\n"
                            + "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1395000,CODECS=\"avc1.77.30, mp4a.40.2\",CLOSED-CAPTIONS=NONE\n"
                            + "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_2_av.m3u8?start=0.0&end=3305.1\n"
                            + "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=23000,CODECS=\"mp4a.40.2\",CLOSED-CAPTIONS=NONE\n"
                            + "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_a.m3u8?start=0.0&end=3305.1\n"
                            + "");

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(4));

    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

  @Test
  public void parseTestArd() {

    M3U8Dto[] expected =
            new M3U8Dto[]{
                    createDto(
                            "https://ndrfs-lh.akamaihd.net/i/ndrfs_nds@430233/index_1216_av-b.m3u8",
                            new String[]{
                                    M3U8Constants.M3U8_PROGRAM_ID,
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_RESOLUTION,
                                    M3U8Constants.M3U8_CODECS
                            },
                            new String[]{"1", "1216000", "640x360", "\"avc1.77.30, mp4a.40.2\""}),
                    createDto(
                            "https://ndrfs-lh.akamaihd.net/i/ndrfs_nds@430233/index_1992_av-p.m3u8",
                            new String[]{
                                    M3U8Constants.M3U8_PROGRAM_ID,
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_RESOLUTION,
                                    M3U8Constants.M3U8_CODECS
                            },
                            new String[]{"1", "1992000", "960x540", "\"avc1.77.30, mp4a.40.2\""})
            };

    M3U8Parser target = new M3U8Parser();
    List<M3U8Dto> actual =
            target.parse(
                    "#EXTM3U\n"
                            + "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1216000,RESOLUTION=640x360,CODECS=\"avc1.77.30, mp4a.40.2\"\n"
                            + "https://ndrfs-lh.akamaihd.net/i/ndrfs_nds@430233/index_1216_av-b.m3u8?sd=10&rebase=on&id=\n"
                            + "#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1992000,RESOLUTION=960x540,CODECS=\"avc1.77.30, mp4a.40.2\"\n"
                            + "https://ndrfs-lh.akamaihd.net/i/ndrfs_nds@430233/index_1992_av-p.m3u8?sd=10&rebase=on&id=\n"
                            + "");

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(2));

    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

  @Test
  public void parseTestArdNoQuotedElementAtTheEnd() {

    M3U8Dto[] expected =
            new M3U8Dto[]{
                    createDto(
                            "https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_3584.m3u8",
                            new String[]{
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    "AVERAGE-BANDWIDTH",
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_RESOLUTION,
                                    "FRAME-RATE"
                            },
                            new String[]{"5824896", "4118400", "\"avc1.640020,mp4a.40.2\"", "1280x720", "50.000"}),
                    createDto(
                            "https://rbbevent04-hls.akamaized.net/hls/live/685987-b/rbbevent04/master_3584.m3u8",
                            new String[]{
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    "AVERAGE-BANDWIDTH",
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_RESOLUTION,
                                    "FRAME-RATE"
                            },
                            new String[]{"5824896", "4118400", "\"avc1.640020,mp4a.40.2\"", "1280x720", "50.000"}),
                    createDto(
                            "https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_2500.m3u8",
                            new String[]{
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    "AVERAGE-BANDWIDTH",
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_RESOLUTION,
                                    "FRAME-RATE"
                            },
                            new String[]{"4301000", "2926000", "\"avc1.4d401f,mp4a.40.2\"", "960x540", "50.000"})
            };

    M3U8Parser target = new M3U8Parser();
    List<M3U8Dto> actual =

            target.parse("#EXTM3U\n" +
                    "#EXT-X-VERSION:3\n" +
                    "#EXT-X-INDEPENDENT-SEGMENTS\n" +
                    "#EXT-X-STREAM-INF:BANDWIDTH=5824896,AVERAGE-BANDWIDTH=4118400,CODECS=\"avc1.640020,mp4a.40.2\",RESOLUTION=1280x720,FRAME-RATE=50.000\n" +
                    "https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_3584.m3u8\n" +
                    "#EXT-X-STREAM-INF:BANDWIDTH=5824896,AVERAGE-BANDWIDTH=4118400,CODECS=\"avc1.640020,mp4a.40.2\",RESOLUTION=1280x720,FRAME-RATE=50.000\n" +
                    "https://rbbevent04-hls.akamaized.net/hls/live/685987-b/rbbevent04/master_3584.m3u8\n" +
                    "#EXT-X-STREAM-INF:BANDWIDTH=4301000,AVERAGE-BANDWIDTH=2926000,CODECS=\"avc1.4d401f,mp4a.40.2\",RESOLUTION=960x540,FRAME-RATE=50.000\n" +
                    "https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_2500.m3u8\n");

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(3));

    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
