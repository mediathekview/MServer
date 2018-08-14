package de.mediathekview.mserver.crawler.rbb.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore("javax.net.ssl.*")
public class RbbDayTaskTest extends RbbTaskTestBase {

  @Test
  public void test() throws IOException {

    final String requestUrl = String.format(RbbConstants.URL_DAY_PAGE, 0);
    JsoupMock.mock(requestUrl, "/rbb/rbb_day1.html");

    final CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Film-im-rbb/Nachtfalter/rbb-Fernsehen/Video?bcastId=10009780&documentId=50509576"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Film-im-rbb/Zwei-%C3%BCbern-Berg/rbb-Fernsehen/Video?bcastId=10009780&documentId=50508654"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-AKTUELL/rbb-AKTUELL-vom-02-03-2018-um-13-Uhr/rbb-Fernsehen/Video?bcastId=3907840&documentId=50511332"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-Fernsehen-weitere-Sendungen/Lichters-Schnitzeljagd-3/rbb-Fernsehen/Video?bcastId=8256334&documentId=50516164"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-AKTUELL/rbb-AKTUELL-vom-02-03-2018-um-16-Uhr/rbb-Fernsehen/Video?bcastId=3907840&documentId=50515958"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-AKTUELL/rbb-AKTUELL-vom-02-03-2018-um-17-Uhr/rbb-Fernsehen/Video?bcastId=3907840&documentId=50516510"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Panda-Gorilla-Co/Panda-Gorilla-Co-Folge-287/rbb-Fernsehen/Video?bcastId=10027190&documentId=50516384"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-UM6/rbb-UM6-vom-02-03-2018/rbb-Fernsehen/Video?bcastId=9597140&documentId=50520456"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-UM6/Stillstand-trotz-Boom/rbb-Fernsehen/Video?bcastId=9597140&documentId=50519178"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-UM6/Sole-statt-Salz/rbb-Fernsehen/Video?bcastId=9597140&documentId=50519204"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-UM6/Transplant-Tiger/rbb-Fernsehen/Video?bcastId=9597140&documentId=50519150"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-UM6/Sport-bei-rbb-UM6/rbb-Fernsehen/Video?bcastId=9597140&documentId=50520332"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/zibb/zibb-vom-02-03-2018/rbb-Fernsehen/Video?bcastId=3822084&documentId=50525556"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/zibb/Rauf-aufs-Eis-oder-besser-doch-nicht/rbb-Fernsehen/Video?bcastId=3822084&documentId=50519444"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/zibb/Wenn-400-000-%C3%BCber-80-Millionen-abstimmen/rbb-Fernsehen/Video?bcastId=3822084&documentId=50519604"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/zibb/Ein-Huhn-auf-Wanderschaft/rbb-Fernsehen/Video?bcastId=3822084&documentId=50520162"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/zibb/Artischocke-Co-So-gesund-sind-Bitters/rbb-Fernsehen/Video?bcastId=3822084&documentId=50519926"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/zibb/Die-Hufelandstra%C3%9Fe-runter/rbb-Fernsehen/Video?bcastId=3822084&documentId=50520232"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Brandenburg-aktuell/Brandenburg-aktuell-vom-02-03-2018/rbb-Fernsehen/Video?bcastId=3822126&documentId=50524226"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Brandenburg-aktuell/Grippewelle-%C3%BCberrollt-Brandenburg/rbb-Fernsehen/Video?bcastId=3822126&documentId=50522462"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Brandenburg-aktuell/Verh%C3%A4rtete-Fronten-bei-Dialogversuch/rbb-Fernsehen/Video?bcastId=3822126&documentId=50522236"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Brandenburg-aktuell/Nachrichten-I/rbb-Fernsehen/Video?bcastId=3822126&documentId=50522264"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Brandenburg-aktuell/Es-geht-etwas-voran/rbb-Fernsehen/Video?bcastId=3822126&documentId=50522288"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Brandenburg-aktuell/Frauenwoche-in-Brandenburg/rbb-Fernsehen/Video?bcastId=3822126&documentId=50522414"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Abendschau/Abendschau-vom-02-03-2018/rbb-Fernsehen/Video?bcastId=3822076&documentId=50523724"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Abendschau/Berlin-ist-sicherer-geworden/rbb-Fernsehen/Video?bcastId=3822076&documentId=50522366"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Abendschau/Neuer-Prozess-gegen-Raser/rbb-Fernsehen/Video?bcastId=3822076&documentId=50522338"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Abendschau/Nachrichten-I/rbb-Fernsehen/Video?bcastId=3822076&documentId=50522314"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Abendschau/Probleme-bei-der-Bahnhofsmission/rbb-Fernsehen/Video?bcastId=3822076&documentId=50524274"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/Abendschau/Gr%C3%BCne-digital/rbb-Fernsehen/Video?bcastId=3822076&documentId=50522706"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-AKTUELL/rbb-AKTUELL-vom-02-03-2018/rbb-Fernsehen/Video?bcastId=3907840&documentId=50527624"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-AKTUELL/Weniger-Straftaten-in-Berlin/rbb-Fernsehen/Video?bcastId=3907840&documentId=50526664"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-AKTUELL/Neuer-Prozess-gegen-Raser/rbb-Fernsehen/Video?bcastId=3907840&documentId=50527098"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-AKTUELL/Nachrichten/rbb-Fernsehen/Video?bcastId=3907840&documentId=50526606"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-AKTUELL/Probleme-bei-der-Bahnhofsmission/rbb-Fernsehen/Video?bcastId=3907840&documentId=50526736"),
        new CrawlerUrlDTO(
            "https://mediathek.rbb-online.de/tv/rbb-AKTUELL/Fritz-wird-Fritzundzwanzig/rbb-Fernsehen/Video?bcastId=3907840&documentId=50526760"),};

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final RbbDayTask target = new RbbDayTask(createCrawler(), urls);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
