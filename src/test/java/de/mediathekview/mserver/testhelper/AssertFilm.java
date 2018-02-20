package de.mediathekview.mserver.testhelper;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;

public final class AssertFilm {
  private AssertFilm() {}

  public static void assertEquals(final Film aActualFilm,
    final Sender aExpectedSender,
    final String aExpectedTheme,
    final String aExpectedTitle,
    final LocalDateTime aExpectedTime,
    final Duration aExpectedDuration,
    final String aExpectedDescription,
    final String aWebsiteUrl,
    final GeoLocations[] aExpectedGeo,
    final String aExpectedUrlSmall,
    final String aExpectedUrlNormal,
    final String aExpectedUrlHd,
    final String aExpectedSubtitle) {
    
      assertThat(aActualFilm, notNullValue());
      assertThat(aActualFilm.getSender(), equalTo(aExpectedSender));
      assertThat(aActualFilm.getThema(), equalTo(aExpectedTheme));
      assertThat(aActualFilm.getTitel(), equalTo(aExpectedTitle));
      assertThat(aActualFilm.getTime(), equalTo(aExpectedTime));
      assertThat(aActualFilm.getDuration(), equalTo(aExpectedDuration));
      assertThat(aActualFilm.getBeschreibung(), equalTo(aExpectedDescription));
      assertThat(aActualFilm.getWebsite().get().toString(), equalTo(aWebsiteUrl));
      assertThat(aActualFilm.getGeoLocations(), Matchers.containsInAnyOrder(aExpectedGeo));

      if (aExpectedUrlSmall.isEmpty()) {
        assertThat(aActualFilm.getUrl(Resolution.SMALL), nullValue());
      } else {
        assertThat(aActualFilm.getUrl(Resolution.SMALL).toString(), equalTo(aExpectedUrlSmall));
      }
      
      assertThat(aActualFilm.getUrl(Resolution.NORMAL).toString(), equalTo(aExpectedUrlNormal));
      
      assertThat(aActualFilm.hasHD(), equalTo(!aExpectedUrlHd.isEmpty()));
      if (!aExpectedUrlHd.isEmpty()) {
        assertThat(aActualFilm.getUrl(Resolution.HD).toString(), equalTo(aExpectedUrlHd));
      }

      assertThat(aActualFilm.hasUT(), equalTo(!aExpectedSubtitle.isEmpty()));
      if(!aExpectedSubtitle.isEmpty()) {
        assertThat(aActualFilm.getSubtitles().toArray(new URL[0])[0].toString(), equalTo(aExpectedSubtitle));
      }      
  }
  
  public static void assertEquals(final Film aActualFilm,
    final Sender aExpectedSender,
    final String aExpectedTheme,
    final String aExpectedTitle,
    final LocalDateTime aExpectedTime,
    final Duration aExpectedDuration,
    final String aExpectedDescription,
    final String aWebsiteUrl,
    final GeoLocations[] aExpectedGeo,
    final String aExpectedUrlSmall,
    final String aExpectedUrlNormal,
    final String aExpectedUrlHd,
    final String aExpectedUrlSignLanguageSmall,
    final String aExpectedUrlSignLanguageNormal,
    final String aExpectedUrlSignLanguageHd,
    final String aExpectedUrlAudioDescriptionSmall,
    final String aExpectedUrlAudioDescriptionNormal,
    final String aExpectedUrlAudioDescriptionHd,
    final String aExpectedSubtitle) {
    
    assertEquals(aActualFilm, aExpectedSender, aExpectedTheme, aExpectedTitle, aExpectedTime, aExpectedDuration, aExpectedDescription, aWebsiteUrl, aExpectedGeo, aExpectedUrlSmall, aExpectedUrlNormal, aExpectedUrlHd, aExpectedSubtitle);
    assertSignLanguages(aActualFilm, aExpectedUrlSignLanguageSmall, aExpectedUrlSignLanguageNormal, aExpectedUrlSignLanguageHd);
    assertAudioDescriptions(aActualFilm, aExpectedUrlAudioDescriptionSmall, aExpectedUrlAudioDescriptionNormal, aExpectedUrlAudioDescriptionHd);
  }  
  
  private static void assertAudioDescriptions(final Film aActualFilm,
    final String aExpectedUrlAudioDescriptionSmall,
    final String aExpectedUrlAudioDescriptionNormal,
    final String aExpectedUrlAudioDescriptionHd
  ) {
    if (aExpectedUrlAudioDescriptionSmall.isEmpty()) {
      assertThat(aActualFilm.getAudioDescription(Resolution.SMALL), nullValue());
    } else {
      assertThat(aActualFilm.getAudioDescription(Resolution.SMALL).toString(), equalTo(aExpectedUrlAudioDescriptionSmall));
    }
    if (aExpectedUrlAudioDescriptionNormal.isEmpty()) {
      assertThat(aActualFilm.getAudioDescription(Resolution.NORMAL), nullValue());
    } else {
      assertThat(aActualFilm.getAudioDescription(Resolution.NORMAL).toString(), equalTo(aExpectedUrlAudioDescriptionNormal));
    }
    if (aExpectedUrlAudioDescriptionHd.isEmpty()) {
      assertThat(aActualFilm.getAudioDescription(Resolution.HD), nullValue());
    } else {
      assertThat(aActualFilm.getAudioDescription(Resolution.HD).toString(), equalTo(aExpectedUrlAudioDescriptionHd));
    }
  }

  private static void assertSignLanguages(final Film aActualFilm,
    final String aExpectedUrlSignLanguageSmall,
    final String aExpectedUrlSignLanguageNormal,
    final String aExpectedUrlSignLanguageHd
  ) {
    if (aExpectedUrlSignLanguageSmall.isEmpty()) {
      assertThat(aActualFilm.getSignLanguage(Resolution.SMALL), nullValue());
    } else {
      assertThat(aActualFilm.getSignLanguage(Resolution.SMALL).toString(), equalTo(aExpectedUrlSignLanguageSmall));
    }
    if (aExpectedUrlSignLanguageNormal.isEmpty()) {
      assertThat(aActualFilm.getSignLanguage(Resolution.NORMAL), nullValue());
    } else {
      assertThat(aActualFilm.getSignLanguage(Resolution.NORMAL).toString(), equalTo(aExpectedUrlSignLanguageNormal));
    }
    if (aExpectedUrlSignLanguageHd.isEmpty()) {
      assertThat(aActualFilm.getSignLanguage(Resolution.HD), nullValue());
    } else {
      assertThat(aActualFilm.getSignLanguage(Resolution.HD).toString(), equalTo(aExpectedUrlSignLanguageHd));
    }
  }
}
