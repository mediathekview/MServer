package mServer.tool;

import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import mServer.crawler.sender.newsearch.Qualities;

/**
 * A test class for the util class {@link M3U8Utils}.
 */
public class M3U8UtilsTest
{

    public static final String TEST_URL_POSITIV_MORE_THEN_THREE_ELEMENTS = "http://adaptiv.wdr.de/i/medp/ww/fsk0/131/1315091/,1315091_15301367,1315091_15301368,1315091_15301370,1315091_15301371,1315091_15301369,.mp4.csmil/master.m3u8";
    public static final String TEST_URL_POSITIV_THREE_ELEMENTS = "http://adaptiv.wdr.de/i/medp/ww/fsk0/131/1315091/,1315091_15301370,1315091_15301371,1315091_15301369,.mp4.csmil/master.m3u8";
    public static final String TEST_URL_POSITIV_TWO_ELEMENTS = "http://adaptiv.wdr.de/i/medp/ww/fsk0/131/1315091/,1315091_15301370,1315091_15301371,.mp4.csmil/master.m3u8";
    public static final String TEST_URL_POSITIV_ONE_ELEMENT = "http://adaptiv.wdr.de/i/medp/ww/fsk0/131/1315091/,1315091_15301370,.mp4.csmil/master.m3u8";

    public static final String TEST_URL_NEGATIV_NONE_ELEMENT = "http://adaptiv.wdr.de/i/medp/ww/fsk0/131/1315091/,,.mp4.csmil/master.m3u8";
    public static final String TEST_URL_NEGATIV_WRONG_URL = "http://adaptiv.example.org/i/medp/123/was/ist/denn/hierlos/master.m3u8";

    public static final String AWAITED_URL_SMALL = "http://ondemand-ww.wdr.de/medp/fsk0/131/1315091/1315091_15301370.mp4";
    public static final String AWAITED_URL_NORMAL = "http://ondemand-ww.wdr.de/medp/fsk0/131/1315091/1315091_15301371.mp4";
    public static final String AWAITED_URL_HD = "http://ondemand-ww.wdr.de/medp/fsk0/131/1315091/1315091_15301369.mp4";

    /**
     * Tests {@link M3U8Utils#gatherUrlsFromWdrM3U8(String)} with a URL with more then three qualities.
     */
    @Test
    public void testGatherUrlsFromWDRM3U8_MoreThenThree_Positiv()
    {
        Map<Qualities, String> qualitiesAndUrls = M3U8Utils.gatherUrlsFromWdrM3U8(TEST_URL_POSITIV_MORE_THEN_THREE_ELEMENTS);
        Assert.assertThat(qualitiesAndUrls.get(Qualities.SMALL), CoreMatchers.is(AWAITED_URL_SMALL));
        Assert.assertThat(qualitiesAndUrls.get(Qualities.NORMAL), CoreMatchers.is(AWAITED_URL_NORMAL));
        Assert.assertThat(qualitiesAndUrls.get(Qualities.HD), CoreMatchers.is(AWAITED_URL_HD));
    }

    /**
     * Tests {@link M3U8Utils#gatherUrlsFromWdrM3U8(String)} with a URL with three qualities.
     */
    @Test
    public void testGatherUrlsFromWDRM3U8_ExactThree_Positiv()
    {
        Map<Qualities, String> qualitiesAndUrls = M3U8Utils.gatherUrlsFromWdrM3U8(TEST_URL_POSITIV_THREE_ELEMENTS);
        Assert.assertThat(qualitiesAndUrls.get(Qualities.SMALL), CoreMatchers.is(AWAITED_URL_SMALL));
        Assert.assertThat(qualitiesAndUrls.get(Qualities.NORMAL), CoreMatchers.is(AWAITED_URL_NORMAL));
        Assert.assertThat(qualitiesAndUrls.get(Qualities.HD), CoreMatchers.is(AWAITED_URL_HD));
    }

    /**
     * Tests {@link M3U8Utils#gatherUrlsFromWdrM3U8(String)} with a URL with only two qualities.
     */
    @Test
    public void testGatherUrlsFromWDRM3U8_ExactTwo_Positiv()
    {
        Map<Qualities, String> qualitiesAndUrls = M3U8Utils.gatherUrlsFromWdrM3U8(TEST_URL_POSITIV_TWO_ELEMENTS);
        Assert.assertThat(qualitiesAndUrls.get(Qualities.SMALL), CoreMatchers.is(AWAITED_URL_SMALL));
        Assert.assertThat(qualitiesAndUrls.get(Qualities.NORMAL), CoreMatchers.is(AWAITED_URL_NORMAL));
    }

    /**
     * Tests {@link M3U8Utils#gatherUrlsFromWdrM3U8(String)} with a URL with only one quality.
     */
    @Test
    public void testGatherUrlsFromWDRM3U8_ExactOne_Positiv()
    {
        Map<Qualities, String> qualitiesAndUrls = M3U8Utils.gatherUrlsFromWdrM3U8(TEST_URL_POSITIV_ONE_ELEMENT);
        Assert.assertThat(qualitiesAndUrls.get(Qualities.SMALL), CoreMatchers.is(AWAITED_URL_SMALL));
    }

    /**
     * Tests {@link M3U8Utils#gatherUrlsFromWdrM3U8(String)} with a URL without a quality.
     */
    @Test
    public void testGatherUrlsFromWDRM3U8_NoneElement_Negativ()
    {
        Map<Qualities, String> qualitiesAndUrls = M3U8Utils.gatherUrlsFromWdrM3U8(TEST_URL_NEGATIV_NONE_ELEMENT);
        Assert.assertThat(qualitiesAndUrls.isEmpty(), CoreMatchers.is(true));
    }

    /**
     * Tests {@link M3U8Utils#gatherUrlsFromWdrM3U8(String)} with a wrong URL.
     */
    @Test
    public void testGatherUrlsFromWDRM3U8_WrongUrl_Negativ()
    {
        Map<Qualities, String> qualitiesAndUrls = M3U8Utils.gatherUrlsFromWdrM3U8(TEST_URL_NEGATIV_WRONG_URL);
        Assert.assertThat(qualitiesAndUrls.isEmpty(), CoreMatchers.is(true));
    }
}
