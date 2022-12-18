package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SrfScheduleDeserializerTest {
  @Test
  public void test() {

    final JsonElement jsonElement = JsonFileReader.readJson("/srf/srf_schedule_page_1.json");

    final SrfScheduleDeserializer target = new SrfScheduleDeserializer();
    final Set<CrawlerUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.size(), equalTo(48));
  }

}
