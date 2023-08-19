package de.mediathekview.mserver.crawler.basic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.tool.MVHttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class IgnoreFilmFilter {
  private static final Logger LOG = LogManager.getLogger(IgnoreFilmFilter.class);
  private List<String> ignoreFilmTitles = new ArrayList<>();
  
  public IgnoreFilmFilter(String configFileNameAndPath) {
    try {
      if (configFileNameAndPath.toLowerCase().startsWith("http")) {
        ignoreFilmTitles = read(new URL(configFileNameAndPath));
      } else {
        ignoreFilmTitles = read(configFileNameAndPath);
      }
      LOG.debug("ignoreFilmList setup with {} entries", size());
    } catch (IOException e) {
      LOG.error("Could not read ignorefilmlist from {} ",configFileNameAndPath, e);
    }
  }
  
  public boolean ignoreFilm(final Film film) {
    for (String title : ignoreFilmTitles) {
      if (film.getTitel().toUpperCase().contains(title.toUpperCase())) {
        LOG.debug("Ignore Film: {} {} {}", film.getSenderName(), film.getThema(), film.getTitel());
        return true;
      }
    }
    return false;
  }
  
  public int size() {
    return ignoreFilmTitles.size();
  }

  private List<String> read(final URL aUrl)
      throws IOException {
    final Request request = new Request.Builder().url(aUrl).build();
    final OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();

    try (final Response response = httpClient.newCall(request).execute()) {
      final ResponseBody responseBody = response.body();
      if (responseBody == null) {
        return new ArrayList<>();
      }
      try (final BufferedReader reader = new BufferedReader(responseBody.charStream())){
        return readIgnoreList( reader);
      }
    }
  }
  
  private List<String> read(final String aFilePath) throws IOException {
    if (getClass().getClassLoader().getResourceAsStream(aFilePath) != null) {
      try (final InputStream is = getClass().getClassLoader().getResourceAsStream(aFilePath);
           final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
          final BufferedReader reader = new BufferedReader(isr)) {
        return readIgnoreList(reader);
      }
    } else {
      try (final FileReader fr = new FileReader(aFilePath);
         final BufferedReader reader = new BufferedReader(fr)) {
       return readIgnoreList(reader);
     }
    }
  }
  
  private List<String> readIgnoreList(BufferedReader is) throws IOException {
    List<String> listOfTitles = new ArrayList<>();
    String line = "";
    while ((line = is.readLine()) != null) {
      if (line.trim().length() > 0) {
        listOfTitles.add(line.trim());
      }
    }
    return listOfTitles;
  }
}
