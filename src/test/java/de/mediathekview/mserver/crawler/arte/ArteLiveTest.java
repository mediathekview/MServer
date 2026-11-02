package de.mediathekview.mserver.crawler.arte;

import com.google.common.util.concurrent.RateLimiter;
import de.mediathekview.mserver.daten.Sender;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in, read-only smoke check against the current ARTE APIs. */
@EnabledIfSystemProperty(named = "arte.live", matches = "true")
class ArteLiveTest {
  @ParameterizedTest
  @CsvSource({"de,ARTE_DE", "fr,ARTE_FR", "en,ARTE_EN", "es,ARTE_ES", "it,ARTE_IT", "pl,ARTE_PL"})
  void currentCatalogueProducesFilms(String language, Sender sender) throws IOException {
    var errors = new ArrayList<Exception>();
    try (var http = new ArteHttpClient(20, RateLimiter.create(4))) {
      var api = new ArteCatalogue(language, sender, 1, http::get, errors::add);
      var programmes = api.discover();
      assertFalse(programmes.isEmpty(), "Live EMAC catalogue must discover programmes");
      int films = 0;
      for (var programme : programmes.stream().limit(3).toList()) {
        for (var film : api.films(programme)) {
          assertFalse(film.getUrls().isEmpty());
          assertTrue(film.getDefaultUrl().orElseThrow().getUrl().getPath().endsWith(".m3u8"));
          assertTrue(http.get(film.getDefaultUrl().orElseThrow().getUrl().toString()).startsWith("#EXTM3U"));
          films++;
        }
      }
      assertTrue(films > 0, "Live player API must yield playable films");
      assertTrue(errors.isEmpty(), errors.toString());
      System.out.printf("ARTE %s live: %d unique programmes discovered, %d films resolved from first three.%n", language, programmes.size(), films);
    }
  }
}
