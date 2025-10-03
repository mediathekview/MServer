package de.mediathekview.mserver.crawler.arte.json;

import de.mediathekview.mserver.daten.Sender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class ArteVideoTypeMapperTest {

  private final Sender sender;
  private final String code;
  private final ArteVideoType expectedVideoType;

  public ArteVideoTypeMapperTest(Sender sender, String code, ArteVideoType expectedVideoType) {
    this.sender = sender;
    this.code = code;
    this.expectedVideoType = expectedVideoType;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {Sender.ARTE_DE, "VO", ArteVideoType.ORIGINAL},
          {Sender.ARTE_DE, "VO-STA", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_DE, "VOA", ArteVideoType.DEFAULT},
          {Sender.ARTE_DE, "VOA-STA", ArteVideoType.DEFAULT},
          {Sender.ARTE_DE, "VOA-STF", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_DE, "VOA-STMA", ArteVideoType.SUBTITLE_INCLUDED},
          {Sender.ARTE_DE, "VOF-STA", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_DE, "VA", ArteVideoType.DEFAULT},
          {Sender.ARTE_DE, "VA-STA", ArteVideoType.DEFAULT},
          {Sender.ARTE_DE, "VA-STMA", ArteVideoType.SUBTITLE_INCLUDED},
          {Sender.ARTE_DE, "VAAUD", ArteVideoType.AUDIO_DESCRIPTION},
          {Sender.ARTE_EN, "VE[ANG]", ArteVideoType.DEFAULT},
          {Sender.ARTE_EN, "VE[ANG]-STE[ANG]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_EN, "VOA-STE[ANG]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_EN, "VOEU-STE[ANG]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_EN, "VOF-STE[ANG]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_FR, "VF", ArteVideoType.DEFAULT},
          {Sender.ARTE_FR, "VF-STF", ArteVideoType.DEFAULT},
          {Sender.ARTE_FR, "VF-STMF", ArteVideoType.SUBTITLE_INCLUDED},
          {Sender.ARTE_FR, "VFAUD", ArteVideoType.AUDIO_DESCRIPTION},
          {Sender.ARTE_FR, "VO-STF", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_FR, "VOF", ArteVideoType.DEFAULT},
          {Sender.ARTE_FR, "VOF-STF", ArteVideoType.DEFAULT},
          {Sender.ARTE_FR, "VOF-STMF", ArteVideoType.SUBTITLE_INCLUDED},
          {Sender.ARTE_ES, "VE[ESP]-STE[ESP]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_ES, "VOA-STE[ESP]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_ES, "VOEU-STE[ESP]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_ES, "VOF-STE[ESP]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_IT, "VE[ITA]-STE[ITA]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_IT, "VOA-STE[ITA]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_IT, "VOEU-STE[ITA]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_IT, "VOF-STE[ITA]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_PL, "VE[POL]-STE[POL]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_PL, "VOA-STE[POL]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_PL, "VOEU-STE[POL]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_PL, "VOF-STE[POL]", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
        });
  }

  @Test
  public void map() {
    final Optional<ArteVideoType> actual = ArteVideoTypeMapper.map(sender, code);
    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get(), equalTo(expectedVideoType));
  }
}
