package de.mediathekview.mserver.crawler.basic;

import de.mediathekview.mlib.daten.Resolution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class M3U8DtoGetResolutionTest {
  private final Optional<Resolution> expectedResolution;
  private final M3U8Dto target;

  public M3U8DtoGetResolutionTest(
      final String aUrl,
      final String aCodec,
      final String aResolution,
      final Optional<Resolution> aExpectedResolution) {

    target = new M3U8Dto(aUrl);
    target.addMeta(M3U8Constants.M3U8_CODECS, aCodec);
    target.addMeta(M3U8Constants.M3U8_RESOLUTION, aResolution);

    expectedResolution = aExpectedResolution;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"noavcl.url", "other codec", "1280x720", Optional.empty()},
          {"small.url", "avc1", "480x320", Optional.of(Resolution.SMALL)},
          {"small.url", "avc1", "512x288", Optional.of(Resolution.SMALL)},
          {"small.url", "avc1", "640x360", Optional.of(Resolution.SMALL)},
          {"small.url", "avc1", "720x544", Optional.of(Resolution.SMALL)},
          {"normal.url", "avc1", "960x544", Optional.of(Resolution.NORMAL)},
          {"normal.url", "avc1", "1280x720", Optional.of(Resolution.NORMAL)},
          {"hd.url", "avc1", "1920x1080", Optional.of(Resolution.HD)},
          {"uhd.url", "avc1", "3840x2160", Optional.of(Resolution.UHD)},
          {"unknown.url", "avc1", "1280x719", Optional.empty()}
        });
  }

  @Test
  public void getResolutionTest() {
    final Optional<Resolution> actual = target.getResolution();

    assertThat(actual, equalTo(expectedResolution));
  }
}
