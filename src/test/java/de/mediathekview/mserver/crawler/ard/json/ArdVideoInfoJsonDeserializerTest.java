package de.mediathekview.mserver.crawler.ard.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;
import org.mockito.Mockito;

public class ArdVideoInfoJsonDeserializerTest extends WireMockTestBase {
  
  @Test
  public void deserializeTestRbb() {
    AbstractCrawler crawler = Mockito.mock(AbstractCrawler.class);
    ArdVideoInfoJsonDeserializer target = new ArdVideoInfoJsonDeserializer(crawler);
    
    JsonElement jsonElement = JsonFileReader.readJson("/rbb/rbb_film_with_subtitle.json");
    
    ArdVideoInfoDTO actual = target.deserialize(jsonElement, ArdVideoInfoDTO.class, null);
    
    assertThat(actual, notNullValue());

    assertThat(actual.getSubtitleUrl(), equalTo("http://mediathek.rbb-online.de/subtitle/217501"));
    assertThat(actual.getVideoUrls().get(Resolution.SMALL), equalTo("https://rbbmediapmdp-a.akamaihd.net/content/a0/93/a093d994-0ab0-498a-ae83-8e7647daa5db/3ccbfc08-3235-418f-9406-6c56cb89bb92_256k.mp4"));
    assertThat(actual.getVideoUrls().get(Resolution.NORMAL), equalTo("https://rbbmediapmdp-a.akamaihd.net/content/a0/93/a093d994-0ab0-498a-ae83-8e7647daa5db/3ccbfc08-3235-418f-9406-6c56cb89bb92_1024k.mp4"));
    assertThat(actual.getVideoUrls().get(Resolution.HD), equalTo("https://rbbmediapmdp-a.akamaihd.net/content/a0/93/a093d994-0ab0-498a-ae83-8e7647daa5db/3ccbfc08-3235-418f-9406-6c56cb89bb92_1800k.mp4"));
  }

  @Test
  public void deserializeTestSr() {
    AbstractCrawler crawler = Mockito.mock(AbstractCrawler.class);
    ArdVideoInfoJsonDeserializer target = new ArdVideoInfoJsonDeserializer(crawler);

    JsonElement jsonElement = JsonFileReader.readJson("/sr/sr_film_video_details1.json");

    ArdVideoInfoDTO actual = target.deserialize(jsonElement, ArdVideoInfoDTO.class, null);

    assertThat(actual, notNullValue());

    assertThat(actual.getVideoUrls().get(Resolution.SMALL), equalTo("https://srstorage01-a.akamaihd.net/Video/FS/MT/traumreise_20170926_124001_M.mp4"));
    assertThat(actual.getVideoUrls().get(Resolution.NORMAL), equalTo("https://srstorage01-a.akamaihd.net/Video/FS/MT/traumreise_20170926_124001_L.mp4"));
    assertThat(actual.getVideoUrls().get(Resolution.HD), equalTo("https://srstorage01-a.akamaihd.net/Video/FS/MT/traumreise_20170926_124001_P.mp4"));
  }
}
