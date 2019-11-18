package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.tool.FileSizeDeterminer;
import de.mediathekview.mserver.crawler.zdf.json.DownloadDto;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class DownloadDtoFilmConverter {

  private DownloadDtoFilmConverter() {}

  public static void addUrlsToFilm(
      final Film aFilm,
      final DownloadDto downloadDto,
      final Optional<ZdfVideoUrlOptimizer> aUrlOptimizer,
      final String aLanguage)
      throws MalformedURLException {

    for (final Map.Entry<Resolution, String> qualitiesEntry :
        downloadDto.getDownloadUrls(aLanguage).entrySet()) {
      String url = qualitiesEntry.getValue();

      if (qualitiesEntry.getKey() == Resolution.NORMAL && aUrlOptimizer.isPresent()) {
        url = aUrlOptimizer.get().getOptimizedUrlNormal(url);
      }

      aFilm.addUrl(
          qualitiesEntry.getKey(),
          new FilmUrl(url, new FileSizeDeterminer(url).getFileSizeInMiB()));
    }

    if (!aFilm.hasHD() && aUrlOptimizer.isPresent()) {
      final Optional<String> hdUrl =
          aUrlOptimizer.get().determineUrlHd(aFilm.getUrl(Resolution.NORMAL).toString());
      if (hdUrl.isPresent()) {
        aFilm.addUrl(
            Resolution.HD,
            new FilmUrl(hdUrl.get(), new FileSizeDeterminer(hdUrl.get()).getFileSizeInMiB()));
      }
    }

    final Optional<String> subtitleUrl = downloadDto.getSubTitleUrl();
    if (subtitleUrl.isPresent()) {
      aFilm.addSubtitle(new URL(subtitleUrl.get()));
    }

    final Optional<GeoLocations> geoLocation = downloadDto.getGeoLocation();
    if (geoLocation.isPresent()) {
      final Collection<GeoLocations> geo = new ArrayList<>();
      geo.add(geoLocation.get());
      aFilm.setGeoLocations(geo);
    }
  }
}
