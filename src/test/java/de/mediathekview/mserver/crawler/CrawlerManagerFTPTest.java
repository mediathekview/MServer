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
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.MessageTypes;
import de.mediathekview.mlib.messages.MessageUtil;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mlib.progress.Progress;
import de.mediathekview.mlib.progress.ProgressListener;
import de.mediathekview.mserver.base.config.MServerConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.config.MServerFTPSettings;

@RunWith(Parameterized.class)
public class CrawlerManagerFTPTest implements MessageListener, ProgressListener {

	private static final Logger LOG = LogManager.getLogger(CrawlerManagerFTPTest.class);
	private static final String TEMP_FOLDER_NAME_PATTERN = "MSERVER_FTP_TEST_%d";
	private static final String FILMLISTS_TARGET_BASE_PATH = "/var/www/mediathekview/filmlisten/";
	private static final String FTP_PASSWORD = "!fTpSeCuReD";
	private static final String FTP_USER = "ftpUser";
	private static final String BASE_FOLDER = "";
	private static final CrawlerManager CRAWLER_MANAGER = CrawlerManager.getInstance();
	private static Path testFileFolderPath;
	private static Path baseFolderPath;
	private static FakeFtpServer fakeFtpServer;

	private final String filmlistPath;
	private final FilmlistFormats format;
	private float lastProgress = 0f;

	@BeforeClass
	public static void initTestData() throws URISyntaxException, IOException {
		baseFolderPath = Paths.get(CrawlerManagerFTPTest.class.getClassLoader().getResource(BASE_FOLDER).toURI());
		testFileFolderPath = Files.createTempDirectory(formatWithDate(TEMP_FOLDER_NAME_PATTERN));
		Files.createDirectory(testFileFolderPath.resolve("filmlists"));

		FakeFtpServer fakeFtpServer = createTestFTPServer();
		fakeFtpServer.start();
	}

	private static FakeFtpServer createTestFTPServer() {
		fakeFtpServer = new FakeFtpServer();
		fakeFtpServer.setServerControlPort(0);
		fakeFtpServer.addUserAccount(new UserAccount(FTP_USER, FTP_PASSWORD, "/home/ftpUser/"));

		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry(FILMLISTS_TARGET_BASE_PATH));
		fakeFtpServer.setFileSystem(fileSystem);
		return fakeFtpServer;
	}

	@Parameterized.Parameters(name = "Test {index} Filmlist FTP upload for {0} with {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "TestFilmlistNewJson.json", FilmlistFormats.JSON },
				{ "TestFilmlistNewJson.json.xz", FilmlistFormats.JSON_COMPRESSED },
				{ "TestFilmlist.json", FilmlistFormats.OLD_JSON },
				{ "TestFilmlist.json.xz", FilmlistFormats.OLD_JSON_COMPRESSED } });
	}

	public CrawlerManagerFTPTest(final String aFilmlistPath, final FilmlistFormats aFormat) {
		filmlistPath = aFilmlistPath;
		format = aFormat;
	}

	@AfterClass
	public static void deleteTempFiles() throws IOException {
		Files.walk(testFileFolderPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

	@Test
	public void testSaveAndUpload() throws IOException {
		CRAWLER_MANAGER.addMessageListener(this);
		CRAWLER_MANAGER.addFTPProgressListener(this);

		MServerConfigDTO config = MServerConfigManager.getInstance().getConfig();
		config.setFilmlistImportFormat(format);
		config.setFilmlistImportLocation(
				baseFolderPath.resolve("filmlists").resolve(filmlistPath).toAbsolutePath().toString());
		CRAWLER_MANAGER.importFilmlist();

		config.getFilmlistSaveFormats().clear();
		config.getFilmlistSaveFormats().add(format);
		config.getFilmlistSavePaths().put(format, testFileFolderPath.resolve(filmlistPath).toAbsolutePath().toString());
		CRAWLER_MANAGER.saveFilmlist();

		MServerFTPSettings ftpSettings = config.getFtpSettings();
		ftpSettings.setFtpEnabled(true);
		ftpSettings.setFtpUrl("localhost");
		ftpSettings.setFtpUsername(FTP_USER);
		ftpSettings.setFtpPassword(FTP_PASSWORD);
		ftpSettings.getFtpTargetFilePaths().clear();
		ftpSettings.getFtpTargetFilePaths().put(format, FILMLISTS_TARGET_BASE_PATH + filmlistPath);
		ftpSettings.setFtpPort(fakeFtpServer.getServerControlPort());
		CRAWLER_MANAGER.uploadFilmlist();
	}

	private static String formatWithDate(final String aPattern) {
		return String.format(aPattern, new Date().getTime());
	}

	@Override
	public void consumeMessage(final Message aMessage, final Object... aParameters) {
		if (MessageTypes.FATAL_ERROR.equals(aMessage.getMessageType())) {
			Assert.fail(String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters));
		} else if (MessageTypes.ERROR.equals(aMessage.getMessageType())) {
			LOG.error(String.format("%s: %s", aMessage.getMessageType().name(),
					String.format(MessageUtil.getInstance().loadMessageText(aMessage), aParameters)));
		}

	}

	@Override
	public void updateProgess(Progress aUploadProgress) {
		if (aUploadProgress.calcProgressInPercent() - lastProgress > 1f) {
			LOG.info("FTP Upload Progress: " + aUploadProgress.calcProgressInPercent());
			lastProgress = aUploadProgress.calcProgressInPercent();
		}
	}

}
