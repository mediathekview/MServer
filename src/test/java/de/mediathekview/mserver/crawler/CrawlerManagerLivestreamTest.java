package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.MessageTypes;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.testhelper.FileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class CrawlerManagerLivestreamTest implements MessageListener {

  private static final Logger LOG = LogManager.getLogger(CrawlerManagerLivestreamTest.class);
  private static final String TEMP_FOLDER_NAME_PATTERN = "MSERVER_TEST_%d";
  private static Path testFileFolderPath;

  private final CrawlerManager crawlerManager;
  private final FilmlistFormats format;
  private final String filmlistPath;
  private final String livestreamPath;
  private final int expectedInitialSize;
  private final int expectedAfterImport;
  

  public CrawlerManagerLivestreamTest(final FilmlistFormats aFormat, final String aFilmlistPath,final String aLivestreamPath,  final int aExpectedInitialSize, final int aExpectedAfterImport ) {
    format = aFormat;
    filmlistPath = aFilmlistPath;
    livestreamPath = aLivestreamPath;
    expectedInitialSize = aExpectedInitialSize;
    expectedAfterImport = aExpectedAfterImport;
    
    // reset singelton CrawlerManager
    Field instance;
    try {
      instance = CrawlerManager.class.getDeclaredField("instance");
      instance.setAccessible(true);
      instance.set(null, null);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }    //
    crawlerManager = CrawlerManager.getInstance();
  }

  @Parameterized.Parameters(name = "Test {index} Filmlist for {0} with {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {FilmlistFormats.JSON, "filmlists/TestFilmlistNewJson.json", "filmlists/livestream/live-streams.json", 3, 50},
          {FilmlistFormats.JSON_COMPRESSED_XZ, "filmlists/TestFilmlistNewJson.json.xz", "filmlists/livestream/live-streams.json.xz", 3, 50},
          {FilmlistFormats.JSON_COMPRESSED_BZIP, "filmlists/TestFilmlistNewJson.json.bz", "filmlists/livestream/live-streams.json.bz", 3, 50},
          {FilmlistFormats.JSON_COMPRESSED_GZIP, "filmlists/TestFilmlistNewJson.json.gz", "filmlists/livestream/live-streams.json.gz", 3, 50},
          {FilmlistFormats.OLD_JSON, "filmlists/TestFilmlist.json", "filmlists/livestream/live-streams_old.json", 3, 50},
          {FilmlistFormats.OLD_JSON_COMPRESSED_XZ, "filmlists/TestFilmlist.json.xz", "filmlists/livestream/live-streams_old.json.xz", 3, 50},
          {FilmlistFormats.OLD_JSON_COMPRESSED_BZIP, "filmlists/TestFilmlist.json.bz", "filmlists/livestream/live-streams_old.json.bz", 3, 50},
          {FilmlistFormats.OLD_JSON_COMPRESSED_GZIP, "filmlists/TestFilmlist.json.gz", "filmlists/livestream/live-streams_old.json.gz", 3, 50},
          {FilmlistFormats.JSON, "filmlists/livestream/live-streams.json", "filmlists/livestream/live-streams.json", 47, 47},
        });
  }

  @AfterClass
  public static void deleteTempFiles() throws IOException {
    Files.walk(testFileFolderPath)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  @BeforeClass
  public static void initTestData() throws Exception {
    testFileFolderPath = Files.createTempDirectory(formatWithDate(TEMP_FOLDER_NAME_PATTERN));
    Files.createDirectory(testFileFolderPath.resolve("filmlists"));
  }

  private static String formatWithDate(final String aPattern) {
    return String.format(aPattern, new Date().getTime());
  }

  @Override
  public void consumeMessage(final Message aMessage, final Object... aParameters) {
    if (MessageTypes.FATAL_ERROR.equals(aMessage.getMessageType())) {
      Assert.fail(String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters));
    } else {
      LOG.info(
          String.format(
              "%s: %s",
              aMessage.getMessageType().name(),
              String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters)));
    }
  }

  @Test
  public void testSaveAndImport() {
    final Path filmListFilePath = FileReader.getPath(filmlistPath);
    final Path livesreamFilmListFilePath = FileReader.getPath(livestreamPath);
    synchronized (crawlerManager) {
      crawlerManager.addMessageListener(this);
      crawlerManager.importFilmlist(format, filmListFilePath.toAbsolutePath().toString());
      //
      assertThat(crawlerManager.getFilmlist().getFilms().size()).isEqualTo(expectedInitialSize);
      //
      crawlerManager.importLivestreamFilmlist(format, livesreamFilmListFilePath.toAbsolutePath().toString());
      //
      assertThat(crawlerManager.getFilmlist().getFilms().size()).isEqualTo(expectedAfterImport);
      //
      crawlerManager.saveFilmlist(testFileFolderPath.resolve(filmlistPath), format);
      //
      assertThat(testFileFolderPath.resolve(filmlistPath)).exists();
    }
  }
}
