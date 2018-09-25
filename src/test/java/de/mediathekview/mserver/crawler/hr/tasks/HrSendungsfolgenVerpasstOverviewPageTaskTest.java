package de.mediathekview.mserver.crawler.hr.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.hr.HrCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
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
public class HrSendungsfolgenVerpasstOverviewPageTaskTest {

  protected MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  @Test
  public void test() throws IOException {
    final String requestUrl = "https://www.hr-fernsehen.de/tv-programm/guide_hrfernsehen-100~_date-2018-09-24.html";
    JsoupMock.mock(requestUrl, "/hr/hr_day_page.html");

    final CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/tv-programm/hank-zipzer,sendung-43442.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/tv-programm/die-zehn-gebote-6,sendung-43404.html"),
        new CrawlerUrlDTO(
            "http://www.daserste.de/unterhaltung/soaps-telenovelas/rote-rosen/index.html"),
        new CrawlerUrlDTO(
            "http://www.daserste.de/unterhaltung/soaps-telenovelas/sturm-der-liebe/index.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/tv-programm/nashorn-zebra--co,sendung-43230.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/maintower/sendungen/maintower-weekend,sendung-43278.html"),
        new CrawlerUrlDTO(
            "https://www.hessenschau.de/tv-sendung/index.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/wetter/index.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/engel-fragt/sendungen/engel-fragt-bin-ich-schoen,sendung-29818.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/herkules/sendungen/herkules,sendung-43316.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/hessen-a-la-carte/sendungen/herrliche-rezepte-mit-handkaese-,sendung-9004.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/hessen-a-la-carte/sendungen/fleisch-fuer-kenner,sendung-7172.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/tv-programm/liebe-ist-das-schoenste-geschenk,sendung-13306.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/tv-programm/nashorn-zebra--co,sendung-43416.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/giraffe-erdmaennchen-und-co/sendungen/giraffe-erdmaennchen--co,sendung-43414.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/hallo-hessen/sendungen/hallo-hessen,sendung-43434.html"),
        new CrawlerUrlDTO(
            "https://www.hessenschau.de/tv-sendung/hs-kompakt-100.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/hallo-hessen/sendungen/hallo-hessen,sendung-43402.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/maintower/sendungen/maintower---zahlreiche-schaeden-durch-sturm-fabienne-,sendung-43412.html"),
        new CrawlerUrlDTO(
            "http://www.mdr.de/brisant/index.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/service-zuhause/sendungen/service-zuhause---minijobs-smarthome-alternativen-zu-plastik-und-richtig-streiten,sendung-43422.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/alle-wetter/sendungen/alle-wetter-unwetter-kaltluft-bodenfrost,sendung-43418.html"),
        new CrawlerUrlDTO(
            "http://www.tagesschau.de/"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/alle-wetter/sendungen/alle-wetter-extra-herbststurm-ueber-hessen,sendung-46014.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/defacto/sendungen/defacto,sendung-43400.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/hessenreporter/sendungen/die-neue-frankfurter-altstadt--ein-jahrhundertprojekt,sendung-43410.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/tatort/tatorte-im-hr/tatort-borowski-und-der-stille-gast,sendung-43436.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/heimspiel/sendungen/heimspiel,sendung-43428.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/tv-programm/der-schrei,sendung-43602.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/tv-programm/bilder-aus-hessen,sendung-43614.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/tv-programm/schloss-einstein,sendung-43590.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/tv-programm/hank-zipzer,sendung-43600.html")
    };

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final HrSendungsfolgenVerpasstOverviewPageTask target = new HrSendungsfolgenVerpasstOverviewPageTask(createCrawler(), urls);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

  protected HrCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>();
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new HrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}