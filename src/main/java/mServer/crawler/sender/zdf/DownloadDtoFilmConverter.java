package mServer.crawler.sender.zdf;

import mServer.crawler.sender.newsearch.Qualities;

import java.util.Map;
import java.util.Optional;

public class DownloadDtoFilmConverter {

  private DownloadDtoFilmConverter() {
  }

  public static void getOptimizedUrls(
    final Map<Qualities, String> downloadUrls,
    final Optional<ZdfVideoUrlOptimizer> aUrlOptimizer) {

    for (final Map.Entry<Qualities, String> qualitiesEntry
      : downloadUrls.entrySet()) {
      String url = qualitiesEntry.getValue();
//@TODO gibt es auch HD-URL optimieren in bisheriger Lösung?

      if (qualitiesEntry.getKey() == Qualities.NORMAL && aUrlOptimizer.isPresent()) {
        url = aUrlOptimizer.get().getOptimizedUrlNormal(url);
        qualitiesEntry.setValue(url);
      }
    }

    if (!downloadUrls.containsKey(Qualities.HD) && aUrlOptimizer.isPresent()) {
      final Optional<String> hdUrl
        = aUrlOptimizer.get().determineUrlHd(downloadUrls.get(Qualities.NORMAL));
      if (hdUrl.isPresent()) {
        downloadUrls.put(Qualities.HD, hdUrl.get());
      }
    }
  }
}
