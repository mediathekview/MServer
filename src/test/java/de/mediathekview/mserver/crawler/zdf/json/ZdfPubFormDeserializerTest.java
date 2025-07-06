package de.mediathekview.mserver.crawler.zdf.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.zdf.ZdfPubFormResult;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ZdfPubFormDeserializerTest {
  private final ZdfPubFormDeserializer target = new ZdfPubFormDeserializer();

  @Test
  void testFirstPage() {
    final JsonObject json = JsonFileReader.readJson("/zdf/zdf_pub_form_page1.json");

    final ZdfPubFormResult actual = target.deserialize(json, Set.class, null);

    assertThat(actual, notNullValue());
    assertEquals(45, actual.getTopics().getElements().size());
    assertEquals(2, actual.getFilms().size());
    assertEquals(Optional.of("cDEyX3NlbmRlYmVyZWljaF81OTg4NDE2"), actual.getTopics().getNextPage());
  }

  @Test
  void testFilmsPage() {
    final JsonObject json = JsonFileReader.readJson("/zdf/zdf_pub_form_page_single_films.json");

    final ZdfPubFormResult actual = target.deserialize(json, Set.class, null);

    assertThat(actual, notNullValue());
    assertEquals(0, actual.getTopics().getElements().size());
    assertEquals(19, actual.getFilms().size());
    assertEquals(
        Optional.of(
            "aW5kZXgtcGFnZS1hcmQtY29sbGVjdGlvbl9hcmRfZHhqdW9tZnl6ZHB6YWc5M29qZm16amF5bmRqa3ltaXptZHU0eW1xLQ=="),
        actual.getTopics().getNextPage());
  }
}
