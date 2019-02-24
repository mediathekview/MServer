package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore(value= {"javax.net.ssl.*", "javax.*", "com.sun.*", "org.apache.logging.log4j.core.config.xml.*"})
public class OrfArchiveTopicTaskTest extends OrfTaskTestBase {
  
  @Test
  public void test() throws IOException {
    final String requestUrl = "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430";
    final String topic = "Die Geschichte des Burgenlands";
    
    JsoupMock.mock(requestUrl, "/orf/orf_archive_topic.html");
    
    TopicUrlDTO[] expected = new TopicUrlDTO[] {
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Die-Fuersten-Esterhazy/9061529"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Vertrieben-und-vergessen/3230849"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Festspielland-Burgenland/9079835"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Zwischen-Idylle-und-Aufbruch/9057246"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Der-Wein-Kult-und-Faszination/3230931"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Gedenkfeier-in-Mauthausen/8949048"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/20-Jahre-Attentat-in-Oberwart/9235812"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Giganten-im-Wind/3229223"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Tour-in-die-Natur/3230773"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Der-Bildhauer-Karl-Prantl/3225717"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Ein-Naturpark-im-Dreilaendereck/3227249"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Altes-Handwerk/3220501"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Die-Fischer-vom-Neusiedler-See/9056940"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Unbekanntes-Grenzland/3230795"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Geschichte-der-Volksgruppen/9112116"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Zweisprachige-Ortstafeln/9056913"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Burgenland-von-1938-bis-1945/9441454"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Der-Weinskandal/9056886"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Der-Eiserne-Vorhang-faellt/9061026"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/1988-Der-Papst-im-Burgenland/9070878"),
      new TopicUrlDTO(topic, "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Das-Loch-in-der-Sued/9061265")
    };
    
    ConcurrentLinkedQueue<TopicUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new TopicUrlDTO(topic, requestUrl));
    
    OrfArchiveTopicTask target = new OrfArchiveTopicTask(createCrawler(), queue);
    Set<TopicUrlDTO> actual = target.invoke();
    
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }  
}
