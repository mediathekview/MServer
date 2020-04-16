package mServer.crawler.sender.newsearch;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * The available GEO locations.
 */
public enum GeoLocations
{
    GEO_NONE("", "WELT", "none", "world", "ALL", ""), // nur in .. zu sehen
    GEO_DE("DE"),
    GEO_AT("AT"),
    GEO_CH("CH"),
    GEO_EU("EU", "EUR_DE_FR"),
    GEO_DE_FR("DE-FR", "DE_FR"),
    GEO_DE_AT_CH("DE-AT-CH", "dach"),
    GEO_DE_AT_CH_EU("DE-AT-CH-EU", "SAT", "EBU");

    private final String description;
    private final String[] alternatives;

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

    public String getDescription()
    {
        return description;
    }
}
