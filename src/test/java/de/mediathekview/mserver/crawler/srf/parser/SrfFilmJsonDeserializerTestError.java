package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.srf.tasks.SrfTaskTestBase;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/** Tests error scenarios of SrfFilmJsonDeserializer */
public class SrfFilmJsonDeserializerTestError extends SrfTaskTestBase {

  @Test
  public void testDrmUrl() {
    final JsonElement jsonElement = JsonFileReader.readJson("/srf/srf_film_page_drm.json");

    final SrfFilmJsonDeserializer target = new SrfFilmJsonDeserializer(createCrawler());
    final Optional<Film> actual = target.deserialize(jsonElement, Film.class, null);

    assertThat(actual.isPresent(), equalTo(false));
  }

  @Test
  public void testFilmUrlBlocked() {
    setupResponseWithoutBody(
        "/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/master.m3u8?start=0.0&end=3305.1",
        403);

    final JsonElement jsonElement = JsonFileReader.readJson("/srf/srf_film_page1.json");

    final SrfFilmJsonDeserializer target = new SrfFilmJsonDeserializer(createCrawler());
    final Optional<Film> actual = target.deserialize(jsonElement, Film.class, null);

    assertThat(actual.isPresent(), equalTo(false));
  }

  @Test
  public void testFilmBlocked() {
    final JsonElement jsonElement = JsonFileReader.readJson("/srf/srf_film_page_geo_block.json");

    final SrfFilmJsonDeserializer target = new SrfFilmJsonDeserializer(createCrawler());
    final Optional<Film> actual = target.deserialize(jsonElement, Film.class, null);

    assertThat(actual.isPresent(), equalTo(false));
  }
}
