package de.mediathekview.mserver.crawler.wdr.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDto;
import de.mediathekview.mserver.testhelper.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class WdrLetterPageDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/wdr/wdr_letter_a.html",
            new WdrTopicUrlDto[] {
              new WdrTopicUrlDto(
                  "Abenteuer Erde",
                  "https://www1.wdr.de/mediathek/video/sendungen/abenteuer-erde/abenteuer-erde-104.html",
                  false),
              new WdrTopicUrlDto(
                  "Aktuelle Stunde",
                  "https://www1.wdr.de/mediathek/video/sendungen/aktuelle-stunde/video-aktuelle-stunde-2430.html",
                  true),
              new WdrTopicUrlDto(
                  "Annemie Hülchrath trifft Prominente!",
                  "https://www1.wdr.de/mediathek/video/sendungen/comedy/annemie-kommt-uebersicht-100.html",
                  false),
              new WdrTopicUrlDto(
                  "Lokalzeit aus Aachen",
                  "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/index.html",
                  false),
              new WdrTopicUrlDto(
                  "Ausgerechnet",
                  "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/index.html",
                  false)
            }
          },
          {"/wdr/wdr_letter_empty.html", new WdrTopicUrlDto[0]}
        });
  }

  private final String htmlFile;
  private final WdrTopicUrlDto[] expectedUrls;

  public WdrLetterPageDeserializerTest(
      final String aHtmlFile, final WdrTopicUrlDto[] aExpectedUrls) {
    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
  }

  @Test
  public void deserializeTest() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);

    WdrLetterPageDeserializer target = new WdrLetterPageDeserializer();
    final List<WdrTopicUrlDto> actual = target.deserialize(document);

    assertThat(actual.size(), equalTo(expectedUrls.length));
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
