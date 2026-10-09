package de.mediathekview.mserver.crawler.arte.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.base.utils.LanguageCodeUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * arte liefert die Sprache der Tonspur im Listing unter {@code videos[].versions[].audioLanguage}.
 * Bisher wurde dieser Block nicht ausgewertet.
 */
class ArteVideoInfoAudioLanguageTest {

  @Test
  void audioLanguagesAreReadFromTheVersionsBlock() {
    final Map<String, String> audioLanguages =
        firstVideo("/arte/arte_film_1_videos.json").getAudioLanguagesByCode();

    assertThat(audioLanguages, notNullValue());
    assertThat(audioLanguages, hasEntry("VOA", "de"));
    assertThat(audioLanguages, hasEntry("VF-STF", "fr"));
    // Die mehrsprachige Fassung traegt arte-seitig den Platzhalter "mul".
    assertThat(audioLanguages, hasEntry("VOEU[ALL]-STE[ANG]", "mul"));
  }

  @Test
  void normalizingTurnsArtesCodesIntoTheFieldsForm() {
    final Map<String, String> audioLanguages =
        firstVideo("/arte/arte_film_1_videos.json").getAudioLanguagesByCode();

    assertThat(LanguageCodeUtils.normalize(audioLanguages.get("VOA")), equalTo(Optional.of("deu")));
    assertThat(LanguageCodeUtils.normalize(audioLanguages.get("VF-STF")), equalTo(Optional.of("fra")));
    // "mul" benennt keine einzelne Sprache - das Feld bleibt leer, statt zu raten.
    assertThat(
        LanguageCodeUtils.normalize(audioLanguages.get("VOEU[ALL]-STE[ANG]")),
        equalTo(Optional.empty()));
  }

  @Test
  void aVideoWithoutVersionsBlockYieldsAnEmptyMapInsteadOfFailing() {
    assertThat(
        firstVideo("/arte/arte_film_2_videos.json").getAudioLanguagesByCode(), notNullValue());
  }

  /**
   * Fuer die reine Originalfassung (Code "VO") traegt der versions-Block nur den Platzhalter
   * {@code und}. arte fuehrt die Sprache des Originals daneben in {@code originalLanguage}.
   */
  @Test
  void theOriginalLanguageIsReadAsFallbackForTheVoPlaceholder() {
    final ArteVideoInfoDto video = firstVideo("/arte/arte_film_5_videos.json");

    assertThat(video.getOriginalLanguage(), equalTo(Optional.of("fr")));
    assertThat(LanguageCodeUtils.normalize(video.getOriginalLanguage().get()), equalTo(Optional.of("fra")));
  }

  /**
   * Wo arte das Original selbst nicht benennt - bei einer rein musikalischen Fassung steht dort der
   * arte-Code "MUS" mit leerem ISO-Code - bleibt auch der Rueckgriff ohne Ergebnis.
   */
  @Test
  void anUnnamedOriginalLanguageStaysEmpty() {
    final ArteVideoInfoDto video = firstVideo("/arte/arte_film_6_videos.json");

    assertThat(
        LanguageCodeUtils.normalize(video.getOriginalLanguage().orElse(null)),
        equalTo(Optional.empty()));
  }

  private ArteVideoInfoDto firstVideo(final String jsonFile) {
    final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);
    final PagedElementListDTO<ArteVideoInfoDto> result =
        new ArteVideoInfoDeserializer().deserialize(jsonElement, null, null);
    return result.getElements().stream().findFirst().orElseThrow();
  }
}
