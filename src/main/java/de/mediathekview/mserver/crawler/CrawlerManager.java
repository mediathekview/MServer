package de.mediathekview.mserver.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.filmlisten.FilmlistFormats;
import de.mediathekview.mlib.filmlisten.FilmlistManager;
import de.mediathekview.mlib.progress.ProgressListener;
import de.mediathekview.mserver.base.config.MServerConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.config.MServerFTPSettings;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.progress.AbstractManager;
import de.mediathekview.mserver.base.uploader.ftp.FtpUploadTarget;
import de.mediathekview.mserver.base.uploader.ftp.FtpUploadTask;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TimeoutTask;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.crawler.dreisat.DreiSatCrawler;
import de.mediathekview.mserver.crawler.funk.FunkCrawler;

/**
 * A manager to control the crawler.
 */
public class CrawlerManager extends AbstractManager {
  private static final String HOME_PATTERN = "^~";
  private static final String USER_HOME_PATH = System.getProperty("user.home");
  private static final String FILMLIST_IMPORT_ERROR_TEMPLATE =
      "Something went terrible wrong on importing the film list with the following location: \"%s\"";
  private static final String HTTP = "http";
  private static final String FILMLIST_JSON_DEFAULT_NAME = "filmliste.json";
  private static final String FILMLIST_JSON_COMPRESSED_DEFAULT_NAME =
      FILMLIST_JSON_DEFAULT_NAME + ".xz";
  private static final Logger LOG = LogManager.getLogger(CrawlerManager.class);
  private static CrawlerManager instance;
  private final MServerConfigDTO config;
  private final ForkJoinPool forkJoinPool;
  private final Filmlist filmlist;
  private final ExecutorService executorService;

  private final Map<Sender, AbstractCrawler> crawlerMap;

  private final FilmlistManager filmlistManager;
  private final Collection<ProgressListener> ftpProgressListeners;

  private CrawlerManager() {
    super();
    config = MServerConfigManager.getInstance().getConfig();

    executorService = Executors.newFixedThreadPool(config.getMaximumCpuThreads());
    forkJoinPool = new ForkJoinPool(config.getMaximumCpuThreads());

    crawlerMap = new EnumMap<>(Sender.class);
    filmlist = new Filmlist();
    filmlistManager = FilmlistManager.getInstance();
    ftpProgressListeners = new ArrayList<>();
    initializeCrawler();
  }

  public static CrawlerManager getInstance() {
    if (instance == null) {
      instance = new CrawlerManager();
    }
    return instance;
  }

  public boolean addAllFTPProgressListener(
      final Collection<? extends ProgressListener> aFTPProgressListener) {
    return ftpProgressListeners.addAll(aFTPProgressListener);
  }

  public boolean addFTPProgressListener(final ProgressListener aFTPProgressListener) {
    return ftpProgressListeners.add(aFTPProgressListener);
  }

  public Set<Sender> getAviableSenderToCrawl() {
    return new HashSet<>(crawlerMap.keySet());
  }

  /**
   * Imports the film list with the given {@link MServerConfigDTO#getFilmlistImportFormat()} of
   * {@link MServerConfigDTO#getFilmlistImportLocation()}.
   */
  public void importFilmlist() {
    if (checkConfigForFilmlistImport()) {
      importFilmlist(config.getFilmlistImportFormat(), config.getFilmlistImportLocation());
    }
  }

  /**
   * Imports the film list with the given {@link FilmlistFormats} and the given location.
   *
   * @param aFormat The{@link FilmlistFormats} to import.
   * @param aFilmlistLocation The given location from which to import. If it starts with
   *        <code>http</code> or <code>https</code> it tries to import from URL. Otherwise it tries
   *        to import from the given Location as a file path.
   */
  public void importFilmlist(final FilmlistFormats aFormat, final String aFilmlistLocation) {
    try {
      final Optional<Filmlist> importedFilmlist;
      if (aFilmlistLocation.startsWith(HTTP)) {
        importedFilmlist = importFilmListFromURl(aFormat, aFilmlistLocation);
      } else {
        importedFilmlist = importFilmlistFromFile(aFormat, aFilmlistLocation);
      }

      if (importedFilmlist.isPresent()) {
        filmlist.merge(importedFilmlist.get());
      }
    } catch (final IOException ioException) {
      LOG.fatal(String.format(FILMLIST_IMPORT_ERROR_TEMPLATE, aFilmlistLocation), ioException);
    }
  }

  /**
   * Saves the actual film list for each {@link FilmlistFormats} of
   * {@link MServerConfigDTO#getFilmlistSaveFormats()} in a file with the path of
   * {@link MServerConfigDTO#getFilmlistSavePaths()}.
   */
  public void saveFilmlist() {
    if (checkConfigForFilmlistSave()) {
      config.getFilmlistSaveFormats()
          .forEach(f -> saveFilmlist(Paths.get(config.getFilmlistSavePaths().get(f)), f));
    }
  }

  /**
   * Saves the actual film list with the given {@link FilmlistFormats} to the given path.
   *
   * @param aSavePath The path where to save the film list.
   * @param aFormat The {@link FilmlistFormats} in which the film list should be saved to.
   */
  public void saveFilmlist(final Path aSavePath, final FilmlistFormats aFormat) {
    final Path filteredSavePath = filterPath(aSavePath);

    Path filmlistFileSafePath;
    if (Files.isDirectory(filteredSavePath)) {
      if (FilmlistFormats.JSON.equals(aFormat) || FilmlistFormats.OLD_JSON.equals(aFormat)) {
        filmlistFileSafePath = filteredSavePath.resolve(FILMLIST_JSON_DEFAULT_NAME);
      } else {
        filmlistFileSafePath = filteredSavePath.resolve(FILMLIST_JSON_COMPRESSED_DEFAULT_NAME);
      }
    } else {
      filmlistFileSafePath = filteredSavePath;
    }

    if (Files.exists(filmlistFileSafePath.getParent())) {
      if (Files.isWritable(filmlistFileSafePath.getParent())) {
        filmlistManager.addAllMessageListener(messageListeners);
        filmlistManager.save(aFormat, filmlist, filmlistFileSafePath);

      } else {
        printMessage(ServerMessages.FILMLIST_SAVE_PATH_MISSING_RIGHTS,
            filmlistFileSafePath.toAbsolutePath().toString());
      }
    } else {
      printMessage(ServerMessages.FILMLIST_SAVE_PATH_INVALID,
          filmlistFileSafePath.toAbsolutePath().toString());
    }
  }

  /**
   * Runs all crawler and starts a timer for
   * {@link MServerConfigDTO#getMaximumServerDurationInMinutes()}.
   */
  public void start() {
    final TimeoutTask timeoutRunner = createTimeoutTask();

    if (config.getMaximumServerDurationInMinutes() != null
        && config.getMaximumServerDurationInMinutes() > 0) {
      timeoutRunner.start();
    }
    final Set<AbstractCrawler> crawlerToRun = getCrawlerToRun();
    runCrawlers(crawlerToRun.toArray(new AbstractCrawler[crawlerToRun.size()]));
    timeoutRunner.stopTimeout();
  }

  /**
   * Starts the crawler for the given {@link Sender}.<br>
   * <br>
   * <b>WARNING:</b> If no crawler is listed for {@link Sender} a {@link IllegalArgumentException}
   * will be thrown.<br>
   * You can ensure if there is a crawler for {@link Sender} with
   * {@link CrawlerManager#getAviableSenderToCrawl}.
   *
   * @param aSender The Sender which crawler to start.
   */
  public void startCrawlerForSender(final Sender... aSenders) {
    final Collection<AbstractCrawler> crawlers = new ArrayList<>();
    for (final Sender sender : aSenders) {

      if (crawlerMap.containsKey(sender)) {
        crawlers.add(crawlerMap.get(sender));
      } else {
        throw new IllegalArgumentException(String
            .format("There is no registered crawler for the Sender \"%s\"", sender.getName()));
      }
    }
    runCrawlers(crawlers.toArray(new AbstractCrawler[crawlers.size()]));
  }

  /**
   * This stops all running crawler.
   */
  public void stop() {
    forkJoinPool.shutdown();
    executorService.shutdown();
  }

  /**
   * Uploads, if enabled trough {@link MServerFTPSettings#getFtpEnabled()}, the film list with each
   * {@link MServerFTPSettings#getFtpTargetFilePaths()} to
   * {@link MServerFTPSettings#getFtpUrl()}.<br>
   * <br>
   * It will use the default port 21 or if set the port of {@link MServerFTPSettings#getFtpPort()}.
   * <br>
   * <br>
   * It will use the credentials {@link MServerFTPSettings#getFtpUsername()} with
   * {@link MServerFTPSettings#getFtpPassword()}.<br>
   * <br>
   * <b>WARNING:</b> It can only upload {@link FilmlistFormats} which are saved before and listed in
   * {@link MServerConfigDTO#getFilmlistSavePaths()}.
   */
  public void uploadFilmlist() {
    final MServerFTPSettings ftpSettings = config.getFtpSettings();
    if (ftpSettings.getFtpEnabled() != null && ftpSettings.getFtpEnabled()) {
      for (final Entry<FilmlistFormats, String> ftpFilePathsEntry : ftpSettings
          .getFtpTargetFilePaths().entrySet()) {
        FtpUploadTarget ftpUploadTarget;
        ftpUploadTarget =
            new FtpUploadTarget(ftpSettings.getFtpUrl(), ftpFilePathsEntry.getValue());

        if (ftpSettings.getFtpPort() != null) {
          ftpUploadTarget.setPort(Optional.of(ftpSettings.getFtpPort()));
        }

        if (ftpSettings.getFtpUsername() != null) {
          ftpUploadTarget.setUsername(Optional.of(ftpSettings.getFtpUsername()));
        }

        if (ftpSettings.getFtpPassword() != null) {
          ftpUploadTarget.setPassword(Optional.of(ftpSettings.getFtpPassword()));
        }

        uploadFilmlist(ftpFilePathsEntry.getKey(), ftpUploadTarget);
      }
    }
  }

  /**
   * Uploads the given {@link FilmlistFormats} whit the settings of the given
   * {@link FtpUploadTarget}.<br>
   * <br>
   * <b>WARNING:</b> It can only upload {@link FilmlistFormats} which are saved before and listed in
   * {@link MServerConfigDTO#getFilmlistSavePaths()}.<br>
   * <br>
   *
   * @param aFilmlistFormat The {@link FilmlistFormats} to upload.
   * @param aFtpUploadTarget The settings where to upload to of {@link FtpUploadTarget}.
   */
  public void uploadFilmlist(final FilmlistFormats aFilmlistFormat,
      final FtpUploadTarget aFtpUploadTarget) {
    if (config.getFilmlistSaveFormats().contains(aFilmlistFormat)) {
      uploadFilmlist(Paths.get(config.getFilmlistSavePaths().get(aFilmlistFormat)),
          aFtpUploadTarget);
    } else {
      printMessage(ServerMessages.FTP_FORMAT_NOT_IN_SAVE_FORMATS, aFilmlistFormat);
    }
  }

  /**
   * Uploads the file with the given path to the location set in the given {@link FtpUploadTarget}.
   *
   * @param aFilmlistPath The path of the file to upload.
   * @param aFtpUploadTarget The upload settings.
   */
  public void uploadFilmlist(final Path aFilmlistPath, final FtpUploadTarget aFtpUploadTarget) {
    final FtpUploadTask ftpUploadTask = new FtpUploadTask(aFilmlistPath, aFtpUploadTarget);
    ftpUploadTask.addAllMessageListener(messageListeners);
    ftpUploadTask.addAllProgressListener(ftpProgressListeners);
    executorService.execute(ftpUploadTask);
  }

  private boolean checkAllUsedFormatsHaveSavePaths() {
    final List<FilmlistFormats> missingSavePathFormats = config.getFilmlistSaveFormats().stream()
        .filter(f -> !config.getFilmlistSavePaths().containsKey(f)).collect(Collectors.toList());
    missingSavePathFormats.forEach(
        f -> printMessage(ServerMessages.NO_FILMLIST_SAVE_PATH_FOR_FORMAT_CONFIGURED, f.name()));
    return missingSavePathFormats.isEmpty();
  }

  private boolean checkConfigForFilmlistImport() {
    if (config.getFilmlistImportFormat() == null) {
      printMessage(ServerMessages.NO_FILMLIST_IMPORT_FORMAT_IN_CONFIG);
      return false;
    }

    if (config.getFilmlistImportLocation() == null) {
      printMessage(ServerMessages.NO_FILMLIST_IMPORT_LOCATION_IN_CONFIG);
      return false;
    }

    return true;
  }

  private boolean checkConfigForFilmlistSave() {
    if (config.getFilmlistSaveFormats().isEmpty()) {
      printMessage(ServerMessages.NO_FILMLIST_FORMAT_CONFIGURED);
      return false;
    }

    if (config.getFilmlistSavePaths().isEmpty()) {
      printMessage(ServerMessages.NO_FILMLIST_SAVE_PATHS_CONFIGURED);
      return false;
    }
    return checkAllUsedFormatsHaveSavePaths();
  }

  private boolean checkFilmlistImportFile(final Path aFilmlistPath) {
    if (Files.notExists(aFilmlistPath)) {
      printMessage(ServerMessages.FILMLIST_IMPORT_FILE_NOT_FOUND,
          aFilmlistPath.toAbsolutePath().toString());
      return false;
    }
    if (!Files.isReadable(aFilmlistPath)) {
      printMessage(ServerMessages.FILMLIST_IMPORT_FILE_NO_READ_PERMISSION,
          aFilmlistPath.toAbsolutePath().toString());
      return false;
    }
    return true;
  }

  private TimeoutTask createTimeoutTask() {
    return new TimeoutTask(config.getMaximumServerDurationInMinutes()) {
      @Override
      public void shutdown() {
        forkJoinPool.shutdownNow();
        printMessage(ServerMessages.SERVER_TIMEOUT);
      }
    };
  }

  private Path filterPath(final Path aSavePath) {
    return Paths.get(aSavePath.toString().replaceFirst(HOME_PATTERN, USER_HOME_PATH));
  }

  private Set<AbstractCrawler> getCrawlerToRun() {
    final Set<AbstractCrawler> crawlerToRun = new HashSet<>();

    if (config.getSenderIncluded().isEmpty()) {
      crawlerToRun.addAll(crawlerMap.values());
    } else {
      config.getSenderIncluded().stream().filter(crawlerMap::containsKey)
          .forEach(s -> crawlerToRun.add(crawlerMap.get(s)));
    }

    if (!config.getSenderExcluded().isEmpty()) {
      config.getSenderExcluded().stream().filter(crawlerMap::containsKey)
          .forEach(s -> crawlerToRun.remove(crawlerMap.get(s)));
    }

    return crawlerToRun;
  }

  private Optional<Filmlist> importFilmlistFromFile(final FilmlistFormats aFormat,
      final String aFilmlistLocation) throws IOException {
    final Path filmlistPath = Paths.get(aFilmlistLocation);
    if (checkFilmlistImportFile(filmlistPath)) {
      return filmlistManager.importList(aFormat, filmlistPath);
    }
    return Optional.empty();
  }

  private Optional<Filmlist> importFilmListFromURl(final FilmlistFormats aFormat,
      final String aFilmlistLocation) throws IOException {
    try {
      return filmlistManager.importList(aFormat, new URL(aFilmlistLocation));
    } catch (final MalformedURLException malformedURLException) {
      printMessage(ServerMessages.FILMLIST_IMPORT_URL_INVALID, aFilmlistLocation);
    }
    return Optional.empty();
  }

  private void initializeCrawler() {
    crawlerMap.put(Sender.ARD, new ArdCrawler(forkJoinPool, messageListeners, progressListeners));
    crawlerMap.put(Sender.BR, new BrCrawler(forkJoinPool, messageListeners, progressListeners));
    crawlerMap.put(Sender.DREISAT,
        new DreiSatCrawler(forkJoinPool, messageListeners, progressListeners));
    crawlerMap.put(Sender.FUNK, new FunkCrawler(forkJoinPool, messageListeners, progressListeners));
  }

  private void runCrawlers(final AbstractCrawler... aCrawlers) {
    try {
      final List<Future<Set<Film>>> results = executorService.invokeAll(Arrays.asList(aCrawlers));
      for (final Future<Set<Film>> result : results) {
        filmlist.addAllFilms(result.get());
      }
    } catch (ExecutionException | InterruptedException exception) {
      printMessage(ServerMessages.SERVER_ERROR);
      LOG.fatal("Something went wrong while exeuting the crawlers.", exception);
    }
  }

}
