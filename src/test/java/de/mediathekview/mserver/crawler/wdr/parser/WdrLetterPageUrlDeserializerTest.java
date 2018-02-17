package de.mediathekview.mserver.crawler.wdr.parser;

import de.mediathekview.mserver.testhelper.FileReader;
import java.io.IOException;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class WdrLetterPageUrlDeserializerTest {

  @Test
  public void deserializeTest() throws IOException {
    final String htmlContent = FileReader.readFile("/wdr/wdr_letter_a.html");
    final Document document = Jsoup.parse(htmlContent);

    String[] expectedUrls = {
      "https://www1.wdr.de/mediathek/video/sendungen-a-z/sendungen-b-102.html",
      "https://www1.wdr.de/mediathek/video/sendungen-a-z/sendungen-c-102.html",
      "https://www1.wdr.de/mediathek/video/sendungen-a-z/sendungen-d-102.html",
      "https://www1.wdr.de/mediathek/video/sendungen-a-z/sendungen-e-102.html",
      "https://www1.wdr.de/mediathek/video/sendungen-a-z/sendungen-f-102.html",
    };
    
    WdrLetterPageUrlDeserializer target = new WdrLetterPageUrlDeserializer();
    final List<String> actual = target.deserialize(document);
    
    assertThat(actual.size(), equalTo(expectedUrls.length));
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
