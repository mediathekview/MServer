package de.mediathekview.mserver.ui.config;

import de.mediathekview.mlib.messages.listener.LogMessageListener;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.Log4JConfigurationFactory;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.config.MServerLogSettingsDTO;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.CrawlerManager;
import de.mediathekview.mserver.progress.listeners.ProgressLogMessageListener;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class MServerConfigUI {
  // logger setup in start
  private Logger LOG = null;
  private static final String CONFIG_FILE_NAME = "MServer-Config.yaml";
  private static final String ARGUMENT_GCONF = "-gconf";
  private LogMessageListener logMessageListener;
  private CrawlerManager manager;

  public MServerConfigUI() {
    super();
  }

  public static void main(final String[] args) {
    new MServerConfigUI().start(args);
  }

  private void addListeners() {
    final List<ProgressLogMessageListener> progressListeners = new ArrayList<>();
    progressListeners.add(new ProgressLogMessageListener());

    final List<MessageListener> messageListeners = new ArrayList<>();
    messageListeners.add(logMessageListener);
    manager.addAllProgressListener(progressListeners);
    manager.addAllMessageListener(messageListeners);
  }

  private void generateDefaultConfiguration() {
    final Path configFilePath = Paths.get(CONFIG_FILE_NAME);
    try {
      final Path defaultConfigFile =
          Paths.get(getClass().getClassLoader().getResource(CONFIG_FILE_NAME).toURI());
      Files.copy(defaultConfigFile, configFilePath);
    } catch (final IOException | URISyntaxException ioException) {
      LOG.error("The default configuration can't be generated.", ioException);
      logMessageListener.consumeMessage(
          ServerMessages.UI_GENERATE_DEFAULT_CONFIG_FILE_FAILED,
          configFilePath.toAbsolutePath().toString());
    }
  }

  private boolean interpretProgramArguments(final String[] aProgramAgruments) {
    if (aProgramAgruments != null && aProgramAgruments.length > 0) {
      if (aProgramAgruments.length > 1) {
        logMessageListener.consumeMessage(ServerMessages.UI_TO_MANY_ARGUMENTS);
      }

      if (ARGUMENT_GCONF.equals(aProgramAgruments[0])) {
        generateDefaultConfiguration();
      }
    }

    return true;
  }

  private boolean logLevelInfoOrLower(final Level configLevel) {
    return Level.INFO.equals(configLevel)
        || Level.DEBUG.equals(configLevel)
        || Level.ALL.equals(configLevel);
  }

  void start() {
    try {
      manager.start();
      manager.importFilmlist();
      manager.importLivestreamFilmlist();
    } finally {
      manager.filterFilmlist();
      manager.saveFilmlist();
      manager.saveDifferenceFilmlist();
      manager.writeHashFile();
      manager.writeIdFile();
      manager.copyFilmlist();
      manager.stop();
    }
  }

  void start(final String[] aProgramAgruments) {
    MServerConfigManager aMServerConfigManager = null;
    if (aProgramAgruments.length > 0 && !ARGUMENT_GCONF.equals(aProgramAgruments[0])) {
      String configFileName = aProgramAgruments[0];
      if (configFileName.startsWith("http")) {
        URL fileUrl;
        try {
          // get a copy of this file to use it as configuration file
          fileUrl = new URL(configFileName);
          String filename = Paths.get(fileUrl.getPath()).getFileName().toString();
          MServerConfigUI.getRemoteFileToLocal(configFileName, filename);
          configFileName = filename;
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }
      aMServerConfigManager = new MServerConfigManager(configFileName);
    } else {
      aMServerConfigManager = new MServerConfigManager(MServerConfigManager.DEFAULT_CONFIG_FILE);
    }
    // here we set the correct configManager for all log4logger
    // logsettings are stored static in our factory
    new Log4JConfigurationFactory(aMServerConfigManager.getConfig().getLogSettings());
    LOG = LogManager.getLogger(MServerConfigUI.class);
    logMessageListener = new LogMessageListener();
    
    if (interpretProgramArguments(aProgramAgruments)) {
      manager = new CrawlerManager(aMServerConfigManager);
      final MServerLogSettingsDTO logSettings = aMServerConfigManager.getConfig().getLogSettings();
      logSettings.setLogActivateConsole(true);
      final Level configLevel = logSettings.getLogLevelConsole();
      if (configLevel == null || !logLevelInfoOrLower(configLevel)) {
        logSettings.setLogLevelConsole(Level.INFO);
      }
      addListeners();
      start();
    }
  }
  
  public static void getRemoteFileToLocal(String source, String target) {
    try {
        URL fileUrl = new URL(source);
        try (InputStream in = fileUrl.openStream()) {
            Path outputPath = Path.of(target);
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }
    } catch (IOException e) {
        e.printStackTrace(); // we do not have a logger yet
    }
  }
}
