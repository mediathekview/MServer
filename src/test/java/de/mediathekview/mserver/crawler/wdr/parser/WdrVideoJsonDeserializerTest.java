package de.mediathekview.mserver.crawler.wdr.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.wdr.WdrMediaDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class WdrVideoJsonDeserializerTest {

  private final String jsonFile;
  private final Optional<String> expectedSubtitle;
  private final String expectedM3U8Url;
  private final Optional<String> expectedSignLanguageUrl;
  private final Optional<String> expectedAudioDescriptionUrl;

  public WdrVideoJsonDeserializerTest(
      final String aJsonFile,
      final Optional<String> aExpectedSubtitle,
      final String aExpectedM3U8Url,
      final Optional<String> aExpectedSignLanguageUrl,
      final Optional<String> aExpectedAudioDescriptionUrl) {

    jsonFile = aJsonFile;
    expectedSubtitle = aExpectedSubtitle;
    expectedM3U8Url = aExpectedM3U8Url;
    expectedSignLanguageUrl = aExpectedSignLanguageUrl;
    expectedAudioDescriptionUrl = aExpectedAudioDescriptionUrl;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/wdr/wdr_video1.json",
            Optional.empty(),
            "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/master.m3u8",
            Optional.empty(),
            Optional.empty()
          },
          {
            "/wdr/wdr_video2.json",
            Optional.of(
                "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/140/1407842/1407842_16348809.xml"),
            "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/master.m3u8",
            Optional.empty(),
            Optional.empty()
          },
          {
            "/wdr/wdr_video3.json",
            Optional.of(
                "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/52/528067/528067_5542163.xml"),
            "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/52/528067/,528067_5540994,528067_5540993,528067_5540992,528067_5540996,528067_5540995,.mp4.csmil/master.m3u8",
            Optional.of(
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/52/528067/,528067_5542160,528067_5542162,528067_5542161,.mp4.csmil/master.m3u8"),
            Optional.empty()
          },
          {
            "/wdr/wdr_video_v1_1.json",
            Optional.empty(),
            "http://ondemand-ww.wdr.de/medp/fsk0/47/476693/476693_12040646.mp4",
            Optional.empty(),
            Optional.empty()
          },
          {
            "/wdr/wdr_video_with_ad_dgs.json",
            Optional.of(
                "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/162/1629126/1629126_18799781.xml"),
            "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/162/1629126/,1629126_18799707,1629126_18799719,1629126_18799712,1629126_18799713,1629126_18799717,1629126_18799722,.mp4.csmil/master.m3u8",
            Optional.of(
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/162/1629126/,1629126_18799706,1629126_18799718,1629126_18799710,1629126_18799711,1629126_18799716,1629126_18799721,.mp4.csmil/master.m3u8"),
            Optional.of(
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/162/1629126/,1629126_18799705,1629126_18799714,1629126_18799708,1629126_18799709,1629126_18799715,1629126_18799720,.mp4.csmil/master.m3u8")
          }
        });
  }

  @Test
  public void test() {
    final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

    final WdrVideoJsonDeserializer target = new WdrVideoJsonDeserializer("http:");
    final Optional<WdrMediaDto> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual, notNullValue());
    assertThat(actual.isPresent(), equalTo(true));

    final WdrMediaDto actualDto = actual.get();
    assertThat(actualDto.getUrl(), equalTo(expectedM3U8Url));
    assertThat(actualDto.getSubtitle(), equalTo(expectedSubtitle));
    assertThat(actualDto.getSignLanguageUrl(), equalTo(expectedSignLanguageUrl));
    assertThat(actualDto.getAudioDescriptionUrl(), equalTo(expectedAudioDescriptionUrl));
  }
}
