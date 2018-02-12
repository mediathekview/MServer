package de.mediathekview.mserver.crawler.wdr.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.wdr.WdrMediaDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class WdrVideoJsonDeserializerTest {
  
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      {
        "/wdr/wdr_video1.json",
        Optional.empty(),
        "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/master.m3u8"
      },
      {
        "/wdr/wdr_video2.json",
        Optional.of("http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/140/1407842/1407842_16348809.xml"),
        "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/master.m3u8"
      },
      {
        "/wdr/wdr_video3.json",
        Optional.of("http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/52/528067/528067_5542163.xml"),
        "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/52/528067/,528067_5540994,528067_5540993,528067_5540992,528067_5540996,528067_5540995,.mp4.csmil/master.m3u8"
      }
    });
  }

  private final String jsonFile;
  private final Optional<String> expectedSubtitle;
  private final String expectedM3U8Url;
  
  public WdrVideoJsonDeserializerTest(final String aJsonFile, 
          final Optional<String> aExpectedSubtitle, 
          final String aExpectedM3U8Url) {
    
    jsonFile = aJsonFile;
    expectedSubtitle = aExpectedSubtitle;
    expectedM3U8Url = aExpectedM3U8Url;
  }
  
  @Test
  public void test() {
    final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);
    
    WdrVideoJsonDeserializer target = new WdrVideoJsonDeserializer("http:");
    Optional<WdrMediaDTO> actual = target.deserialize(jsonElement, null, null);
    
    assertThat(actual, notNullValue());
    assertThat(actual.isPresent(), equalTo(true));
    
    WdrMediaDTO actualDto = actual.get();
    assertThat(actualDto.getUrl(), equalTo(expectedM3U8Url));
    assertThat(actualDto.getSubtitle(), equalTo(expectedSubtitle));
  }
}