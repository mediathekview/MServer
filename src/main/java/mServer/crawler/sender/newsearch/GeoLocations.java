package mServer.crawler.sender.newsearch;

/**
 * The available GEO locations.
 */
public enum GeoLocations
{
    GEO_NONE(""), // nur in .. zu sehen
    GEO_DE("DE"),
    GEO_AT("AT"),
    GEO_CH("CH"),
    GEO_EU("EU"),
    GEO_WELT("WELT"),
    GEO_DE_AT_CH("DE-AT-CH"),
    GEO_DE_AT_CH_EU("DE-AT-CH-EU");

    private final String description;

    GeoLocations(String aDescription)
    {
        description = aDescription;
    }

    public String getDescription()
    {
        return description;
    }
}
