package mServer.developTest.crawler.sender.arte;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.gson.JsonObject;

import de.mediathekview.mlib.daten.GeoLocations;
import mServer.crawler.sender.arte.ArteVideoDetailsDTO;
import mServer.crawler.sender.arte.ArteVideoDetailsDeserializer;
import mServer.developTest.test.JsonFileReader;

@RunWith(Parameterized.class)
public class ArteVideoDetailsDeserializerTest
{
    private static final DateTimeFormatter broadcastDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");// 2016-10-29T16:15:00Z

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]
        {
                { "/arte/arte_video_details_first_several_minors_geo_defr.json", "2017-07-10T18:15:00Z",
                        GeoLocations.GEO_DE_FR },
                { "/arte/arte_video_details_several_majors_minors_geo_null.json", "2017-08-01T16:25:00Z",
                        GeoLocations.GEO_NONE },
                { "/arte/arte_video_details_first_with_catchuprights_past_geo_sat.json", "2017-05-22T11:36:00Z",
                        GeoLocations.GEO_DE_AT_CH_EU },
                { "/arte/arte_video_details_first_without_catchuprights_geo_eudefr.json", "2017-07-16T23:30:00Z",
                        GeoLocations.GEO_DE_FR },
                { "/arte/arte_video_details_no_broadcastprogrammings_geo_all.json", "2017-06-30T13:00:00Z",
                        GeoLocations.GEO_NONE }, });
    }

    private final String jsonFile;
    private final GeoLocations geo;
    private final LocalDateTime expectedBroadcastBegin;

    public ArteVideoDetailsDeserializerTest(final String aJsonFile, final LocalDateTime aExpectedBroadcastBegin,
            final GeoLocations aGeo)
    {
        jsonFile = aJsonFile;
        expectedBroadcastBegin = aExpectedBroadcastBegin;
        geo = aGeo;
    }

    @Test
    public void testDeserialize()
    {

        final JsonObject jsonObject = JsonFileReader.readJson(jsonFile);

        final ArteVideoDetailsDeserializer target = new ArteVideoDetailsDeserializer();
        final ArteVideoDetailsDTO actual = target.deserialize(jsonObject, ArteVideoDetailsDTO.class, null);

        assertThat(actual, notNullValue());
        assertThat(actual.getBroadcastBegin(), equalTo(expectedBroadcastBegin));
        assertThat(actual.getGeoLocation(), equalTo(geo));
    }

}
