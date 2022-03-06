package de.mediathekview.mserver.crawler.basic;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class M3U8ParserTest {

  private static M3U8Dto createDto(final String aUrl, final String[] keys, final String[] values) {

    final M3U8Dto dto = new M3U8Dto(aUrl);
    for (int i = 0; i < keys.length; i++) {
      dto.addMeta(keys[i], values[i]);
    }

    return dto;
  }

  @Test
  public void parseTestSrf() {

    final M3U8Dto[] expected =
        new M3U8Dto[] {
          createDto(
              "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_av.m3u8",
              new String[] {
                M3U8Constants.M3U8_PROGRAM_ID,
                M3U8Constants.M3U8_BANDWIDTH,
                M3U8Constants.M3U8_CODECS,
                M3U8Constants.M3U8_CLOSED_CAPTIONS
              },
              new String[] {"1", "118000", "\"avc1.66.30, mp4a.40.2\"", "NONE"}),
          createDto(
              "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_1_av.m3u8",
              new String[] {
                M3U8Constants.M3U8_PROGRAM_ID,
                M3U8Constants.M3U8_BANDWIDTH,
                M3U8Constants.M3U8_CODECS,
                M3U8Constants.M3U8_CLOSED_CAPTIONS
              },
              new String[] {"1", "739000", "\"avc1.77.30, mp4a.40.2\"", "NONE"}),
          createDto(
              "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_2_av.m3u8",
              new String[] {
                M3U8Constants.M3U8_PROGRAM_ID,
                M3U8Constants.M3U8_BANDWIDTH,
                M3U8Constants.M3U8_CODECS,
                M3U8Constants.M3U8_CLOSED_CAPTIONS
              },
              new String[] {"1", "1395000", "\"avc1.77.30, mp4a.40.2\"", "NONE"}),
          createDto(
              "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_a.m3u8",
              new String[] {
                M3U8Constants.M3U8_PROGRAM_ID,
                M3U8Constants.M3U8_BANDWIDTH,
                M3U8Constants.M3U8_CODECS,
                M3U8Constants.M3U8_CLOSED_CAPTIONS
              },
              new String[] {"1", "23000", "\"mp4a.40.2\"", "NONE"}),
        };

    final M3U8Parser target = new M3U8Parser();
    final List<M3U8Dto> actual =
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

    final M3U8Dto[] expected =
        new M3U8Dto[] {
          createDto(
              "https://ndrfs-lh.akamaihd.net/i/ndrfs_nds@430233/index_1216_av-b.m3u8",
              new String[] {
                M3U8Constants.M3U8_PROGRAM_ID,
                M3U8Constants.M3U8_BANDWIDTH,
                M3U8Constants.M3U8_RESOLUTION,
                M3U8Constants.M3U8_CODECS
              },
              new String[] {"1", "1216000", "640x360", "\"avc1.77.30, mp4a.40.2\""}),
          createDto(
              "https://ndrfs-lh.akamaihd.net/i/ndrfs_nds@430233/index_1992_av-p.m3u8",
              new String[] {
                M3U8Constants.M3U8_PROGRAM_ID,
                M3U8Constants.M3U8_BANDWIDTH,
                M3U8Constants.M3U8_RESOLUTION,
                M3U8Constants.M3U8_CODECS
              },
              new String[] {"1", "1992000", "960x540", "\"avc1.77.30, mp4a.40.2\""})
        };

    final M3U8Parser target = new M3U8Parser();
    final List<M3U8Dto> actual =
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

    final M3U8Dto[] expected =
        new M3U8Dto[] {
          createDto(
              "https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_3584.m3u8",
              new String[] {
                M3U8Constants.M3U8_BANDWIDTH,
                "AVERAGE-BANDWIDTH",
                M3U8Constants.M3U8_CODECS,
                M3U8Constants.M3U8_RESOLUTION,
                "FRAME-RATE"
              },
              new String[] {
                "5824896", "4118400", "\"avc1.640020,mp4a.40.2\"", "1280x720", "50.000"
              }),
          createDto(
              "https://rbbevent04-hls.akamaized.net/hls/live/685987-b/rbbevent04/master_3584.m3u8",
              new String[] {
                M3U8Constants.M3U8_BANDWIDTH,
                "AVERAGE-BANDWIDTH",
                M3U8Constants.M3U8_CODECS,
                M3U8Constants.M3U8_RESOLUTION,
                "FRAME-RATE"
              },
              new String[] {
                "5824896", "4118400", "\"avc1.640020,mp4a.40.2\"", "1280x720", "50.000"
              }),
          createDto(
              "https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_2500.m3u8",
              new String[] {
                M3U8Constants.M3U8_BANDWIDTH,
                "AVERAGE-BANDWIDTH",
                M3U8Constants.M3U8_CODECS,
                M3U8Constants.M3U8_RESOLUTION,
                "FRAME-RATE"
              },
              new String[] {"4301000", "2926000", "\"avc1.4d401f,mp4a.40.2\"", "960x540", "50.000"})
        };

    final M3U8Parser target = new M3U8Parser();
    final List<M3U8Dto> actual =
        target.parse(
            "#EXTM3U\n"
                + "#EXT-X-VERSION:3\n"
                + "#EXT-X-INDEPENDENT-SEGMENTS\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=5824896,AVERAGE-BANDWIDTH=4118400,CODECS=\"avc1.640020,mp4a.40.2\",RESOLUTION=1280x720,FRAME-RATE=50.000\n"
                + "https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_3584.m3u8\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=5824896,AVERAGE-BANDWIDTH=4118400,CODECS=\"avc1.640020,mp4a.40.2\",RESOLUTION=1280x720,FRAME-RATE=50.000\n"
                + "https://rbbevent04-hls.akamaized.net/hls/live/685987-b/rbbevent04/master_3584.m3u8\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=4301000,AVERAGE-BANDWIDTH=2926000,CODECS=\"avc1.4d401f,mp4a.40.2\",RESOLUTION=960x540,FRAME-RATE=50.000\n"
                + "https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_2500.m3u8\n");

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(3));

    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }


  @Test
  public void parseTestArdFunk() {

    final M3U8Dto[] expected =
            new M3U8Dto[] {
                    createDto(
                            "22679-qhFBn6dpPtxVW9K-audio=128000-video=316000.m3u8",
                            new String[] {
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_RESOLUTION,
                                    "AUDIO",
                                    M3U8Constants.M3U8_CLOSED_CAPTIONS
                            },
                            new String[] {
                                    "471000", "\"mp4a.40.2,avc1.4D401F\"", "426x240", "\"audio-aacl-128\"", "NONE"
                            }),
                    createDto(
                            "22679-qhFBn6dpPtxVW9K-audio=152000-video=748000.m3u8",
                            new String[] {
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_RESOLUTION,
                                    "AUDIO",
                                    M3U8Constants.M3U8_CLOSED_CAPTIONS
                            },
                            new String[] {
                                    "954000", "\"mp4a.40.2,avc1.4D401F\"", "640x360", "\"audio-aacl-152\"", "NONE"
                            }),
                    createDto(
                            "22679-qhFBn6dpPtxVW9K-audio=152000-video=1451000.m3u8",
                            new String[] {
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_RESOLUTION,
                                    "AUDIO",
                                    M3U8Constants.M3U8_CLOSED_CAPTIONS
                            },
                            new String[] {
                                    "1700000", "\"mp4a.40.2,avc1.4D401F\"", "1024x576", "\"audio-aacl-152\"", "NONE"
                            }),
                    createDto(
                            "22679-qhFBn6dpPtxVW9K-audio=152000-video=2771000.m3u8",
                            new String[] {
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_RESOLUTION,
                                    "AUDIO",
                                    M3U8Constants.M3U8_CLOSED_CAPTIONS
                            },
                            new String[] {
                                    "3099000", "\"mp4a.40.2,avc1.4D401F\"", "1280x720", "\"audio-aacl-152\"", "NONE"
                            }),
                    createDto(
                            "22679-qhFBn6dpPtxVW9K-audio=152000-video=3838000.m3u8",
                            new String[] {
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    M3U8Constants.M3U8_RESOLUTION,
                                    "AUDIO",
                                    M3U8Constants.M3U8_CLOSED_CAPTIONS
                            },
                            new String[] {
                                    "4230000", "\"mp4a.40.2,avc1.4D401F\"", "1920x1080", "\"audio-aacl-152\"", "NONE"
                            }),
                    createDto(
                            "22679-qhFBn6dpPtxVW9K-audio=128000.m3u8",
                            new String[] {
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    "AUDIO"
                            },
                            new String[] {
                                    "136000", "\"mp4a.40.2\"", "\"audio-aacl-128\""
                            }),
                    createDto(
                            "22679-qhFBn6dpPtxVW9K-audio=152000.m3u8",
                            new String[] {
                                    M3U8Constants.M3U8_BANDWIDTH,
                                    M3U8Constants.M3U8_CODECS,
                                    "AUDIO"
                            },
                            new String[] {
                                    "162000", "\"mp4a.40.2\"", "\"audio-aacl-152\""
                            }),
            };

    final M3U8Parser target = new M3U8Parser();
    final List<M3U8Dto> actual =
        target.parse(
            "#EXTM3U\n"
                + "#EXT-X-VERSION:4\n"
                + "## Created with Unified Streaming Platform  (version=1.11.3-24483)\n"
                + "\n"
                + "# AUDIO groups\n"
                + "#EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID=\"audio-aacl-128\",NAME=\"audio\",DEFAULT=YES,AUTOSELECT=YES,CHANNELS=\"2\"\n"
                + "#EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID=\"audio-aacl-152\",NAME=\"audio\",DEFAULT=YES,AUTOSELECT=YES,CHANNELS=\"2\"\n"
                + "\n"
                + "# variants\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=471000,CODECS=\"mp4a.40.2,avc1.4D401F\",RESOLUTION=426x240,AUDIO=\"audio-aacl-128\",CLOSED-CAPTIONS=NONE\n"
                + "22679-qhFBn6dpPtxVW9K-audio=128000-video=316000.m3u8\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=954000,CODECS=\"mp4a.40.2,avc1.4D401F\",RESOLUTION=640x360,AUDIO=\"audio-aacl-152\",CLOSED-CAPTIONS=NONE\n"
                + "22679-qhFBn6dpPtxVW9K-audio=152000-video=748000.m3u8\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=1700000,CODECS=\"mp4a.40.2,avc1.4D401F\",RESOLUTION=1024x576,AUDIO=\"audio-aacl-152\",CLOSED-CAPTIONS=NONE\n"
                + "22679-qhFBn6dpPtxVW9K-audio=152000-video=1451000.m3u8\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=3099000,CODECS=\"mp4a.40.2,avc1.4D401F\",RESOLUTION=1280x720,AUDIO=\"audio-aacl-152\",CLOSED-CAPTIONS=NONE\n"
                + "22679-qhFBn6dpPtxVW9K-audio=152000-video=2771000.m3u8\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=4230000,CODECS=\"mp4a.40.2,avc1.4D401F\",RESOLUTION=1920x1080,AUDIO=\"audio-aacl-152\",CLOSED-CAPTIONS=NONE\n"
                + "22679-qhFBn6dpPtxVW9K-audio=152000-video=3838000.m3u8\n"
                + "\n"
                + "# variants\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=136000,CODECS=\"mp4a.40.2\",AUDIO=\"audio-aacl-128\"\n"
                + "22679-qhFBn6dpPtxVW9K-audio=128000.m3u8\n"
                + "#EXT-X-STREAM-INF:BANDWIDTH=162000,CODECS=\"mp4a.40.2\",AUDIO=\"audio-aacl-152\"\n"
                + "22679-qhFBn6dpPtxVW9K-audio=152000.m3u8\n"
                + "\n"
                + "# keyframes\n"
                + "#EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=42000,CODECS=\"avc1.4D401F\",RESOLUTION=426x240,URI=\"keyframes/22679-qhFBn6dpPtxVW9K-video=316000.m3u8\"\n"
                + "#EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=100000,CODECS=\"avc1.4D401F\",RESOLUTION=640x360,URI=\"keyframes/22679-qhFBn6dpPtxVW9K-video=748000.m3u8\"\n"
                + "#EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=193000,CODECS=\"avc1.4D401F\",RESOLUTION=1024x576,URI=\"keyframes/22679-qhFBn6dpPtxVW9K-video=1451000.m3u8\"\n"
                + "#EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=368000,CODECS=\"avc1.4D401F\",RESOLUTION=1280x720,URI=\"keyframes/22679-qhFBn6dpPtxVW9K-video=2771000.m3u8\"\n"
                + "#EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=509000,CODECS=\"avc1.4D401F\",RESOLUTION=1920x1080,URI=\"keyframes/22679-qhFBn6dpPtxVW9K-video=3838000.m3u8\"");

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(7));

    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

}
