package de.mediathekview.mserver.crawler.dreisat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.FileReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DreisatDayPageHtmlDeserializerTest {

  private DreisatDayPageHtmlDeserializer target;

  @BeforeEach
  void setupDeserializer() {
    target = new DreisatDayPageHtmlDeserializer(DreisatConstants.URL_API_BASE);
  }

  @ParameterizedTest
  @MethodSource("getTestData")
  void deserializeTest(final String htmlFile, final List<CrawlerUrlDTO> expectedEntries) {
    final Document document = Jsoup.parse(FileReader.readFile(htmlFile));

    final Set<CrawlerUrlDTO> actual = target.deserialize(document);

    assertThat(actual).isNotNull().hasSize(expectedEntries.size()).containsAll(expectedEntries);
  }

  static Stream<Arguments> getTestData() {
    return Stream.of(
            arguments(
                    "/dreisat/dreisat_day_page1.html",
                    List.of(
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/sendung-vom-20-mai-2020-100.json"),
                            new CrawlerUrlDTO("https://api.3sat.de/content/documents/200520-sendung-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/sehen-statt-hoeren-vom-22-mai-2020-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/7-tage-auf-dem-jakobsweg-100.json"),
                            new CrawlerUrlDTO("https://api.3sat.de/content/documents/natur-pur-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/die-macht-der-vulkane-jahre-ohne-sommer-102.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/die-macht-der-vulkane-im-schatten-der-feuerberge-102.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/wilder-planet-wenn-die-erde-verrueckt-spielt-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/wilder-planet-erdbeben-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/wilder-planet-vulkane-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/fantastische-phaenomene1-104.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/fantastische-phaenomene2-104.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/200522-sendung-nano-102.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/sendung-vom-22-mai-2020-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/million-dollar-baby-102.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/pufpaffs-happy-hour-folge-59-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/olaf-schubert-sexy-forever-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/miss-allie-meine-herz-und-die-toilette-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/nessi-tausendschoen-30-jahre-zenit-104.json"))),
            arguments(
                    "/dreisat/dreisat_page_second_day.html",
                    List.of(
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/liebesgschichten-und-heiratssachen-2022-10-100.json"),
                            new CrawlerUrlDTO("https://api.3sat.de/content/documents/rolling-stones-102.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/don-giovanni-wiener-staatsoper-2021-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/quer-vom-22-oktober-2022-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/reaktionen-auf-ruecktritt-von-liz-truss-sendung-vom-21-10-2022-100.json"),
                            new CrawlerUrlDTO("https://api.3sat.de/content/documents/futur-wir-1-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/der-wilde-wald-der-kaiserin-kurzprogramm-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/unsere-waelder-im-reich-des-wassers-100.json"),
                            new CrawlerUrlDTO("https://api.3sat.de/content/documents/tina-turner-108.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/kulturplatz-vom-22-oktober-2022-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/bilder-aus-suedtirol-vom-22-oktober-2022-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/kunst--krempel-vom-22-oktober-2022-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/unsere-waelder-ein-jahr-unter-baeumen-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/laendermagazin-vom-22-oktober-2022-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/kwandwe-die-rueckkehr-der-loewen-102.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/221021-sendung-droht-ein-atomarer-konflikt-nano-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/liebe-versetzt-berge-104.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/unsere-waelder-die-sprache-der-baeume-100.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/clarissas-geheimnis-102.json"),
                            new CrawlerUrlDTO(
                                    "https://api.3sat.de/content/documents/offenes-geheimnis-100.json"))));
  }
}