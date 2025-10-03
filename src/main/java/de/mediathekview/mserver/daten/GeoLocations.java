package de.mediathekview.mserver.daten;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * The available GEO locations.
 */
public enum GeoLocations {
  GEO_NONE("", "WELT", "none", "world", "ALL", ""), // nur in .. zu sehen
  GEO_DE("DE"),
  GEO_AT("AT"),
  GEO_CH("CH"),
  GEO_EU("EU"),
  GEO_DE_FR("DE-FR", "DE_FR"),
  GEO_DE_AT_CH("DE-AT-CH", "dach"),
  GEO_DE_AT_CH_EU("DE-AT-CH-EU", "SAT", "EBU"),
  GEO_DE_AT_CH_FR("DE-AT-CH-FR", "EUR_DE_FR");

  private final String description;
  private String[] alternatives;

  GeoLocations(final String aDescription, final String... aAlternatives) {
    description = aDescription;
    alternatives = aAlternatives;
  }

  /**
   * Finds a GeoLocation based on its description and alternatives.
   *
   * @param aTerm A term like the description or one of the alternatives.
   * @return The GeoLocation if found or else an empty Optional.
   */
  public static Optional<GeoLocations> find(final String aTerm) {
    for (final GeoLocations geoLoc : GeoLocations.values()) {
      if (geoLoc.getDescription().equalsIgnoreCase(aTerm)
          || StringUtils.equalsAnyIgnoreCase(aTerm, geoLoc.alternatives)) {
        return Optional.of(geoLoc);
      }
    }
    return Optional.empty();
  }

  public static GeoLocations getFromDescription(final String aDescription) {
    return find(aDescription).orElse(GEO_NONE);
  }

  public String getDescription() {
    return description;
  }
}
