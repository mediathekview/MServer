package de.mediathekview.mserver.crawler.arte.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.mediathekview.mserver.crawler.arte.json.ArteVideoInfoDto;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoLinkDto;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

@RunWith(Parameterized.class)
public class ArteVideoLinkTaskTest extends WireMockTestBase {
  private final String filmUrl;
  private final ArteVideoLinkDto[] arteVideoLinkDtoData;
  
  public ArteVideoLinkTaskTest(String filmUrl, ArteVideoLinkDto[] arteVideoLinkDtoData) {
    this.filmUrl=filmUrl;
    this.arteVideoLinkDtoData = arteVideoLinkDtoData;
  }
  
  
  @Test
  public void test() {
    setupSuccessfulJsonResponse(this.filmUrl, this.filmUrl);
    Set<ArteVideoInfoDto> resultSet = executeTask(buildWireMockUrl(this.filmUrl));
    
    assertEquals(resultSet.size(), 1);
    ArteVideoInfoDto result = resultSet.stream().findAny().get();
    assertEquals(result.getVideoLinks().size(), this.arteVideoLinkDtoData.length);
    //
    for (int i = 0; i < this.arteVideoLinkDtoData.length; i++) {
      assertEntry(result.getVideoLinks().get(i), this.arteVideoLinkDtoData[i]);
    }
  }
  
  private void assertEntry(ArteVideoLinkDto act, ArteVideoLinkDto expected) {
    assertEquals(act.getAudioCode(), expected.getAudioCode());
    assertEquals(act.getAudioLabel(), expected.getAudioLabel());
    assertEquals(act.getAudioShortLabel(), expected.getAudioShortLabel());
    assertEquals(act.getHeight(), expected.getHeight());
    assertEquals(act.getProgramId(), expected.getProgramId());
    assertEquals(act.getQuality(), expected.getQuality());
    assertEquals(act.getWidth(), expected.getWidth());
    assertEquals(act.getUrl(), expected.getUrl());
  }
  
  
  private Set<ArteVideoInfoDto> executeTask(String... requestUrl) {
    final Queue<ArteVideoInfoDto> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      ArteVideoInfoDto avid = new ArteVideoInfoDto(Optional.of(url),Optional.of(""),Optional.of(""),Optional.of(""));
      avid.setUrl(url);
      input.add(avid);
    }
    return new ArteVideoLinkTask(ArteTaskTestBase.createCrawler(), input).invoke();
  }
  
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/arte/arte_video_1.json", // url
            new ArteVideoLinkDto[] {
                new ArteVideoLinkDto(
                    Optional.of("121542-000-A"),
                    Optional.of("https://arteptweb-a.akamaihd.net/am/ptweb/121000/121500/121542-000-A_SQ_0_VOF_09098757_MP4-2200_AMM-PTWEB-20240728150944_2MuElnHI7x.mp4"),
                    Optional.of("SQ"),
                    Optional.of("2"),
                    Optional.of("VOF"),
                    Optional.of("Französisch"),
                    Optional.of("FR"),
                    Optional.of("1280"),
                    Optional.of("720")
                ),
                new ArteVideoLinkDto(
                    Optional.of("121542-000-A"),
                    Optional.of("https://arteptweb-a.akamaihd.net/am/ptweb/121000/121500/121542-000-A_HQ_0_VOF_09098755_MP4-800_AMM-PTWEB-20240728150944_2MuEjnHI7x.mp4"),
                    Optional.of("HQ"),
                    Optional.of("2"),
                    Optional.of("VOF"),
                    Optional.of("Französisch"),
                    Optional.of("FR"),
                    Optional.of("640"),
                    Optional.of("360")
                ),
                new ArteVideoLinkDto(
                    Optional.of("121542-000-A"),
                    Optional.of("https://arteptweb-a.akamaihd.net/am/ptweb/121000/121500/121542-000-A_SQ_0_VA_09098752_MP4-2200_AMM-PTWEB-20240728151022_2MuEgnHI7x.mp4"),
                    Optional.of("SQ"),
                    Optional.of("1"),
                    Optional.of("VA"),
                    Optional.of("Deutsch"),
                    Optional.of("DE"),
                    Optional.of("1280"),
                    Optional.of("720")
                    
                ),
                new ArteVideoLinkDto( //3
                    Optional.of("121542-000-A"),
                    Optional.of("https://arteptweb-a.akamaihd.net/am/ptweb/121000/121500/121542-000-A_EQ_0_VOF_09098754_MP4-1500_AMM-PTWEB-20240728150944_2MuEinHI7x.mp4"),
                    Optional.of("EQ"),
                    Optional.of("2"),
                    Optional.of("VOF"),
                    Optional.of("Französisch"),
                    Optional.of("FR"),
                    Optional.of("720"),
                    Optional.of("406")
                ),
                new ArteVideoLinkDto(
                    Optional.of("121542-000-A"),
                    Optional.of("https://arteptweb-a.akamaihd.net/am/ptweb/121000/121500/121542-000-A_EQ_0_VA_09098749_MP4-1500_AMM-PTWEB-20240728151022_2MuEdnHI7w.mp4"),
                    Optional.of("EQ"),
                    Optional.of("1"),
                    Optional.of("VA"),
                    Optional.of("Deutsch"),
                    Optional.of("DE"),
                    Optional.of("720"),
                    Optional.of("406")
                ),
                new ArteVideoLinkDto(
                    Optional.of("121542-000-A"),
                    Optional.of("https://arteptweb-a.akamaihd.net/am/ptweb/121000/121500/121542-000-A_HQ_0_VA_09098750_MP4-800_AMM-PTWEB-20240728151022_2MuEenHI7w.mp4"),
                    Optional.of("HQ"),
                    Optional.of("1"),
                    Optional.of("VA"),
                    Optional.of("Deutsch"),
                    Optional.of("DE"),
                    Optional.of("640"),
                    Optional.of("360")
                    
                ),
            }
          }
        });
  }
  
  
}
