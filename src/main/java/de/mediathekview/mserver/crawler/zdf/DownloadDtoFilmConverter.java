package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.zdf.json.DownloadDto;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import mServer.crawler.CrawlerTool;

public class DownloadDtoFilmConverter {
  public static void addUrlsToFilm(final Film aFilm, final DownloadDto aDownloadDto, final Optional<ZdfVideoUrlOptimizer> aUrlOptimizer) throws MalformedURLException {

    for (final Map.Entry<Resolution, String> qualitiesEntry : aDownloadDto.getDownloadUrls().entrySet()) {
      String url = qualitiesEntry.getValue();

      if (qualitiesEntry.getKey() == Resolution.NORMAL && aUrlOptimizer.isPresent()) {
        url = aUrlOptimizer.get().getOptimizedUrlNormal(url);
      }

      aFilm.addUrl(qualitiesEntry.getKey(), CrawlerTool.stringToFilmUrl(url));
    }

    if (!aFilm.hasHD() && aUrlOptimizer.isPresent()) {
      Optional<String> hdUrl = aUrlOptimizer.get().determineUrlHd(aFilm.getUrl(Resolution.NORMAL).toString());
      if (hdUrl.isPresent()) {
        aFilm.addUrl(Resolution.HD, CrawlerTool.stringToFilmUrl(hdUrl.get()));
      }
    }

    if (aDownloadDto.getSubTitleUrl().isPresent()) {
      aFilm.addSubtitle(new URL(aDownloadDto.getSubTitleUrl().get()));
    }

    if (aDownloadDto.getGeoLocation().isPresent()) {
      final Collection<GeoLocations> geo = new ArrayList<>();
      geo.add(aDownloadDto.getGeoLocation().get());
      aFilm.setGeoLocations(geo);
    }
  }
}
