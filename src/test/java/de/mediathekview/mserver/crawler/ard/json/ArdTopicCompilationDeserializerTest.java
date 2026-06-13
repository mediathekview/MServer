package de.mediathekview.mserver.crawler.ard.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.ard.ArdTopicInfoDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.jupiter.api.Test;

class ArdTopicCompilationDeserializerTest {

  @Test
  void deserialize() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_compilation_page.json");

    final ArdTopicCompilationDeserializer instance = new ArdTopicCompilationDeserializer();

    final ArdTopicInfoDto filmInfos = instance.deserialize(jsonElement, null, null);

    assertThat(filmInfos.getFilmInfos().size(), equalTo(24));
    assertThat(filmInfos.getPageSize(), equalTo(24));
    assertThat(filmInfos.getTotalElements(), equalTo(202));
    assertThat(filmInfos.getPageNumber(), equalTo(0));
  }

  @Test
  void deserializeSubPage() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_compilation_subpage.json");
    final ArdTopicCompilationDeserializer instance = new ArdTopicCompilationDeserializer();

    final ArdTopicInfoDto filmInfos = instance.deserialize(jsonElement, null, null);

    assertThat(filmInfos.getFilmInfos().size(), equalTo(12));
    assertThat(filmInfos.getPageSize(), equalTo(24));
    assertThat(filmInfos.getTotalElements(), equalTo(108));
    assertThat(filmInfos.getPageNumber(), equalTo(4));
  }
}
