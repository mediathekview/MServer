package de.mediathekview.mserver.crawler.ard.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.crawler.ard.ArdFilmDto;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import org.junit.jupiter.api.Test;

/**
 * Prueft, dass die Sprache der Originalversion aus den Daten gelesen wird und nicht nur fuer die
 * beiden fest verdrahteten Codes "eng" und "fra" ankommt.
 */
class ArdFilmDeserializerAudioLanguageTest {

  @Test
  void originalVersionInAnyLanguageKeepsItsLanguageCode() {
    // Dieselbe Sendung wie ard_item_OV.json, aber die Originalversion ist als "spa" ausgezeichnet
    // statt mit ARDs Platzhalter "ov". Weder die OV- noch die eng-/fra-Abfrage greift hier; der
    // Treffer kommt ueber den Platzhalter "*", und die Sprache wird aus audio[0].languageCode
    // gelesen.
    final Film originalVersion = findOriginalVersion("/ard/ard_item_OV_spanish.json");

    assertThat(originalVersion, notNullValue());
    assertThat(originalVersion.getAudioLanguage(), equalTo("spa"));
  }

  @Test
  void originalVersionWithoutNamedLanguageStaysUnset() {
    // Gegenprobe mit dem unveraenderten Fixture: dort meldet die ARD nur ihren eigenen Platzhalter
    // "ov", der keine Sprache benennt. Dann darf auch nichts gesetzt werden.
    final Film originalVersion = findOriginalVersion("/ard/ard_item_OV.json");

    assertThat(originalVersion, notNullValue());
    assertThat(originalVersion.getAudioLanguage(), nullValue());
  }


  @Test
  void standardVersionMustNotInheritTheOriginalVersionLanguage() {
    // Regression: createFilm hat das Feld frueher unbedingt gesetzt und wird fuer beide Filme mit
    // demselben ArdVideoInfoDto aufgerufen. Dadurch bekam die deutsche Hauptfassung die Sprache
    // der Originalversion - bei einem Beitrag mit deutscher und englischer Tonspur also "eng".
    final List<Film> alle = allFilms("/ard/ard_item_OV_eng_plus_deu.json");
    assertThat(alle.size(), equalTo(2));

    final Film standard =
        alle.stream().filter(f -> !f.getTitel().contains("(Originalversion)")).findFirst().orElse(null);
    final Film originalVersion =
        alle.stream().filter(f -> f.getTitel().contains("(Originalversion)")).findFirst().orElse(null);

    assertThat(standard, notNullValue());
    assertThat(originalVersion, notNullValue());
    assertThat(standard.getAudioLanguage(), nullValue());
    assertThat(originalVersion.getAudioLanguage(), equalTo("eng"));
  }

  private List<Film> allFilms(final String jsonFile) {
    final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);
    return new ArdFilmDeserializer(createCrawler()).deserialize(jsonElement, null, null).stream()
        .map(ArdFilmDto::getFilm)
        .toList();
  }

  @Test
  void languageCodeWithRegionIsReducedToTheLanguage() {
    // Sollte die ARD den Code je mit Regionsangabe liefern, muss trotzdem der reine
    // ISO-639-2/T-Code im Feld stehen - "spa-ES" also als "spa".
    final Film originalVersion = findOriginalVersion("/ard/ard_item_OV_spanish_region.json");

    assertThat(originalVersion, notNullValue());
    assertThat(originalVersion.getAudioLanguage(), equalTo("spa"));
  }

  private Film findOriginalVersion(final String jsonFile) {
    final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);
    final List<ArdFilmDto> films =
        new ArdFilmDeserializer(createCrawler()).deserialize(jsonElement, null, null);

    return films.stream()
        .map(ArdFilmDto::getFilm)
        .filter(film -> film.getTitel().contains("(Originalversion)"))
        .findFirst()
        .orElse(null);
  }

  private ArdCrawler createCrawler() {
    return new ArdCrawler(
        new ForkJoinPool(),
        new ArrayList<MessageListener>(),
        new ArrayList<SenderProgressListener>(),
        new MServerConfigManager("MServer-JUnit-Config.yaml"));
  }
}
