package de.mediathekview.mserver.crawler.zdf.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.zdf.ZdfFilmDto;
import de.mediathekview.mserver.crawler.zdf.ZdfPubFormResult;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.HashMap;
import java.util.Map;
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

  @Test
  void testFilmPageSenderCorrect() {
    final JsonObject json = JsonFileReader.readJson("/zdf/zdf_pub_form_page_filme.json");

    final Map<String, Sender> expectedSenders = new HashMap<>();
    expectedSenders.put("A Bigger Splash", Sender.ZDF);
    expectedSenders.put("2 Guns", Sender.ZDF);
    expectedSenders.put("Hollow Man - Unsichtbare Gefahr", Sender.ZDF_NEO);
    expectedSenders.put("Gattaca", Sender.ZDF_NEO);
    expectedSenders.put("OSS 117 - Liebesgrüße aus Afrika", Sender.ZDF);
    expectedSenders.put("Nicht schuldig", Sender.ZDF_NEO);
    expectedSenders.put("Die Agentin", Sender.ZDF);
    expectedSenders.put("Stand by Me - Das Geheimnis eines Sommers", Sender.ZDF_NEO);
    expectedSenders.put("La Maison - Haus der Lust", Sender.ZDF);
    expectedSenders.put("Paradise Highway - Straße der Angst", Sender.ZDF);
    expectedSenders.put("Rogue Agent - Er liebt nur dich", Sender.ZDF);
    expectedSenders.put("Mortal - Mut ist unsterblich", Sender.ZDF);
    expectedSenders.put("The Commuter - Die Fremde im Zug", Sender.ZDF);
    expectedSenders.put("Verschwunden auf Sardinien", Sender.ZDF);
    expectedSenders.put("Halt nicht an!", Sender.ZDF);
    expectedSenders.put("The break-up club", Sender.ZDF);
    expectedSenders.put("Maigret und das tote Mädchen", Sender.ZDF);
    expectedSenders.put("Aline - The Voice of Love", Sender.ZDF);
    expectedSenders.put("Black Box - Gefährliche Wahrheit", Sender.ZDF);
    expectedSenders.put("Blutroter Sommer", Sender.ZDF);
    expectedSenders.put("Marinette - Kämpferin. Fußballerin. Legende.", Sender.ZDF);
    expectedSenders.put("Freies Land", Sender.ZDF);
    expectedSenders.put("Geheimsache Malta", Sender.ZDF);

    final ZdfPubFormResult actual = target.deserialize(json, Set.class, null);

    assertThat(actual, notNullValue());
    assertEquals(0, actual.getTopics().getElements().size());
    assertEquals(expectedSenders.size(), actual.getFilms().size());
    expectedSenders.forEach((expectedTitle, expectedSender) -> {
      final Optional<Sender> actualSender = actual.getFilms().stream()
          .filter(film -> film.getTitle().equalsIgnoreCase(expectedTitle))
          .map(ZdfFilmDto::getSender)
          .findFirst();
      assertTrue(actualSender.isPresent(), "Film not found: " + expectedTitle);
      assertEquals(expectedSender, actualSender.get(), "Sender mismatch for film: " + expectedTitle);
    });
    assertEquals(
        Optional.of(
            "MmYzMzlmYWUtMGJlNi00NmIzLWI1NDctNzVlY2QwZTRhM2NiLW1vdmll"),
        actual.getTopics().getNextPage());
  }
}
