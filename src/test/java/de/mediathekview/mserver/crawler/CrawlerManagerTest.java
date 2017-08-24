package de.mediathekview.mserver.crawler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.MessageTypes;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mlib.messages.listener.MessageListener;

@RunWith(Parameterized.class)
public class CrawlerManagerTest implements MessageListener
{

    private static final Logger LOG = LogManager.getLogger(CrawlerManagerTest.class);
    private static final String TEMP_FOLDER_NAME_PATTERN = "MSERVER_TEST_%d";
    private static final String BASE_FOLDER = "";
    private static final CrawlerManager CRAWLER_MANAGER = CrawlerManager.getInstance();
    private static Path testFileFolderPath;
    private static Path baseFolderPath;

    private final String filmlistPath;
    private final FilmlistFormats format;

    @BeforeClass
    public static void initTestData() throws URISyntaxException, IOException
    {
        baseFolderPath = Paths.get(CrawlerManagerTest.class.getClassLoader().getResource(BASE_FOLDER).toURI());
        testFileFolderPath = Files.createTempDirectory(formatWithDate(TEMP_FOLDER_NAME_PATTERN));
        Files.createDirectory(testFileFolderPath.resolve("filmlists"));
    }

    @Parameterized.Parameters(name = "Test {index} Filmlist for {0} with {1}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]
        {
                { "filmlists/TestFilmlistNewJson.json", FilmlistFormats.JSON },
                { "filmlists/TestFilmlistNewJson.json.xz", FilmlistFormats.JSON_COMPRESSED },
                { "filmlists/TestFilmlist.json", FilmlistFormats.OLD_JSON },
                { "filmlists/TestFilmlist.json.xz", FilmlistFormats.OLD_JSON_COMPRESSED } });
    }

    public CrawlerManagerTest(final String aFilmlistPath, final FilmlistFormats aFormat)
    {
        filmlistPath = aFilmlistPath;
        format = aFormat;
    }

    private static String formatWithDate(final String aPattern)
    {
        return String.format(aPattern, new Date().getTime());
    }

    @AfterClass
    public static void deleteTempFiles() throws IOException
    {
        Files.walk(testFileFolderPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    @Test
    public void testSaveAndImport() throws IOException
    {
        CRAWLER_MANAGER.addMessageListener(this);
        CRAWLER_MANAGER.importFilmlist(format, baseFolderPath.resolve(filmlistPath).toAbsolutePath().toString());
        CRAWLER_MANAGER.saveFilmlist(testFileFolderPath.resolve(filmlistPath), format);
    }

    @Override
    public void consumeMessage(final Message aMessage, final Object... aParameters)
    {
        if (MessageTypes.FATAL_ERROR.equals(aMessage.getMessageType()))
        {
            Assert.fail(MessageUtil.getInstance().loadMessageText(aMessage));
        }
        else
        {
            LOG.info("%s: %s", aMessage.getMessageType().name(), MessageUtil.getInstance().loadMessageText(aMessage));
        }

    }

}
