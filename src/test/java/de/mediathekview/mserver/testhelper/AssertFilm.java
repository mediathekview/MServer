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
}
