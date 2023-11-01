package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.MessageTypes;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.ImportFilmlistConfiguration;
import de.mediathekview.mserver.testhelper.FileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.api.Assertions.fail;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Stream;


public class CrawlerManagerImportFilmlistsTest implements MessageListener {

  private static final Logger LOG = LogManager.getLogger(CrawlerManagerImportFilmlistsTest.class);
  private static final String TEMP_FOLDER_NAME_PATTERN = "MSERVER_TEST_%d";
  private static Path testFileFolderPath;

  static Stream<Arguments> getTestArgumentForFilmlistsInDifferentFormats() {
    ImportFilmlistConfiguration import1 = new ImportFilmlistConfiguration(
        true,
        FileReader.getPath("./filmlists/importFilmlist/FilmlistImportTest1.json").toString(),
        FilmlistFormats.OLD_JSON,
        true,
        false);
    ImportFilmlistConfiguration import2 = new ImportFilmlistConfiguration(
        true,
        FileReader.getPath("./filmlists/importFilmlist/FilmlistImportTest2.json").toString(),
        FilmlistFormats.OLD_JSON,
        true,
        false);
    ImportFilmlistConfiguration import3 = new ImportFilmlistConfiguration(
        true,
        FileReader.getPath("./filmlists/importFilmlist/FilmlistImportTest3.json").toString(),
        FilmlistFormats.OLD_JSON,
        true,
        false);
    ImportFilmlistConfiguration import4 = new ImportFilmlistConfiguration(
        true,
        FileReader.getPath("./filmlists/importFilmlist/FilmlistImportTest2.json").toString(),
        FilmlistFormats.OLD_JSON,
        false,
        false);
    
    return Stream.of(
        arguments(import1, import1, 3, 0), // two times the same list should result in no additional films
        arguments(import1, import2, 5, 3), // two different lists should result in the sum of both lists - the fist list is also diff since list two is "old"
        arguments(import1, import3, 4, 1), // overlapping lists - one entry is new compared to the "old" list
        arguments(import2, import3, 4, 1), // overlapping lists - one entry is new compared to the "old" list
        arguments(import1, import4, 5, 0)  // two different lists and not diff list active
        
    );
  }
  
  @ParameterizedTest
  @Execution(ExecutionMode.SAME_THREAD)
  @MethodSource("getTestArgumentForFilmlistsInDifferentFormats")
  void testSaveAndImport(final ImportFilmlistConfiguration initialList, final ImportFilmlistConfiguration additionalList,  final int expectedSize, final int expectedDiffListSize) {
    CrawlerManager crawlerManagerForEachRun = createEmptyCrawlerManager();
    crawlerManagerForEachRun.addMessageListener(this);
    crawlerManagerForEachRun.importFilmlist(initialList);
    crawlerManagerForEachRun.importFilmlist(additionalList);
    assertThat(crawlerManagerForEachRun.getFilmlist().getFilms()).hasSize(expectedSize);
    assertThat(crawlerManagerForEachRun.getDifferenceList().getFilms()).hasSize(expectedDiffListSize);
  }

  public CrawlerManager createEmptyCrawlerManager() {
    // reset singelton CrawlerManager to have an empty filmlist
    Field instance;
    try {
      instance = CrawlerManager.class.getDeclaredField("instance");
      instance.setAccessible(true);
      instance.set(null, null);
    } catch (Exception e) {
      fail("Exception mooking crawler manager: " + e.getMessage());
    }    //
    return CrawlerManager.getInstance();
  }

  @AfterAll
  public static void deleteTempFiles() throws IOException {
    Files.walk(testFileFolderPath)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  @BeforeAll
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
      fail(String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters));
    } else {
      LOG.info(
          String.format(
              "%s: %s",
              aMessage.getMessageType().name(),
              String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters)));
    }
  }

}
