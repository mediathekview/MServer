package de.mediathekview.mserver.crawler.basic;

import de.mediathekview.mserver.crawler.srf.SrfConstants;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class M3U8ParserTest {

  @Test
  public void parseTestSrf() {

    M3U8Dto[] expected = new M3U8Dto[]{
      createDto("https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_av.m3u8?start=0.0&end=3305.1",
      new String[]{SrfConstants.M3U8_PROGRAM_ID, SrfConstants.M3U8_BANDWIDTH, SrfConstants.M3U8_CODECS, SrfConstants.M3U8_CLOSED_CAPTIONS},
      new String[]{"1", "118000", "\"avc1.66.30, mp4a.40.2\"", "NONE"}),
      createDto("https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_1_av.m3u8?start=0.0&end=3305.1",
      new String[]{SrfConstants.M3U8_PROGRAM_ID, SrfConstants.M3U8_BANDWIDTH, SrfConstants.M3U8_CODECS, SrfConstants.M3U8_CLOSED_CAPTIONS},
      new String[]{"1", "739000", "\"avc1.77.30, mp4a.40.2\"", "NONE"}),
      createDto("https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_2_av.m3u8?start=0.0&end=3305.1",
      new String[]{SrfConstants.M3U8_PROGRAM_ID, SrfConstants.M3U8_BANDWIDTH, SrfConstants.M3U8_CODECS, SrfConstants.M3U8_CLOSED_CAPTIONS},
      new String[]{"1", "1395000", "\"avc1.77.30, mp4a.40.2\"", "NONE"}),
      createDto("https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_a.m3u8?start=0.0&end=3305.1",
      new String[]{SrfConstants.M3U8_PROGRAM_ID, SrfConstants.M3U8_BANDWIDTH, SrfConstants.M3U8_CODECS, SrfConstants.M3U8_CLOSED_CAPTIONS},
      new String[]{"1", "23000", "\"mp4a.40.2\"", "NONE"}),
    };

    M3U8Parser target = new M3U8Parser();
    List<M3U8Dto> actual = target.parse("#EXTM3U\n"
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

  private static M3U8Dto createDto(String aUrl, String[] keys, String[] values) {

    M3U8Dto dto = new M3U8Dto(aUrl);
    for (int i = 0; i < keys.length; i++) {
      dto.addMeta(keys[i], values[i]);
    }

    return dto;
  }
}
