package mServer.crawler.sender.arte;

import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import mServer.crawler.sender.newsearch.GeoLocations;
import mServer.test.JsonFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ArteVideoDetailsDeserializerTest {
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {   
            { "/arte/arte_video_details_first_several_minors_geo_defr.json", "2017-07-10T18:15:00Z", GeoLocations.GEO_DE_FR },
            { "/arte/arte_video_details_several_majors_minors_geo_null.json", "2017-08-01T16:25:00Z", GeoLocations.GEO_NONE },
            { "/arte/arte_video_details_first_with_catchuprights_past_geo_sat.json", "2017-05-22T11:36:00Z", GeoLocations.GEO_EU },
            { "/arte/arte_video_details_first_without_catchuprights_geo_eudefr.json", "2017-07-16T23:30:00Z", GeoLocations.GEO_DE_FR },
            { "/arte/arte_video_details_no_broadcastprogrammings_geo_all.json", "2017-06-30T13:00:00Z", GeoLocations.GEO_NONE },
        });
    }
    
    private final String jsonFile;
    private final String expectedBroadcastBegin;
    private final GeoLocations geo;

    public ArteVideoDetailsDeserializerTest(String aJsonFile, String aExpectedBroadcastBegin, GeoLocations aGeo) {
        this.jsonFile = aJsonFile;
        this.expectedBroadcastBegin = aExpectedBroadcastBegin;
        this.geo = aGeo;
    }
    
    @Test
    public void testDeserialize() {
        
        JsonObject jsonObject = JsonFileReader.readJson(jsonFile);
        
        Calendar today = Calendar.getInstance();
        today.set(2017, 6, 11); // 11.07.2017 als heute verwenden
        
        ArteVideoDetailsDeserializer target = new ArteVideoDetailsDeserializer(today);
        ArteVideoDetailsDTO actual = target.deserialize(jsonObject, ArteVideoDetailsDTO.class, null);
        
        assertThat(actual, notNullValue());
        assertThat(actual.getBroadcastBegin(), equalTo(expectedBroadcastBegin));
        assertThat(actual.getGeoLocation(), equalTo(geo));
    }
    
}
