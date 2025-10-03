package de.mediathekview.mserver.testhelper;

import de.mediathekview.mserver.daten.*;
import org.hamcrest.Matchers;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public final class AssertFilm {

  private AssertFilm() {}

  public static void assertEquals(
      final Film aActualFilm,
      final Sender aExpectedSender,
      final String aExpectedTheme,
      final String aExpectedTitle,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aWebsiteUrl) {

    assertThat(aActualFilm, notNullValue());
    assertThat(aActualFilm.getSender(), equalTo(aExpectedSender));
    assertThat(aActualFilm.getThema(), equalTo(aExpectedTheme));
    assertThat(aActualFilm.getTitel(), equalTo(aExpectedTitle));
    assertThat(aActualFilm.getTime(), equalTo(aExpectedTime));
    assertThat(aActualFilm.getDuration(), equalTo(aExpectedDuration));
    assertThat(aActualFilm.getBeschreibung(), equalTo(aExpectedDescription));
    if (!aWebsiteUrl.isEmpty()) {
      assertThat(aActualFilm.getWebsite().get().toString(), equalTo(aWebsiteUrl));
    } else {
      assertThat(aActualFilm.getWebsite().isPresent(), equalTo(false));
    }
  }

  public static void assertEquals(
      final Film aActualFilm,
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

    assertEquals(
        aActualFilm,
        aExpectedSender,
        aExpectedTheme,
        aExpectedTitle,
        aExpectedTime,
        aExpectedDuration,
        aExpectedDescription,
        aWebsiteUrl);

    assertThat(aActualFilm.getGeoLocations(), Matchers.containsInAnyOrder(aExpectedGeo));

    assertUrl(aExpectedUrlSmall, aActualFilm.getUrl(Resolution.SMALL));
    assertUrl(aExpectedUrlNormal, aActualFilm.getUrl(Resolution.NORMAL));
    assertUrl(aExpectedUrlHd, aActualFilm.getUrl(Resolution.HD));

    assertThat(aActualFilm.hasUT(), equalTo(!aExpectedSubtitle.isEmpty()));
    if (!aExpectedSubtitle.isEmpty()) {
      assertThat(
          aActualFilm.getSubtitles().stream().map(URL::toString).collect(Collectors.toList()),
          hasItem(aExpectedSubtitle));
    }
  }

  public static void assertEquals(
      final Film aActualFilm,
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

    assertEquals(
        aActualFilm,
        aExpectedSender,
        aExpectedTheme,
        aExpectedTitle,
        aExpectedTime,
        aExpectedDuration,
        aExpectedDescription,
        aWebsiteUrl,
        aExpectedGeo,
        aExpectedUrlSmall,
        aExpectedUrlNormal,
        aExpectedUrlHd,
        aExpectedSubtitle);
    assertSignLanguages(
        aActualFilm,
        aExpectedUrlSignLanguageSmall,
        aExpectedUrlSignLanguageNormal,
        aExpectedUrlSignLanguageHd);
    assertAudioDescriptions(
        aActualFilm,
        aExpectedUrlAudioDescriptionSmall,
        aExpectedUrlAudioDescriptionNormal,
        aExpectedUrlAudioDescriptionHd);
  }

  public static void assertUrl(final String aExpectedUrl, final FilmUrl aActualUrl) {
    if (aExpectedUrl.isEmpty()) {
      assertThat(aActualUrl, nullValue());
    } else {
      assertThat(
          cleanupWireMockUrlForCheck(aActualUrl.toString()),
          equalTo(cleanupWireMockUrlForCheck(aExpectedUrl)));
    }
  }
  
  // generate a test case constructor from a film
  public static String toTestCase(final String fileNameAndPath, final Film f) {
    String x = ("" +
        "/*jsonFile*/ \""+fileNameAndPath+"\"" + ",\n" +
        "/*topic*/ \""+ f.getThema()+"\"" + ",\n" +
        "/*title*/ \""+ f.getTitel()+"\"" + ",\n" +
        "/*description*/ \""+ f.getBeschreibung()+"\"" + ",\n" +
        "/*date*/ LocalDateTime.parse(\""+ f.getTime().toString()+"\")" + ",\n" +
        "/*duration*/ Duration.parse(\""+ f.getDuration().toString()+"\")" + ",\n" +
        "/*small*/ \""+ f.getUrl(Resolution.SMALL)+"\"" + ",\n" +
        "/*normal*/ \""+ f.getUrl(Resolution.NORMAL)+"\"" + ",\n" +
        "/*hd*/ \""+ f.getUrl(Resolution.HD)+"\"" + ",\n" +
        "/*ADsmall*/ \""+ (f.getAudioDescription(Resolution.SMALL) != null ? f.getAudioDescription(Resolution.SMALL) : "") +"\"" + ",\n" +
        "/*ADnormal*/ \""+ (f.getAudioDescription(Resolution.NORMAL) != null ? f.getAudioDescription(Resolution.NORMAL) : "")+"\"" + ",\n" +
        "/*ADhd*/ \""+ (f.getAudioDescription(Resolution.HD) != null ? f.getAudioDescription(Resolution.HD) : "")+"\"" + ",\n" +
        "/*DGSsmall*/ \""+ (f.getSignLanguage(Resolution.SMALL) != null ? f.getSignLanguage(Resolution.SMALL) : "") +"\"" + ",\n" +
        "/*DGSnormal*/ \""+ (f.getSignLanguage(Resolution.NORMAL) != null ? f.getSignLanguage(Resolution.NORMAL) : "")+"\"" + ",\n" +
        "/*DGShd*/ \""+ (f.getSignLanguage(Resolution.HD) != null ? f.getSignLanguage(Resolution.HD) : "")+"\"" + ",\n" +
        "/*sub*/ \""+ (f.getSubtitles().parallelStream().findFirst().isPresent() ? f.getSubtitles().parallelStream().findFirst().get().toString(): "")+"\"" + ",\n" +
        "/*website*/ \""+ (f.getWebsite().isPresent() ? f.getWebsite().get().toString() : "")+"\"" + ",\n" +
        "/*hd*/ GeoLocations."+ f.getGeoLocations().toString() + ",\n" +
        "/*related*/ new ArdFilmInfoDto[0]" + ",\n" +
        "/*sender*/ Optional.of(Sender."+f.getSender()+")" + ",\n" +
        "");
    return x;
  }

  private static String cleanupWireMockUrlForCheck(final String urlToCleanUp) {
    return urlToCleanUp.replaceAll("localhost:\\d+", "localhost:8589");
  }

  public static void assertUrl(final String aExpectedUrl, final Optional<String> aActualUrl) {
    assertThat(aActualUrl.isPresent(), equalTo(!aExpectedUrl.isEmpty()));
    if (aActualUrl.isPresent()) {
      assertThat(aActualUrl.get(), equalTo(aExpectedUrl));
    }
  }

  private static void assertAudioDescriptions(
      final Film aActualFilm,
      final String aExpectedUrlAudioDescriptionSmall,
      final String aExpectedUrlAudioDescriptionNormal,
      final String aExpectedUrlAudioDescriptionHd) {
    assertUrl(aExpectedUrlAudioDescriptionSmall, aActualFilm.getAudioDescription(Resolution.SMALL));
    assertUrl(
        aExpectedUrlAudioDescriptionNormal, aActualFilm.getAudioDescription(Resolution.NORMAL));
    assertUrl(aExpectedUrlAudioDescriptionHd, aActualFilm.getAudioDescription(Resolution.HD));
  }

  private static void assertSignLanguages(
      final Film aActualFilm,
      final String aExpectedUrlSignLanguageSmall,
      final String aExpectedUrlSignLanguageNormal,
      final String aExpectedUrlSignLanguageHd) {
    assertUrl(aExpectedUrlSignLanguageSmall, aActualFilm.getSignLanguage(Resolution.SMALL));
    assertUrl(aExpectedUrlSignLanguageNormal, aActualFilm.getSignLanguage(Resolution.NORMAL));
    assertUrl(aExpectedUrlSignLanguageHd, aActualFilm.getSignLanguage(Resolution.HD));
  }
  
}
