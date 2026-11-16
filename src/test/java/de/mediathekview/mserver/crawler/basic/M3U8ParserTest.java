package de.mediathekview.mserver.crawler.basic;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;

import de.mediathekview.mserver.testhelper.FileReader;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class M3U8ParserTest {

  private static M3U8Dto createDto(final String aUrl, final String[] keys, final String[] values) {

    final M3U8Dto dto = new M3U8Dto(aUrl);
    addMeta(dto, keys, values);

    return dto;
  }

  private static void addMeta(final M3U8Dto dto, final String[] keys, final String[] values) {
    for (int i = 0; i < keys.length; i++) {
      dto.addMeta(keys[i], values[i]);
    }
  }

  @Test
  void parseTestSrf() {

    final M3U8Dto[] expected = {
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
            """
            #EXTM3U
            #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=118000,CODECS="avc1.66.30, mp4a.40.2",CLOSED-CAPTIONS=NONE
            https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_av.m3u8?start=0.0&end=3305.1
            #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=739000,CODECS="avc1.77.30, mp4a.40.2",CLOSED-CAPTIONS=NONE
            https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_1_av.m3u8?start=0.0&end=3305.1
            #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1395000,CODECS="avc1.77.30, mp4a.40.2",CLOSED-CAPTIONS=NONE
            https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_2_av.m3u8?start=0.0&end=3305.1
            #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=23000,CODECS="mp4a.40.2",CLOSED-CAPTIONS=NONE
            https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_a.m3u8?start=0.0&end=3305.1
            """);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(4));

    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

  @Test
  void parseTestSrf2() {

    final M3U8Dto[] expected = {
      createDto(
          "QualityLevels(128000)/Manifest(audio_deu,format=m3u8-aapl)",
          new String[] {
            "SUBTITLES",
            M3U8Constants.M3U8_CLOSED_CAPTIONS,
            M3U8Constants.M3U8_BANDWIDTH,
            M3U8Constants.M3U8_CODECS,
            "AUDIO"
          },
          new String[] {"\"subs\"", "NONE", "138976", "\"mp4a.40.2\"", "\"audio\""}),
      createDto(
          "QualityLevels(499936)/Manifest(video,format=m3u8-aapl)",
          new String[] {
            "SUBTITLES",
            M3U8Constants.M3U8_CLOSED_CAPTIONS,
            M3U8Constants.M3U8_BANDWIDTH,
            M3U8Constants.M3U8_RESOLUTION,
            M3U8Constants.M3U8_CODECS,
            "AUDIO"
          },
          new String[] {
            "\"subs\"", "NONE", "658070", "480x270", "\"avc1.4d4015,mp4a.40.2\"", "\"audio\""
          }),
      createDto(
          "QualityLevels(249968)/Manifest(video,format=m3u8-aapl)",
          new String[] {
            "SUBTITLES",
            M3U8Constants.M3U8_CLOSED_CAPTIONS,
            M3U8Constants.M3U8_BANDWIDTH,
            M3U8Constants.M3U8_RESOLUTION,
            M3U8Constants.M3U8_CODECS,
            "AUDIO"
          },
          new String[] {
            "\"subs\"", "NONE", "402603", "320x180", "\"avc1.42c00c,mp4a.40.2\"", "\"audio\""
          }),
      createDto(
          "QualityLevels(1199924)/Manifest(video,format=m3u8-aapl)",
          new String[] {
            "SUBTITLES",
            M3U8Constants.M3U8_CLOSED_CAPTIONS,
            M3U8Constants.M3U8_BANDWIDTH,
            M3U8Constants.M3U8_RESOLUTION,
            M3U8Constants.M3U8_CODECS,
            "AUDIO"
          },
          new String[] {
            "\"subs\"", "NONE", "1373458", "640x360", "\"avc1.4d401e,mp4a.40.2\"", "\"audio\""
          }),
      createDto(
          "QualityLevels(1999872)/Manifest(video,format=m3u8-aapl)",
          new String[] {
            "SUBTITLES",
            M3U8Constants.M3U8_CLOSED_CAPTIONS,
            M3U8Constants.M3U8_BANDWIDTH,
            M3U8Constants.M3U8_RESOLUTION,
            M3U8Constants.M3U8_CODECS,
            "AUDIO"
          },
          new String[] {
            "\"subs\"", "NONE", "2191005", "960x540", "\"avc1.4d401f,mp4a.40.2\"", "\"audio\""
          }),
    };

    final M3U8Parser target = new M3U8Parser();
    final List<M3U8Dto> actual =
        target.parse(
            """
            Optional[#EXTM3U
            #EXT-X-VERSION:5
            #EXT-X-STREAM-INF:SUBTITLES="subs",CLOSED-CAPTIONS=NONE,BANDWIDTH=138976,CODECS="mp4a.40.2",AUDIO="audio"
            QualityLevels(128000)/Manifest(audio_deu,format=m3u8-aapl)
            #EXT-X-STREAM-INF:SUBTITLES="subs",CLOSED-CAPTIONS=NONE,BANDWIDTH=658070,RESOLUTION=480x270,CODECS="avc1.4d4015,mp4a.40.2",AUDIO="audio"
            QualityLevels(499936)/Manifest(video,format=m3u8-aapl)
            #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=658070,RESOLUTION=480x270,CODECS="avc1.4d4015",URI="QualityLevels(499936)/Manifest(video,format=m3u8-aapl,type=keyframes)"
            #EXT-X-STREAM-INF:SUBTITLES="subs",CLOSED-CAPTIONS=NONE,BANDWIDTH=402603,RESOLUTION=320x180,CODECS="avc1.42c00c,mp4a.40.2",AUDIO="audio"
            QualityLevels(249968)/Manifest(video,format=m3u8-aapl)
            #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=402603,RESOLUTION=320x180,CODECS="avc1.42c00c",URI="QualityLevels(249968)/Manifest(video,format=m3u8-aapl,type=keyframes)"
            #EXT-X-STREAM-INF:SUBTITLES="subs",CLOSED-CAPTIONS=NONE,BANDWIDTH=1373458,RESOLUTION=640x360,CODECS="avc1.4d401e,mp4a.40.2",AUDIO="audio"
            QualityLevels(1199924)/Manifest(video,format=m3u8-aapl)
            #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=1373458,RESOLUTION=640x360,CODECS="avc1.4d401e",URI="QualityLevels(1199924)/Manifest(video,format=m3u8-aapl,type=keyframes)"
            #EXT-X-STREAM-INF:SUBTITLES="subs",CLOSED-CAPTIONS=NONE,BANDWIDTH=2191005,RESOLUTION=960x540,CODECS="avc1.4d401f,mp4a.40.2",AUDIO="audio"
            QualityLevels(1999872)/Manifest(video,format=m3u8-aapl)
            #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=2191005,RESOLUTION=960x540,CODECS="avc1.4d401f",URI="QualityLevels(1999872)/Manifest(video,format=m3u8-aapl,type=keyframes)"]\
            """);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(5));

    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

  @Test
  void parseTestArd() {

    final M3U8Dto[] expected = {
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
            """
            #EXTM3U
            #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1216000,RESOLUTION=640x360,CODECS="avc1.77.30, mp4a.40.2"
            https://ndrfs-lh.akamaihd.net/i/ndrfs_nds@430233/index_1216_av-b.m3u8?sd=10&rebase=on&id=
            #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1992000,RESOLUTION=960x540,CODECS="avc1.77.30, mp4a.40.2"
            https://ndrfs-lh.akamaihd.net/i/ndrfs_nds@430233/index_1992_av-p.m3u8?sd=10&rebase=on&id=
            """);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(2));

    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

  @Test
  void parseTestArdNoQuotedElementAtTheEnd() {

    final M3U8Dto[] expected = {
      createDto(
          "https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_3584.m3u8",
          new String[] {
            M3U8Constants.M3U8_BANDWIDTH,
            "AVERAGE-BANDWIDTH",
            M3U8Constants.M3U8_CODECS,
            M3U8Constants.M3U8_RESOLUTION,
            "FRAME-RATE"
          },
          new String[] {"5824896", "4118400", "\"avc1.640020,mp4a.40.2\"", "1280x720", "50.000"}),
      createDto(
          "https://rbbevent04-hls.akamaized.net/hls/live/685987-b/rbbevent04/master_3584.m3u8",
          new String[] {
            M3U8Constants.M3U8_BANDWIDTH,
            "AVERAGE-BANDWIDTH",
            M3U8Constants.M3U8_CODECS,
            M3U8Constants.M3U8_RESOLUTION,
            "FRAME-RATE"
          },
          new String[] {"5824896", "4118400", "\"avc1.640020,mp4a.40.2\"", "1280x720", "50.000"}),
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
            """
            #EXTM3U
            #EXT-X-VERSION:3
            #EXT-X-INDEPENDENT-SEGMENTS
            #EXT-X-STREAM-INF:BANDWIDTH=5824896,AVERAGE-BANDWIDTH=4118400,CODECS="avc1.640020,mp4a.40.2",RESOLUTION=1280x720,FRAME-RATE=50.000
            https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_3584.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=5824896,AVERAGE-BANDWIDTH=4118400,CODECS="avc1.640020,mp4a.40.2",RESOLUTION=1280x720,FRAME-RATE=50.000
            https://rbbevent04-hls.akamaized.net/hls/live/685987-b/rbbevent04/master_3584.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=4301000,AVERAGE-BANDWIDTH=2926000,CODECS="avc1.4d401f,mp4a.40.2",RESOLUTION=960x540,FRAME-RATE=50.000
            https://rbbevent04-hls.akamaized.net/hls/live/685987/rbbevent04/master_2500.m3u8
            """);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(3));

    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

  @Test
  void parseTestArdFunk() {

    final M3U8Dto[] expected = {
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
          new String[] {M3U8Constants.M3U8_BANDWIDTH, M3U8Constants.M3U8_CODECS, "AUDIO"},
          new String[] {"136000", "\"mp4a.40.2\"", "\"audio-aacl-128\""}),
      createDto(
          "22679-qhFBn6dpPtxVW9K-audio=152000.m3u8",
          new String[] {M3U8Constants.M3U8_BANDWIDTH, M3U8Constants.M3U8_CODECS, "AUDIO"},
          new String[] {"162000", "\"mp4a.40.2\"", "\"audio-aacl-152\""}),
    };

    final M3U8Parser target = new M3U8Parser();
    final List<M3U8Dto> actual =
        target.parse(
            """
            #EXTM3U
            #EXT-X-VERSION:4
            ## Created with Unified Streaming Platform  (version=1.11.3-24483)

            # variants
            #EXT-X-STREAM-INF:BANDWIDTH=471000,CODECS="mp4a.40.2,avc1.4D401F",RESOLUTION=426x240,AUDIO="audio-aacl-128",CLOSED-CAPTIONS=NONE
            22679-qhFBn6dpPtxVW9K-audio=128000-video=316000.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=954000,CODECS="mp4a.40.2,avc1.4D401F",RESOLUTION=640x360,AUDIO="audio-aacl-152",CLOSED-CAPTIONS=NONE
            22679-qhFBn6dpPtxVW9K-audio=152000-video=748000.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=1700000,CODECS="mp4a.40.2,avc1.4D401F",RESOLUTION=1024x576,AUDIO="audio-aacl-152",CLOSED-CAPTIONS=NONE
            22679-qhFBn6dpPtxVW9K-audio=152000-video=1451000.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=3099000,CODECS="mp4a.40.2,avc1.4D401F",RESOLUTION=1280x720,AUDIO="audio-aacl-152",CLOSED-CAPTIONS=NONE
            22679-qhFBn6dpPtxVW9K-audio=152000-video=2771000.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=4230000,CODECS="mp4a.40.2,avc1.4D401F",RESOLUTION=1920x1080,AUDIO="audio-aacl-152",CLOSED-CAPTIONS=NONE
            22679-qhFBn6dpPtxVW9K-audio=152000-video=3838000.m3u8

            # variants
            #EXT-X-STREAM-INF:BANDWIDTH=136000,CODECS="mp4a.40.2",AUDIO="audio-aacl-128"
            22679-qhFBn6dpPtxVW9K-audio=128000.m3u8
            #EXT-X-STREAM-INF:BANDWIDTH=162000,CODECS="mp4a.40.2",AUDIO="audio-aacl-152"
            22679-qhFBn6dpPtxVW9K-audio=152000.m3u8

            # keyframes
            #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=42000,CODECS="avc1.4D401F",RESOLUTION=426x240,URI="keyframes/22679-qhFBn6dpPtxVW9K-video=316000.m3u8"
            #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=100000,CODECS="avc1.4D401F",RESOLUTION=640x360,URI="keyframes/22679-qhFBn6dpPtxVW9K-video=748000.m3u8"
            #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=193000,CODECS="avc1.4D401F",RESOLUTION=1024x576,URI="keyframes/22679-qhFBn6dpPtxVW9K-video=1451000.m3u8"
            #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=368000,CODECS="avc1.4D401F",RESOLUTION=1280x720,URI="keyframes/22679-qhFBn6dpPtxVW9K-video=2771000.m3u8"
            #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=509000,CODECS="avc1.4D401F",RESOLUTION=1920x1080,URI="keyframes/22679-qhFBn6dpPtxVW9K-video=3838000.m3u8"\
            """);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(7));

    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

  @Test
  void parseTestArte() {

    final M3U8Parser target = new M3U8Parser();
    final List<M3U8Dto> actual =
        target.parse(
            """
            #EXTM3U
           #EXT-X-VERSION:7
           #EXT-X-INDEPENDENT-SEGMENTS

           #EXT-X-STREAM-INF:BANDWIDTH=1884504,AVERAGE-BANDWIDTH=640472,VIDEO-RANGE=SDR,CODECS="avc1.4D001E,mp4a.40.2",RESOLUTION=768x432,FRAME-RATE=25.000,SCORE=3.1,HDCP-LEVEL=NONE,CLOSED-CAPTIONS=NONE,AUDIO="audio_0",SUBTITLES="subtitle_0"
           https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v432_crf_h264.m3u8
           #EXT-X-STREAM-INF:BANDWIDTH=4655536,AVERAGE-BANDWIDTH=1967608,VIDEO-RANGE=SDR,CODECS="avc1.640028,mp4a.40.2",RESOLUTION=1920x1080,FRAME-RATE=25.000,SCORE=5.1,HDCP-LEVEL=NONE,CLOSED-CAPTIONS=NONE,AUDIO="audio_0",SUBTITLES="subtitle_0"
           https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v1080_crf_h264.m3u8
           #EXT-X-STREAM-INF:BANDWIDTH=3002704,AVERAGE-BANDWIDTH=1089312,VIDEO-RANGE=SDR,CODECS="avc1.64001F,mp4a.40.2",RESOLUTION=1280x720,FRAME-RATE=25.000,SCORE=4.1,HDCP-LEVEL=NONE,CLOSED-CAPTIONS=NONE,AUDIO="audio_0",SUBTITLES="subtitle_0"
           https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v720_crf_h264.m3u8
           #EXT-X-STREAM-INF:BANDWIDTH=1850088,AVERAGE-BANDWIDTH=551224,VIDEO-RANGE=SDR,CODECS="hev1.1.6.L90.B0,mp4a.40.2",RESOLUTION=768x432,FRAME-RATE=25.000,SCORE=3.2,HDCP-LEVEL=NONE,CLOSED-CAPTIONS=NONE,AUDIO="audio_0",SUBTITLES="subtitle_0"
           https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v432_crf_h265.m3u8
           #EXT-X-STREAM-INF:BANDWIDTH=4745064,AVERAGE-BANDWIDTH=1831248,VIDEO-RANGE=SDR,CODECS="hev1.1.6.L120.B0,mp4a.40.2",RESOLUTION=1920x1080,FRAME-RATE=25.000,SCORE=5.2,HDCP-LEVEL=NONE,CLOSED-CAPTIONS=NONE,AUDIO="audio_0",SUBTITLES="subtitle_0"
           https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v1080_crf_h265.m3u8
           #EXT-X-STREAM-INF:BANDWIDTH=3023272,AVERAGE-BANDWIDTH=928568,VIDEO-RANGE=SDR,CODECS="hev1.1.6.L93.B0,mp4a.40.2",RESOLUTION=1280x720,FRAME-RATE=25.000,SCORE=4.2,HDCP-LEVEL=NONE,CLOSED-CAPTIONS=NONE,AUDIO="audio_0",SUBTITLES="subtitle_0"
           https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v720_crf_h265.m3u8

           #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=167000,AVERAGE-BANDWIDTH=41744,VIDEO-RANGE=SDR,CODECS="avc1.4D001E",RESOLUTION=768x432,SCORE=3.1,URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v432_crf_h264_iframe_index.m3u8"
           #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=428192,AVERAGE-BANDWIDTH=97136,VIDEO-RANGE=SDR,CODECS="avc1.640028",RESOLUTION=1920x1080,SCORE=5.1,URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v1080_crf_h264_iframe_index.m3u8"
           #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=297344,AVERAGE-BANDWIDTH=66368,VIDEO-RANGE=SDR,CODECS="avc1.64001F",RESOLUTION=1280x720,SCORE=4.1,URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v720_crf_h264_iframe_index.m3u8"
           #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=115464,AVERAGE-BANDWIDTH=24848,VIDEO-RANGE=SDR,CODECS="hev1.1.6.L90.B0",RESOLUTION=768x432,SCORE=3.2,URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v432_crf_h265_iframe_index.m3u8"
           #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=276768,AVERAGE-BANDWIDTH=69248,VIDEO-RANGE=SDR,CODECS="hev1.1.6.L120.B0",RESOLUTION=1920x1080,SCORE=5.2,URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v1080_crf_h265_iframe_index.m3u8"
           #EXT-X-I-FRAME-STREAM-INF:BANDWIDTH=184504,AVERAGE-BANDWIDTH=40808,VIDEO-RANGE=SDR,CODECS="hev1.1.6.L93.B0",RESOLUTION=1280x720,SCORE=4.2,URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_v720_crf_h265_iframe_index.m3u8"

           #EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="audio_0",LANGUAGE="de",NAME="Deutsch",AUTOSELECT=YES,DEFAULT=YES,CHARACTERISTICS="tv.arte.audio.version.va-deu",URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_aud_VA-ALL_4_43449617_6233076_20260920051304.m3u8"
           #EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="audio_0",LANGUAGE="fr",NAME="Französisch",AUTOSELECT=YES,DEFAULT=NO,CHARACTERISTICS="tv.arte.audio.version.vof-fra",URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_aud_VOF-FRA_2_43449617_6212833_20260920051304.m3u8"
           #EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="audio_0",LANGUAGE="mul",NAME="Mehrsprachig",AUTOSELECT=YES,DEFAULT=NO,CHARACTERISTICS="tv.arte.audio.version.voeu-mul",URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_aud_VOEU-FRA_3_43449617_6216252_20260920051304.m3u8"
           #EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="audio_0",LANGUAGE="de",NAME="Deutsch (Klare Sprache)",AUTOSELECT=YES,DEFAULT=NO,CHARACTERISTICS="tv.arte.audio.version.va-deu,public.accessibility.enhances-speech-intelligibility",URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_aud_VA-ALL_4_43449617_6233076_20260920051304_CA.m3u8"
           #EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="audio_0",LANGUAGE="fr",NAME="Französisch (Klare Sprache)",AUTOSELECT=YES,DEFAULT=NO,CHARACTERISTICS="tv.arte.audio.version.vof-fra,public.accessibility.enhances-speech-intelligibility",URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_aud_VOF-FRA_2_43449617_6212833_20260920051304_CA.m3u8"

           #EXT-X-MEDIA:TYPE=SUBTITLES,GROUP-ID="subtitle_0",LANGUAGE="de",NAME="Deutsch (automatische Untertitel)",AUTOSELECT=YES,FORCED=YES,DEFAULT=NO,CHARACTERISTICS="tv.arte.subtitle.version.va-all,tv.arte.subtitle.mandatory",URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_st_VA-ALL_43449617_6233075_20260920051304.m3u8"
           #EXT-X-MEDIA:TYPE=SUBTITLES,GROUP-ID="subtitle_0",LANGUAGE="fr",NAME="Französisch (automatische Untertitel)",AUTOSELECT=YES,FORCED=YES,DEFAULT=NO,CHARACTERISTICS="tv.arte.subtitle.version.vf-fra,tv.arte.subtitle.mandatory",URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_st_VF-FRA_43449617_6216322_20260920051304.m3u8"
           #EXT-X-MEDIA:TYPE=SUBTITLES,GROUP-ID="subtitle_0",LANGUAGE="fr",NAME="Französisch (Für Gehörlose und Schwerhörige)",AUTOSELECT=YES,FORCED=NO,DEFAULT=NO,CHARACTERISTICS="tv.arte.subtitle.version.vf-mal,public.accessibility.transcribes-spoken-dialog,public.accessibility.describes-music-and-sound",URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_st_VF-MAL_43449617_6228241_20260920051304.m3u8"
           #EXT-X-MEDIA:TYPE=SUBTITLES,GROUP-ID="subtitle_0",LANGUAGE="en",NAME="Englisch",AUTOSELECT=YES,FORCED=NO,DEFAULT=NO,CHARACTERISTICS="tv.arte.subtitle.version.vo-ang,public.accessibility.transcribes-spoken-dialog",URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_st_VO-ANG_43449617_6252597_20260920051304.m3u8"
           #EXT-X-MEDIA:TYPE=SUBTITLES,GROUP-ID="subtitle_0",LANGUAGE="it",NAME="Italienisch",AUTOSELECT=YES,FORCED=NO,DEFAULT=NO,CHARACTERISTICS="tv.arte.subtitle.version.vo-ita,public.accessibility.transcribes-spoken-dialog",URI="https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_st_VO-ITA_43449617_6260518_20260920051304.m3u8"

           #SPRITES: https://arte-cmafhls.akamaized.net/am/cmaf/113000/113600/113627-000-A/2026092005C23F716A64A1679BF1248AF29454091A/medias/113627-000-A_SPR.vtt\
           """);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(16));
  }

  @Test
  void parseArteSubtitleMediaUrl() {
    final String playlist = FileReader.readFile("/arte/arte_video_with_subtitle.m3u8");

    List<M3U8Dto> parsed = new M3U8Parser().parse(playlist);
    M3U8Dto subtitle =
        parsed.stream()
            .filter(dto -> dto.getType() == M3U8Dto.M3U8Type.MEDIA_SUBTITLE)
            .findFirst()
            .orElseThrow();

    assertEquals(
        "https://arte-cmafhls.akamaized.net/am/cmaf/112000/112300/112308-002-A/202607241465A0A0188D2D0A531F486B20B6512278/medias/112308-002-A_st_VA-MAL_42867295_6192220.m3u8",
        subtitle.getUrl());
  }
}
