package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.mockito.Mockito;

public class ArdVideoInfoJsonDeserializerTest extends WireMockTestBase {
  
  @Test
  public void deserializeTestSR() {
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
