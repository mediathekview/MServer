package de.mediathekview.mserver.filmlisten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.FilmUrl;
import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.filmlisten.reader.FilmlistReader;
import de.mediathekview.mserver.filmlisten.writer.FilmlistWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Die Sprache muss auch durch das neue Format und ueber merge() hinweg erhalten bleiben. */
class NewFilmlistAudioLanguageTest {

  @TempDir Path tempDir;

  @Test
  void audioLanguageSurvivesTheNewFormat() throws IOException {
    final Filmlist input = new Filmlist();
    final Film withLanguage = buildFilm("Mit Sprache (Originalversion)");
    withLanguage.setAudioLanguage("eng");
    input.add(withLanguage);
    input.add(buildFilm("Ohne Sprache"));

    final Path tempFile = Files.createTempFile(tempDir, "NewFormatAudioLanguage", ".json");
    new FilmlistWriter().write(input, tempFile);
    final Optional<Filmlist> read =
        new FilmlistReader().read(new FileInputStream(tempFile.toString()));
    Files.deleteIfExists(tempFile);

    assertTrue(read.isPresent());
    assertEquals("eng", findByTitle(read.get(), "Mit Sprache (Originalversion)").getAudioLanguage());
    assertNull(findByTitle(read.get(), "Ohne Sprache").getAudioLanguage());
  }

  @Test
  void mergeKeepsAKnownLanguage() throws MalformedURLException {
    // Eine importierte aeltere Filmliste kennt das Feld nicht. Wird sie mit einem frischen Crawl
    // zusammengefuehrt, darf die dort bekannte Sprache nicht verloren gehen.
    final Film ausAelteremStand = buildFilm("Folge (Originalversion)");
    final Film ausFrischemCrawl = buildFilm("Folge (Originalversion)");
    ausFrischemCrawl.setAudioLanguage("fra");

    ausAelteremStand.merge(ausFrischemCrawl);

    assertEquals("fra", ausAelteremStand.getAudioLanguage());
  }

  @Test
  void mergeDoesNotOverwriteAnExistingLanguage() throws MalformedURLException {
    final Film bekannt = buildFilm("Folge (Originalversion)");
    bekannt.setAudioLanguage("eng");
    final Film ohne = buildFilm("Folge (Originalversion)");

    bekannt.merge(ohne);

    assertEquals("eng", bekannt.getAudioLanguage());
  }

  private Film buildFilm(final String titel) throws MalformedURLException {
    final Film film =
        new Film(
            UUID.randomUUID(),
            Sender.ARD,
            titel,
            "Testthema",
            LocalDateTime.of(2026, 1, 1, 20, 15),
            Duration.ofMinutes(90));
    film.addUrl(Resolution.NORMAL, new FilmUrl("https://example.org/v.mp4", 1L));
    return film;
  }

  private Film findByTitle(final Filmlist list, final String titel) {
    return list.getFilms().values().stream()
        .filter(film -> titel.equals(film.getTitel()))
        .findFirst()
        .orElseThrow();
  }
}
