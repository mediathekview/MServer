package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.filmlisten.FilmlistManager;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class WriteFilmlistHashFileTest {

  @Test
  public void testWriteFilmlistHashFile() throws IOException {
    final Path testHashFilePath = Files.createTempFile("filmlist", "hash");
    final Filmlist filmlist = new Filmlist();
    FilmlistManager.getInstance().writeHashFile(filmlist, testHashFilePath.toAbsolutePath());
    assertThat(
        String.valueOf(filmlist.hashCode()), equalTo(Files.readAllLines(testHashFilePath).get(0)));
  }
}
