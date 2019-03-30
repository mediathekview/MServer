package de.mediathekview.mserver.ui.config;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mlib.filmlisten.FilmlistManager;
import de.mediathekview.mserver.base.config.MServerConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrawlerTimeoutFilmlistTest {

  private Path testFilmlistPath;
  private FilmlistFormats testFilmlistFormat;

  @Before
  public void setUp() throws IOException {
    final MServerConfigManager config = MServerConfigManager.getInstance();
    final MServerConfigDTO serverConfig = config.getConfig();
    serverConfig.setSenderIncluded(Stream.of(Sender.ARD).collect(Collectors.toSet()));
    serverConfig.setFilmlistImporEnabled(false);
    testFilmlistFormat = FilmlistFormats.JSON;
    serverConfig.setFilmlistSaveFormats(Stream.of(testFilmlistFormat).collect(Collectors.toSet()));
    serverConfig.getCopySettings().setCopyEnabled(false);

    final HashMap<FilmlistFormats, String> savePaths = new HashMap<>();
    testFilmlistPath = Files.createTempFile("TestFilmlist", ".json").toAbsolutePath();
    savePaths.put(FilmlistFormats.JSON, testFilmlistPath.toString());
    serverConfig.setFilmlistSavePaths(savePaths);

    config.getSenderConfig(Sender.ARD).setMaximumCrawlDurationInMinutes(1);
  }

  @Test
  public void testFilmlistNotEmptyAfterTimeout() throws IOException {
    new MServerConfigUI().start(null);
    Assert.assertThat(
        "No filmlist was created!", Files.exists(testFilmlistPath), Matchers.is(true));
    final Optional<Filmlist> filmlist =
        FilmlistManager.getInstance().importList(testFilmlistFormat, testFilmlistPath);

    Assert.assertThat("The filmlist couldn't be read!", filmlist.isPresent(), Matchers.is(true));
    Assert.assertThat(
        "The filmlist is empty!",
        filmlist.get().getFilms().values(),
        Matchers.not((Matchers.empty())));
  }
}
