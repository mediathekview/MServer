package mServer.crawler.sender.zdf;

import de.mediathekview.mlib.daten.DatenFilm;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import mServer.crawler.sender.newsearch.GeoLocations;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.zdf.json.DownloadDto;

public class DownloadDtoFilmConverter {

  private DownloadDtoFilmConverter() {
  }

  public static void addUrlsToFilm(
          final DatenFilm aFilm,
          final DownloadDto downloadDto,
          final Optional<ZdfVideoUrlOptimizer> aUrlOptimizer,
          final String aLanguage)
          throws MalformedURLException {

    for (final Map.Entry<Qualities, String> qualitiesEntry
            : downloadDto.getDownloadUrls(aLanguage).entrySet()) {
      String url = qualitiesEntry.getValue();

      if (qualitiesEntry.getKey() == Qualities.NORMAL && aUrlOptimizer.isPresent()) {
        url = aUrlOptimizer.get().getOptimizedUrlNormal(url);
      }

      aFilm.addUrl(
              qualitiesEntry.getKey(),
              new FilmUrl(url, new FileSizeDeterminer(url).getFileSizeInMiB()));
    }

    if (!aFilm.hasHD() && aUrlOptimizer.isPresent()) {
      final Optional<String> hdUrl
              = aUrlOptimizer.get().determineUrlHd(aFilm.getUrl(Qualities.NORMAL).toString());
      if (hdUrl.isPresent()) {
        aFilm.addUrl(
                Qualities.HD,
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
