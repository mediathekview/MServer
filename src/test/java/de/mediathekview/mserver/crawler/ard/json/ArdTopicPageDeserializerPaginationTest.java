package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.ard.ArdTopicInfoDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArdTopicPageDeserializerPaginationTest {
  @Test
  public void testDeserializePagination() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_topic_pagination.json");

    final ArdTopicPageDeserializer instance = new ArdTopicPageDeserializer();

    final ArdTopicInfoDto ardTopicInfoDto = instance.deserialize(jsonElement, null, null);

    assertThat(ardTopicInfoDto.getSubPageNumber(), is(0));
    assertThat(ardTopicInfoDto.getMaxSubPageNumber(), is(5));
  }
}
