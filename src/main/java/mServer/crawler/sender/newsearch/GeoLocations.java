package mServer.crawler.sender.newsearch;

/**
 * The available GEO locations.
 */
public enum GeoLocations
{
    GEO_DE("DE"), // nur in .. zu sehen
    GEO_AT("AT"),
    GEO_CH("CH"),
    GEO_EU("EU"),
    GEO_WELT("WELT");

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
