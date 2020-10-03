package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.filmlisten.FilmlistManager;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WriteFilmlistIdFileTest {

  @Test
  public void testWriteFilmlistIdFile() throws IOException {
    final Path testIdFilePath = Files.createTempFile("filmlist", "id");
    final Filmlist filmlist = new Filmlist();
    FilmlistManager.getInstance().writeIdFile(filmlist, testIdFilePath.toAbsolutePath());
    assertThat(
        String.valueOf(filmlist.getListId()), equalTo(Files.readAllLines(testIdFilePath).get(0)));
  }
}
