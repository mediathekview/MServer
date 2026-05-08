package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.ard.ArdTopicInfoDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class ArdTopicCompilationDeserializerTest {

  @Test
  void deserialize(){
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_compilation_page.json");

    final ArdTopicCompilationDeserializer instance = new ArdTopicCompilationDeserializer();

    final ArdTopicInfoDto filmInfos = instance.deserialize(jsonElement, null, null);

    assertThat(filmInfos.getFilmInfos().size(), equalTo(24));
  }
}
