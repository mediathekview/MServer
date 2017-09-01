package de.mediathekview.mserver.ui.config;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.messages.listener.LogMessageListener;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.config.MServerLogSettingsDTO;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.CrawlerManager;
import de.mediathekview.mserver.progress.listeners.ProgressLogMessageListener;

public final class MServerConfigUI {

	private static final Logger LOG = LogManager.getLogger(MServerConfigUI.class);
	private static final String CONFIG_FILE_NAME = "MServer-Config.yaml";
	private static final String ARGUMENT_GCONF = "-gconf";
	private final LogMessageListener logMessageListener;
	private final MServerConfigDTO config;

	public static void main(final String[] args) {
		new MServerConfigUI().start(args);
	}

	public MServerConfigUI() {
		super();
		config = MServerConfigManager.getInstance().getConfig();
		final MServerLogSettingsDTO logSettings = config.getLogSettings();
		logSettings.setLogActivateConsole(true);

		logMessageListener = new LogMessageListener();

		final Level configLevel = logSettings.getLogLevelConsole();
		if (configLevel == null || !logLevelInfoOrLower(configLevel)) {
			logSettings.setLogLevelConsole(Level.INFO);
		}

	}

	private boolean logLevelInfoOrLower(final Level configLevel) {
		return Level.INFO.equals(configLevel) || Level.DEBUG.equals(configLevel) || Level.ALL.equals(configLevel);
	}

	void start(final String[] aProgramAgruments) {
		if (interpretProgramArguments(aProgramAgruments)) {
			final CrawlerManager manager = CrawlerManager.getInstance();
			addListeners(manager);
			manager.start();
			manager.importFilmlist();
			manager.saveFilmlist();
			manager.uploadFilmlist();
		}
	}

	private void addListeners(final CrawlerManager manager) {
		final List<ProgressLogMessageListener> progressListeners = new ArrayList<>();
		progressListeners.add(new ProgressLogMessageListener());

		final List<MessageListener> messageListeners = new ArrayList<>();
		messageListeners.add(logMessageListener);
		manager.addAllProgressListener(progressListeners);
		manager.addAllMessageListener(messageListeners);
	}

	private boolean interpretProgramArguments(final String[] aProgramAgruments) {
		if (aProgramAgruments != null && aProgramAgruments.length > 0) {
			if (aProgramAgruments.length > 1) {
				logMessageListener.consumeMessage(ServerMessages.UI_TO_MANY_ARGUMENTS);
			}

			if (ARGUMENT_GCONF.equals(aProgramAgruments[0])) {
				generateDefaultConfiguration();
			} else {
				logMessageListener.consumeMessage(ServerMessages.UI_UNKNOWN_ARGUMENT, aProgramAgruments[0],
						ARGUMENT_GCONF);
				return false;
			}
		}

		return true;
	}

	private void generateDefaultConfiguration() {
		final Path configFilePath = Paths.get(CONFIG_FILE_NAME);
		try {
			Path defaultConfigFile = Paths.get(this.getClass().getClassLoader().getResource(CONFIG_FILE_NAME).toURI());
			Files.copy(defaultConfigFile, configFilePath);
		} catch (final IOException | URISyntaxException ioException) {
			LOG.error("The default configuration can't be generated.", ioException);
			logMessageListener.consumeMessage(ServerMessages.UI_GENERATE_DEFAULT_CONFIG_FILE_FAILED,
					configFilePath.toAbsolutePath().toString());
		}
	}

}
