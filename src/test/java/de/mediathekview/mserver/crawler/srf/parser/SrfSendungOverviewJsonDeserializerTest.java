package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class SrfSendungOverviewJsonDeserializerTest {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      { "/srf/srf_sendung_overview_page1.json", 
        Optional.of("https://www.srf.ch/play/v2/tv/show/6fd27ab0-d10f-450f-aaa9-836f1cac97bd/latestEpisodes/tillMonth/12-2017?nextPageHash=09e12b6f403c2da8bfde15a1c99070d4f1c58eef3c29b0ea2f598fc7a2dcbbae313cfdfbbe05cc5893d619409e53baba5c75cbee20dad0631f4f560b6e4084a21f6d2a8264018f47a564cdea2a9099a708af136b02c7288f22c1c24f3e62269a839ac673cb33b4a8"),
        new String[] {
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/e3720c24-58b9-4056-b098-17acbf97f9a6.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/c19eeb6c-c168-40a3-a205-4298a18576aa.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/d38b05d8-20f8-409a-b1de-b8a3b4be021e.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/f58c940d-89bc-4f7d-9fb4-a4634fac1b32.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/18a0ec86-4829-44e0-8261-d647d7907c7c.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/001f4ea3-72a0-40d1-ad6e-1e584f0b9222.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/d588365b-b2e8-47ff-9e23-1ae2510c6f97.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/63ff41bb-5ab3-4b3a-bc02-5bea921447dc.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/de2b5dc4-e5b5-4ba0-a05d-a2ce4ce1bb46.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/99dbaab8-36e3-4f12-bf5f-34f197ed0777.json"
        }      
      },
      { "/srf/srf_sendung_overview_page_last.json", 
        Optional.empty(), 
        new String[] { 
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/69cf918f-185a-4806-92f6-031e7f09844d.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/4eb1dbdf-dab8-4690-ba93-fdbafebbd5de.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/af4c9505-c265-49f6-86c8-67fe90dd0a2f.json",
          "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/22b9dd2c-d1fd-463b-91de-d804eda74889.json"
        } 
      },
    });
  }
  
  private final String jsonFile;
  private final Optional<String> expectedNextPageId;
  private final String[] expectedUrls;
  private final SrfSendungOverviewJsonDeserializer target;
  
  public SrfSendungOverviewJsonDeserializerTest(String aJsonFile, Optional<String> aExpectedNextPageId, String[] aExpectedUrls) {
    jsonFile = aJsonFile;
    expectedNextPageId = aExpectedNextPageId;
    expectedUrls = aExpectedUrls;
    
    target = new SrfSendungOverviewJsonDeserializer();
  }
  
  @Test
  public void test() {
    JsonElement jsonElement = JsonFileReader.readJson(jsonFile);
    
    SrfSendungOverviewDTO actual = target.deserialize(jsonElement, SrfSendungOverviewDTO.class, null);
    
    assertThat(actual, notNullValue());
    assertThat(actual.getNextPageId(), equalTo(expectedNextPageId));
    assertThat(actual.getUrls(), Matchers.containsInAnyOrder(expectedUrls));
  }
}
