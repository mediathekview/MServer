package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.crawler.arte.tasks.ArteVideoDetailDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;

@RunWith(Parameterized.class)
public class ArteVideoDetailsDeserializerTest {

  private final String jsonFile;
  private final Sender sender;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedUrlWithSubtitleSmall;
  private final String expectedUrlWithSubtitleNormal;
  private final String expectedUrlWithSubtitleHd;
  private final String expectedUrlOriginalWithSubtitleSmall;
  private final String expectedUrlOriginalWithSubtitleNormal;
  private final String expectedUrlOriginalWithSubtitleHd;
  private final String expectedUrlAudioDescSmall;
  private final String expectedUrlAudioDescNormal;
  private final String expectedUrlAudioDescHd;
  private final String expectedUrlOriginalSmall;
  private final String expectedUrlOriginalNormal;
  private final String expectedUrlOriginalHd;

  public ArteVideoDetailsDeserializerTest(
      final String jsonFile,
      final Sender sender,
      final String expectedUrlSmall,
      final String expectedUrlNormal,
      final String expectedUrlHd,
      final String expectedUrlWithSubtitleSmall,
      final String expectedUrlWithSubtitleNormal,
      final String expectedUrlWithSubtitleHd,
      final String expectedUrlOriginalWithSubtitleSmall,
      final String expectedUrlOriginalWithSubtitleNormal,
      final String expectedUrlOriginalWithSubtitleHd,
      final String expectedUrlAudioDescSmall,
      final String expectedUrlAudioDescNormal,
      final String expectedUrlAudioDescHd,
      final String expectedUrlOriginalSmall,
      final String expectedUrlOriginalNormal,
      final String expectedUrlOriginalHd) {

    this.jsonFile = jsonFile;
    this.sender = sender;
    this.expectedUrlSmall = expectedUrlSmall;
    this.expectedUrlNormal = expectedUrlNormal;
    this.expectedUrlHd = expectedUrlHd;
    this.expectedUrlWithSubtitleSmall = expectedUrlWithSubtitleSmall;
    this.expectedUrlWithSubtitleNormal = expectedUrlWithSubtitleNormal;
    this.expectedUrlWithSubtitleHd = expectedUrlWithSubtitleHd;
    this.expectedUrlOriginalWithSubtitleSmall = expectedUrlOriginalWithSubtitleSmall;
    this.expectedUrlOriginalWithSubtitleNormal = expectedUrlOriginalWithSubtitleNormal;
    this.expectedUrlOriginalWithSubtitleHd = expectedUrlOriginalWithSubtitleHd;
    this.expectedUrlAudioDescSmall = expectedUrlAudioDescSmall;
    this.expectedUrlAudioDescNormal = expectedUrlAudioDescNormal;
    this.expectedUrlAudioDescHd = expectedUrlAudioDescHd;
    this.expectedUrlOriginalSmall = expectedUrlOriginalSmall;
    this.expectedUrlOriginalNormal = expectedUrlOriginalNormal;
    this.expectedUrlOriginalHd = expectedUrlOriginalHd;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/arte/arte_film_video1.json",
            Sender.ARTE_DE,
            "https://arteptweb-a.akamaihd.net/am/ptweb/084000/084700/084733-002-A_HQ_0_VA-STA_04065289_MP4-800_AMM-PTWEB_1588D7RrM3.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/084000/084700/084733-002-A_EQ_0_VA-STA_04065295_MP4-1500_AMM-PTWEB_1586Q7RqlB.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/084000/084700/084733-002-A_SQ_0_VA-STA_04065288_MP4-2200_AMM-PTWEB_1588w7RraU.mp4",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
          },
          {
            "/arte/arte_film_video_audio_desc_de.json",
            Sender.ARTE_DE,
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_HQ_0_VA-STA_04530699_MP4-800_AMM-PTWEB_1D78dzrOJf.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_EQ_0_VA-STA_04530700_MP4-1500_AMM-PTWEB_1D6xwzrJ81.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_SQ_0_VA-STA_04530698_MP4-2200_AMM-PTWEB_1D78KzrOD5.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_HQ_0_VA-STMA_04530703_MP4-800_AMM-PTWEB_1D8uFzvxvH.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_EQ_0_VA-STMA_04530704_MP4-1500_AMM-PTWEB_1D6y6zrJEN.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_SQ_0_VA-STMA_04530702_MP4-2200_AMM-PTWEB_1D79WzrOmR.mp4",
            "",
            "",
            "",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_HQ_0_VAAUD_04530707_MP4-800_AMM-PTWEB_1D78ezrOJf.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_EQ_0_VAAUD_04530708_MP4-1500_AMM-PTWEB_1D6yCzrJI1.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_SQ_0_VAAUD_04530706_MP4-2200_AMM-PTWEB_1D78azrOGD.mp4",
            "",
            "",
            "",
          },
          {
            "/arte/arte_film_video_audio_desc_fr.json",
            Sender.ARTE_FR,
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_HQ_0_VF-STF_04530711_MP4-800_AMM-PTWEB_1D79EzrOWH.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_EQ_0_VF-STF_04530712_MP4-1500_AMM-PTWEB_1D6y7zrJEN.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_SQ_0_VF-STF_04530710_MP4-2200_AMM-PTWEB_1D8vLzvyEd.mp4",
            "",
            "",
            "",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_HQ_0_VO-STF_04530715_MP4-800_AMM-PTWEB_1D79IzrOct.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_EQ_0_VO-STF_04530716_MP4-1500_AMM-PTWEB_1D6yDzrJI1.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_SQ_0_VO-STF_04530714_MP4-2200_AMM-PTWEB_1D79ZzrOpg.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_HQ_0_VFAUD_04530719_MP4-800_AMM-PTWEB_1D79XzrOmR.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_EQ_0_VFAUD_04530720_MP4-1500_AMM-PTWEB_1D6yMzrJRJ.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_SQ_0_VFAUD_04530718_MP4-2200_AMM-PTWEB_1D79czrOsi.mp4",
            "",
            "",
            "",
          },
          {
            "/arte/arte_film_video_subtitle_and_original.json",
            Sender.ARTE_FR,
            "https://arteptweb-a.akamaihd.net/am/ptweb/049000/049400/049448-001-A_HQ_0_VF-STF_06326517_MP4-800_AMM-PTWEB_1g7Uu1JkjsH.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/049000/049400/049448-001-A_EQ_0_VF-STF_06326516_MP4-1500_AMM-PTWEB_1g7Ui1Jkjs8.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/049000/049400/049448-001-A_SQ_0_VF-STF_06326519_MP4-2200_AMM-PTWEB_1g7Iq1JkgcY.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/049000/049400/049448-001-A_HQ_0_VF-STMF_06326525_MP4-800_AMM-PTWEB_1g7Ul1Jkjs9.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/049000/049400/049448-001-A_EQ_0_VF-STMF_06326524_MP4-1500_AMM-PTWEB_1g7Uk1Jkjs9.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/049000/049400/049448-001-A_SQ_0_VF-STMF_06326527_MP4-2200_AMM-PTWEB_1g7J11Jkgfk.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/049000/049400/049448-001-A_HQ_0_VO-STF_06326521_MP4-800_AMM-PTWEB_1g7Uw1JkjtV.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/049000/049400/049448-001-A_EQ_0_VO-STF_06326520_MP4-1500_AMM-PTWEB_1g7Uv1JkjsH.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/049000/049400/049448-001-A_SQ_0_VO-STF_06326523_MP4-2200_AMM-PTWEB_1g7IZ1JkfTX.mp4",
            "",
            "",
            "",
            "",
            "",
            "",
          },
          {
            "/arte/arte_film_video_it.json",
            Sender.ARTE_IT,
            "",
            "",
            "",
            "",
            "",
            "",
            "https://arteptweb-a.akamaihd.net/am/ptweb/083000/083400/083402-001-A_HQ_0_VOF-STE%5BITA%5D_04304495_MP4-800_AMM-PTWEB_19LwgYzgHA.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/083000/083400/083402-001-A_EQ_0_VOF-STE%5BITA%5D_04304493_MP4-1500_AMM-PTWEB_19LweYzgHA.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/083000/083400/083402-001-A_SQ_0_VOF-STE%5BITA%5D_04304494_MP4-2200_AMM-PTWEB_19LwfYzgHA.mp4",
            "",
            "",
            "",
            "",
            "",
            ""
          },
          {
            "/arte/arte_film_video_original.json",
            Sender.ARTE_DE,
            "https://arteptweb-a.akamaihd.net/am/ptweb/103000/103400/103432-001-A_HQ_0_VA_06305095_MP4-800_AMM-PTWEB_1fpVJ1JHIFT.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/103000/103400/103432-001-A_EQ_0_VA_06305094_MP4-1500_AMM-PTWEB_1fpVc1JHIFW.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/103000/103400/103432-001-A_SQ_0_VA_06305097_MP4-2200_AMM-PTWEB_1fpPg1JHHO1.mp4",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "https://arteptweb-a.akamaihd.net/am/ptweb/103000/103400/103432-001-A_HQ_0_VO_06305090_MP4-800_AMM-PTWEB_1fpVH1JHIFT.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/103000/103400/103432-001-A_EQ_0_VO_06305089_MP4-1500_AMM-PTWEB_1fpVb1JHIFT.mp4",
            "https://arteptweb-a.akamaihd.net/am/ptweb/103000/103400/103432-001-A_SQ_0_VO_06305092_MP4-2200_AMM-PTWEB_1fpQF1JHHU1.mp4"
          }
        });
  }

  @Test
  public void testDeserialize() {
    final JsonElement jsonObject = JsonFileReader.readJson(jsonFile);

    final ArteVideoDetailsDeserializer target = new ArteVideoDetailsDeserializer(sender);
    final Optional<ArteVideoDetailDTO> actual = target.deserialize(jsonObject, null, null);

    assertThat(actual.isPresent(), equalTo(true));
    final ArteVideoDetailDTO actualDetail = actual.get();

    assertFilmUrl(actualDetail.get(Resolution.SMALL), expectedUrlSmall);
    assertFilmUrl(actualDetail.get(Resolution.NORMAL), expectedUrlNormal);
    assertFilmUrl(actualDetail.get(Resolution.HD), expectedUrlHd);
    assertFilmUrl(actualDetail.getSubtitle(Resolution.SMALL), expectedUrlWithSubtitleSmall);
    assertFilmUrl(actualDetail.getSubtitle(Resolution.NORMAL), expectedUrlWithSubtitleNormal);
    assertFilmUrl(actualDetail.getSubtitle(Resolution.HD), expectedUrlWithSubtitleHd);
    assertFilmUrl(
        actualDetail.getOriginalWithSubtitle(Resolution.SMALL),
        expectedUrlOriginalWithSubtitleSmall);
    assertFilmUrl(
        actualDetail.getOriginalWithSubtitle(Resolution.NORMAL),
        expectedUrlOriginalWithSubtitleNormal);
    assertFilmUrl(
        actualDetail.getOriginalWithSubtitle(Resolution.HD), expectedUrlOriginalWithSubtitleHd);
    assertFilmUrl(actualDetail.getAudioDescription(Resolution.SMALL), expectedUrlAudioDescSmall);
    assertFilmUrl(actualDetail.getAudioDescription(Resolution.NORMAL), expectedUrlAudioDescNormal);
    assertFilmUrl(actualDetail.getAudioDescription(Resolution.HD), expectedUrlAudioDescHd);
    assertFilmUrl(actualDetail.getOriginal(Resolution.SMALL), expectedUrlOriginalSmall);
    assertFilmUrl(actualDetail.getOriginal(Resolution.NORMAL), expectedUrlOriginalNormal);
    assertFilmUrl(actualDetail.getOriginal(Resolution.HD), expectedUrlOriginalHd);
  }

  private void assertFilmUrl(final String actualUrl, final String expectedUrl) {
    if (expectedUrl.isEmpty()) {
      assertThat(actualUrl, nullValue());
    } else {
      assertThat(actualUrl, equalTo(expectedUrl));
    }
  }
}
