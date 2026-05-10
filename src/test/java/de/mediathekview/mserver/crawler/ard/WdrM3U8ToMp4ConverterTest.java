package de.mediathekview.mserver.crawler.ard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import de.mediathekview.mserver.daten.Resolution;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WdrM3U8ToMp4ConverterTest {

  @Test
  void convertValidUrl() {
    final String input =
        "https://wdrvod-rwrtr.akamaized.net/i/,/media/p/public/de/2026/04/19/4818d599-7da2-44cb-ba95-b791ae8d3735/4818d599-7da2-44cb-ba95-b791ae8d3735_AVC-,270,360,540,720,1080,.mp4.csmil/index-f4-v1-a1.m3u8";

    WdrM3U8ToMp4Converter target = new WdrM3U8ToMp4Converter();
    final Map<Resolution, String> actual = target.convert(input);
    assertThat(actual.size(), equalTo(4));
    assertThat(
        actual.get(Resolution.VERY_SMALL),
        equalTo(
            "https://wdr-progressive.ard-mcdn.de/media/p/public/de/2026/04/19/4818d599-7da2-44cb-ba95-b791ae8d3735/4818d599-7da2-44cb-ba95-b791ae8d3735_AVC-360.mp4"));
    assertThat(
        actual.get(Resolution.SMALL),
        equalTo(
            "https://wdr-progressive.ard-mcdn.de/media/p/public/de/2026/04/19/4818d599-7da2-44cb-ba95-b791ae8d3735/4818d599-7da2-44cb-ba95-b791ae8d3735_AVC-540.mp4"));
    assertThat(
        actual.get(Resolution.NORMAL),
        equalTo(
            "https://wdr-progressive.ard-mcdn.de/media/p/public/de/2026/04/19/4818d599-7da2-44cb-ba95-b791ae8d3735/4818d599-7da2-44cb-ba95-b791ae8d3735_AVC-720.mp4"));
    assertThat(
        actual.get(Resolution.HD),
        equalTo(
            "https://wdr-progressive.ard-mcdn.de/media/p/public/de/2026/04/19/4818d599-7da2-44cb-ba95-b791ae8d3735/4818d599-7da2-44cb-ba95-b791ae8d3735_AVC-1080.mp4"));
  }

  @Test
  void convertInvalidUrl() {
    final String input = "https://some-other-url.net/i/,/uups.m3u8";

    WdrM3U8ToMp4Converter target = new WdrM3U8ToMp4Converter();
    final Map<Resolution, String> actual = target.convert(input);
    assertThat(actual.size(), equalTo(0));
  }
}
