package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfTopicUrlDTO;
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
@PowerMockIgnore("javax.net.ssl.*")
public class OrfDayTaskTest extends OrfTaskTestBase {
  
  @Test
  public void test() throws IOException {
    final String requestUrl = "http://tvthek.orf.at/schedule/03.02.2018";
    JsoupMock.mock(requestUrl, "/orf/orf_day.html");
    
    OrfTopicUrlDTO[] expected = new OrfTopicUrlDTO[] {
      new OrfTopicUrlDTO("Wetter-Panorama", "http://tvthek.orf.at/profile/Wetter-Panorama/7268748/Wetter-Panorama/13962273"),
      new OrfTopicUrlDTO("Servus Kasperl", "http://tvthek.orf.at/profile/Servus-Kasperl/3272601/Servus-Kasperl-Kasperl-Co-Eine-Kragenweite-zu-gross/13963856"),
      new OrfTopicUrlDTO("Das Yoga-Magazin", "http://tvthek.orf.at/profile/Yoga-Magazin/7708946/Das-Yoga-Magazin-Folge-71/13964045"),
      new OrfTopicUrlDTO("Hallo okidoki", "http://tvthek.orf.at/profile/Hallo-Okidoki/2616615/Hallo-okidoki/13963859"),
      new OrfTopicUrlDTO("ABC Bär", "http://tvthek.orf.at/profile/ABC-Baer/4611813/ABC-Baer/13963860"),
      new OrfTopicUrlDTO("Tolle Tiere", "http://tvthek.orf.at/profile/Tolle-Tiere/13764575/Tolle-Tiere/13963862"),
      new OrfTopicUrlDTO("Tom Turbo - Detektivclub", "http://tvthek.orf.at/profile/Tom-Turbo-Detektivclub/2616703/Tom-Turbo-Detektivclub-Das-Amulett-der-blauen-Fee/13963864"),
      new OrfTopicUrlDTO("ZIB 9:00", "http://tvthek.orf.at/profile/ZIB-900/71256/ZIB-900/13963865"),
      new OrfTopicUrlDTO("Schmatzo - Kochen mit WOW", "http://tvthek.orf.at/profile/Schmatzo/13886275/Schmatzo-Kochen-mit-WOW/13963867"),
      new OrfTopicUrlDTO("Julia - eine ungewöhnliche Frau", "http://tvthek.orf.at/profile/Julia-eine-ungewoehnliche-Frau/13888264/Julia-eine-ungewoehnliche-Frau-Der-Hormonskandal/13963869"),
      new OrfTopicUrlDTO("Oben ohne", "http://tvthek.orf.at/profile/Oben-ohne/13886963/Oben-ohne-Judith-und-James/13963870"),
      new OrfTopicUrlDTO("Nordische Kombination Hakuba", "http://tvthek.orf.at/profile/Nordische-Kombination/13886493/Nordische-Kombination-Hakuba-Skispringen-und-Langlauf-Highlights/13963989"),
      new OrfTopicUrlDTO("Der Bär ist los! Die Geschichte von Bruno", "http://tvthek.orf.at/profile/Der-Baer-ist-los-Die-Geschichte-von-Bruno/13888385/Der-Baer-ist-los-Die-Geschichte-von-Bruno/13963871"),
      new OrfTopicUrlDTO("Ski alpin", "http://tvthek.orf.at/profile/Ski-alpin-Damen/13886308/Ski-alpin-Abfahrt-der-Damen-Garmisch/13963987"),
      new OrfTopicUrlDTO("AD | Ski alpin", "http://tvthek.orf.at/profile/AD-Ski-alpin-Damen/13886309/AD-Ski-alpin-Abfahrt-der-Damen-Garmisch/13964038"),
      new OrfTopicUrlDTO("Tennis Davis-Cup", "http://tvthek.orf.at/profile/Tennis-Davis-Cup/13886440/Tennis-Davis-Cup-Oesterreich-vs-Weissrussland-Tag-2-aus-St-Poelten/13963873"),
      new OrfTopicUrlDTO("Tirol heute", "http://tvthek.orf.at/profile/Tirol-heute/70023/Tirol-heute/13964032"),
      new OrfTopicUrlDTO("Vorarlberg heute", "http://tvthek.orf.at/profile/Vorarlberg-heute/70024/Vorarlberg-heute/13964031"),
      new OrfTopicUrlDTO("Wien heute", "http://tvthek.orf.at/profile/Wien-heute/70018/Wien-heute/13964026"),
      new OrfTopicUrlDTO("Servus, Srečno, Ciao", "http://tvthek.orf.at/profile/Servus-Sreno-Ciao/8179756/Servus-Sreno-Ciao/13963412"),
      new OrfTopicUrlDTO("Wetter Burgenland", "http://tvthek.orf.at/profile/Wetter-Burgenland/8094958/Wetter-Burgenland/13964066"),
      new OrfTopicUrlDTO("Wetter Kärnten", "http://tvthek.orf.at/profile/Wetter-Kaernten/8094982/Wetter-Kaernten/13964068"),
      new OrfTopicUrlDTO("Wetter Niederösterreich", "http://tvthek.orf.at/profile/Wetter-Niederoesterreich/8094947/Wetter-Niederoesterreich/13964070"),
      new OrfTopicUrlDTO("Wetter Oberösterreich", "http://tvthek.orf.at/profile/Wetter-Oberoesterreich/8094936/Wetter-Oberoesterreich/13964072"),    
      new OrfTopicUrlDTO("Wetter Salzburg", "http://tvthek.orf.at/profile/Wetter-Salzburg/8095016/Wetter-Salzburg/13964074"),
      new OrfTopicUrlDTO("Wetter Steiermark", "http://tvthek.orf.at/profile/Wetter-Steiermark/8094971/Wetter-Steiermark/13964076"),
      new OrfTopicUrlDTO("Wetter Tirol", "http://tvthek.orf.at/profile/Wetter-Tirol/8094993/Wetter-Tirol/13964078"),
      new OrfTopicUrlDTO("Wetter Vorarlberg", "http://tvthek.orf.at/profile/Wetter-Vorarlberg/8095005/Wetter-Vorarlberg/13964080"),
      new OrfTopicUrlDTO("Wetter Wien", "http://tvthek.orf.at/profile/Wetter-Wien/8094871/Wetter-Wien/13964082"),
      new OrfTopicUrlDTO("ZIB 1", "http://tvthek.orf.at/profile/ZIB-1/1203/ZIB-1/13963892"),
      new OrfTopicUrlDTO("ZIB 1 (ÖGS)", "http://tvthek.orf.at/profile/ZIB-1-OeGS/145302/ZIB-1-OeGS/13964083"),
      new OrfTopicUrlDTO("Sport Aktuell", "http://tvthek.orf.at/profile/Sport-Aktuell/889789/Sport-Aktuell/13963895"),
      new OrfTopicUrlDTO("Sport 20", "http://tvthek.orf.at/profile/Sport-20/2642577/Sport-20/13963897"),
      new OrfTopicUrlDTO("ZIB 20", "http://tvthek.orf.at/profile/ZIB-20/1218/ZIB-20/13963896"),
      new OrfTopicUrlDTO("Seitenblicke", "http://tvthek.orf.at/profile/Seitenblicke/4790197/Seitenblicke/13963898"),
    };
    
    ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(requestUrl));
    
    OrfDayTask target = new OrfDayTask(createCrawler(), queue);
    Set<OrfTopicUrlDTO> actual = target.invoke();
    
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }  
}
