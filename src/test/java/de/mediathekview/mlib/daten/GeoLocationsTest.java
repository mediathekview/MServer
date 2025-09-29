package de.mediathekview.mlib.daten;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GeoLocationsTest {

  @ParameterizedTest
  @CsvSource({"SAT", "EBU", "ebu"})
  void testDiffernetPresentGeolocationsAreFound(String searchTerm) {
    Optional<GeoLocations> actual = GeoLocations.find(searchTerm);

    assertThat(actual).isPresent()
            .contains(GeoLocations.GEO_DE_AT_CH_EU);
  }

}
