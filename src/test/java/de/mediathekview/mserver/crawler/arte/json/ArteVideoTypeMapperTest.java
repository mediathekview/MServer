package de.mediathekview.mserver.crawler.arte.json;

import de.mediathekview.mlib.daten.Sender;
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
          {Sender.ARTE_DE, "VA", ArteVideoType.DEFAULT},
          {Sender.ARTE_DE, "VA-STA", ArteVideoType.DEFAULT},
          {Sender.ARTE_FR, "VF-STF", ArteVideoType.DEFAULT},
          {Sender.ARTE_DE, "VA-STMA", ArteVideoType.SUBTITLE_INCLUDED},
          {Sender.ARTE_FR, "VF-STMF", ArteVideoType.SUBTITLE_INCLUDED},
          {Sender.ARTE_DE, "VAAUD", ArteVideoType.AUDIO_DESCRIPTION},
          {Sender.ARTE_FR, "VFAUD", ArteVideoType.AUDIO_DESCRIPTION},
          {Sender.ARTE_FR, "VO-STF", ArteVideoType.ORIGINAL_WITH_SUBTITLE},
          {Sender.ARTE_DE, "VO", ArteVideoType.ORIGINAL}
        });
  }

  @Test
  public void map() {
    final Optional<ArteVideoType> actual = ArteVideoTypeMapper.map(sender, code);
    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get(), equalTo(expectedVideoType));
  }
}
