package de.mediathekview.mserver.crawler.arte.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoInfoDto;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(Parameterized.class)
public class ArteDtoVideo2FilmTaskTest extends WireMockTestBase {

  private final String inputResource;
  private final Film[] expectedFilms;

  public ArteDtoVideo2FilmTaskTest(String inputResource, Film[] expectedFilms) {
    this.inputResource = inputResource;
    this.expectedFilms = expectedFilms;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    Film film1 = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "Re: Der Traum vom Paradies - Nur die Liebe zählt",
        "Aktuelles und Gesellschaft - Reportagen und Recherchen",
        LocalDateTime.of(2024,04,26,19,42,32),
        Duration.parse("PT30M18S"));
    film1.setBeschreibung("Seit über fünf Jahren lebt eine Gruppe von Aussteigern auf der Kanareninsel La Gomera in wechselnder Besetzung ein alternatives Lebensmodell. Ein Leben abseits bürgerlicher Konventionen, mit offenen Partnerschaften, dominiert vom Grundgedanken der Liebe als Lebenshaltung.");
    film1.setGeoLocations(Arrays.asList(GeoLocations.GEO_DE_AT_CH_EU));
    film1.setWebsite(toUrl("https://www.arte.tv/de/videos/111749-020-A/re-der-traum-vom-paradies-nur-die-liebe-zaehlt/"));
    film1.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/111000/111700/111749-020-A_HQ_0_VOA_08815961_MP4-800_AMM-PTWEB-80922086414600_2J3AeShPD8.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/111000/111700/111749-020-A_EQ_0_VOA_08815960_MP4-1500_AMM-PTWEB-80922086414600_2J3ApShPGZ.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/111000/111700/111749-020-A_SQ_0_VOA_08815959_MP4-2200_AMM-PTWEB-80922086414600_2J39OShOv6.mp4"
    ));

    Film film1a = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "Re: Der Traum vom Paradies - Nur die Liebe zählt (mit Untertitel)",
        "Aktuelles und Gesellschaft - Reportagen und Recherchen",
        LocalDateTime.of(2024,04,26,19,42,32),
        Duration.parse("PT30M18S"));
    film1a.setBeschreibung("Seit über fünf Jahren lebt eine Gruppe von Aussteigern auf der Kanareninsel La Gomera in wechselnder Besetzung ein alternatives Lebensmodell. Ein Leben abseits bürgerlicher Konventionen, mit offenen Partnerschaften, dominiert vom Grundgedanken der Liebe als Lebenshaltung.");
    film1a.setGeoLocations(Arrays.asList(GeoLocations.GEO_DE_AT_CH_EU));
    film1a.setWebsite(toUrl("https://www.arte.tv/de/videos/111749-020-A/re-der-traum-vom-paradies-nur-die-liebe-zaehlt/"));
    film1a.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/111000/111700/111749-020-A_HQ_0_VOA-STMA_08815966_MP4-800_AMM-PTWEB-101162502546818_2J3ArShPGa.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/111000/111700/111749-020-A_EQ_0_VOA-STMA_08815965_MP4-1500_AMM-PTWEB-101162502546818_2J3AqShPGZ.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/111000/111700/111749-020-A_SQ_0_VOA-STMA_08815964_MP4-2200_AMM-PTWEB-101162502546818_2J39jShP0S.mp4"
    ));
    //
    Film film2 = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "In Therapie - Eine Serie als Mosaik - Staffel 1 (Originalversion mit Untertitel)",
        "Fernsehfilme und Serien - Serien",
        LocalDateTime.of(2024,9,20,10,43,33),
        Duration.parse("PT3M2S"));
    film2.setBeschreibung("Das Erfolgsduo Eric Toledano und Olivier Nakache (\"Ziemlich beste Freunde\") erzählt, wie es das Team der Serie \"In Therapie\" zusammengestellt hat.");
    film2.setGeoLocations(Arrays.asList(GeoLocations.GEO_DE_AT_CH_FR));
    film2.setWebsite(toUrl("https://www.arte.tv/de/videos/100934-001-A/in-therapie-eine-serie-als-mosaik/"));
    film2.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/100000/100900/100934-001-A_HQ_0_VOF-STA_09235275_MP4-800_AMM-PTWEB-40411236271561_2P1Xo10GfaL.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/100000/100900/100934-001-A_EQ_0_VOF-STA_09235274_MP4-1500_AMM-PTWEB-40411236271561_2P1Xn10GfaL.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/100000/100900/100934-001-A_SQ_0_VOF-STA_09235277_MP4-2200_AMM-PTWEB-40411236271561_2P1Xq10GfaL.mp4"
    ));
    //
    Film film3 = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "GEO Reportage - Gitarren und Flamenco in Granada",
        "Entdeckung der Welt - Leben anderswo",
        LocalDateTime.of(2025,05,24,8,53,58),
        Duration.parse("PT52M19S"));
    film3.setBeschreibung("Gitarrenmusik, Instrumentenbau und Flamenco sind bedeutende Teile der andalusischen Kultur. So wie die zwölfjährige Claudia Calle möchten viele junge Menschen im Süden Spaniens durch die Musik berühmt werden: als Tänzer, Musiker oder Instrumentenbauer. Àlvaro Pérez spielt seit seinem sechsten Lebensjahr Gitarre. Er kauft seine Instrumente bei Francisco Manuel Díaz ...");
    film3.setGeoLocations(Arrays.asList(GeoLocations.GEO_NONE));
    film3.setWebsite(toUrl("https://www.arte.tv/de/videos/078702-021-A/geo-reportage/"));
    film3.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/078000/078700/078702-021-A_HQ_0_VOA_09963595_MP4-800_AMM-PTWEB-80803096529234_2ZSv7Yr7Pz.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/078000/078700/078702-021-A_EQ_0_VOA_09963593_MP4-1500_AMM-PTWEB-80803096529234_2ZSvZYr7Zv.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/078000/078700/078702-021-A_SQ_0_VOA_09963592_MP4-2200_AMM-PTWEB-80803096529234_2ZSu6Yr7Bo.mp4"
    ));
    Film film3a = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "GEO Reportage - Gitarren und Flamenco in Granada (mit Untertitel)",
        "Entdeckung der Welt - Leben anderswo",
        LocalDateTime.of(2025,05,24,8,53,58),
        Duration.parse("PT52M19S"));
    film3a.setBeschreibung("Gitarrenmusik, Instrumentenbau und Flamenco sind bedeutende Teile der andalusischen Kultur. So wie die zwölfjährige Claudia Calle möchten viele junge Menschen im Süden Spaniens durch die Musik berühmt werden: als Tänzer, Musiker oder Instrumentenbauer. Àlvaro Pérez spielt seit seinem sechsten Lebensjahr Gitarre. Er kauft seine Instrumente bei Francisco Manuel Díaz ...");
    film3a.setGeoLocations(Arrays.asList(GeoLocations.GEO_NONE));
    film3a.setWebsite(toUrl("https://www.arte.tv/de/videos/078702-021-A/geo-reportage/"));
    film3a.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/078000/078700/078702-021-A_HQ_0_VOA-STMA_09963599_MP4-800_AMM-PTWEB-100983826679341_2ZSvbYr7Zw.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/078000/078700/078702-021-A_EQ_0_VOA-STMA_09963597_MP4-1500_AMM-PTWEB-100983826679341_2ZSvaYr7Zw.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/078000/078700/078702-021-A_SQ_0_VOA-STMA_09963596_MP4-2200_AMM-PTWEB-100983826679341_2ZSm8Yr6mB.mp4"
    ));
    //
    Film film4 = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "Melenas - Bilbao BBK Live Festival 2024 (Originalversion)",
        "ARTE Concert - Pop & Rock",
        LocalDateTime.of(2024,12,19,11,59,48),
        Duration.parse("PT25M32S"));
    film4.setBeschreibung("Das spanische Quartett Melenas bringt Krautrock mit Pop-Einflüssen auf die Bühne des Bilbao BBK Live und stellt bei dieser Gelegenheit sein drittes Album Ahora vor.");
    film4.setGeoLocations(Arrays.asList(GeoLocations.GEO_NONE));
    film4.setWebsite(toUrl("https://www.arte.tv/de/videos/120718-011-A/melenas/"));
    film4.setUrls(toResolutionMap(
        "SMALL","https://arteconcert-a.akamaihd.net/am/concert/120000/120700/120718-011-C_HQ_0_VO_09513174_MP4-800_AMM-CONCERT-NEXT-60682878339051_2T8E01KVJUF.mp4",
        "NORMAL", "https://arteconcert-a.akamaihd.net/am/concert/120000/120700/120718-011-C_EQ_0_VO_09513172_MP4-1500_AMM-CONCERT-NEXT-60682878339051_2T8EG1KVJYn.mp4",
        "HD", "https://arteconcert-a.akamaihd.net/am/concert/120000/120700/120718-011-C_SQ_0_VO_09513175_MP4-2200_AMM-CONCERT-NEXT-60682878339051_2T8Fa1KVJh8.mp4"
    ));
    //
    //
    Film film5 = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "\"Dahomey\" - Interview mit Mati Diop - Goldener Bär der Berlinale für \"Dahomey\" (Originalversion mit Untertitel)",
        "Kino - Rund um den Film",
        LocalDateTime.of(2024,02,22,17,05,03),
        Duration.parse("PT3M58S"));
    film5.setBeschreibung("In Dahomey greift die Regisseurin Mati Diop viele grundlegende Fragen auf, vor allem die der Rückgabe von Kunstwerken, die westliche Kolonialtruppen in Afrika erbeutet haben, in dem Fall die Franzosen in Benin, aber indirekt auch die der Kolonisation und Dekolonisation, die bis heute nicht geregelt ist. Mit Poesie und Kraft beleuchtet die Dokumentation diese prägende Epoche.");
    film5.setGeoLocations(Arrays.asList(GeoLocations.GEO_DE_AT_CH_FR));
    film5.setWebsite(toUrl("https://www.arte.tv/de/videos/118149-011-A/dahomey-interview-mit-mati-diop/"));
    film5.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/118000/118100/118149-011-A_HQ_0_VOF-STA_08639436_MP4-800_AMM-PTWEB-80922104532920_2GTUnF2DDw.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/118000/118100/118149-011-A_EQ_0_VOF-STA_08639450_MP4-1500_AMM-PTWEB-80922104532920_2GTWLF2DJs.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/118000/118100/118149-011-A_SQ_0_VOF-STA_08639438_MP4-2200_AMM-PTWEB-80922104532920_2GTUAF2D9J.mp4"
    ));
    //
    Film film6 = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "Raúl Paz - Musikalische Höhenflüge (Originalversion)",
        "ARTE Concert - World",
        LocalDateTime.of(2025,03,04,12,19,32),
        Duration.parse("PT35M30S"));
    film6.setBeschreibung("Raúl Paz ist als Vertreter der franko-kubanischen Musik bei den Musikalischen Höhenflügen zu Gast. Im Cabaret Sauvage präsentiert der Musiker mit Guajiro Chic ein Album, auf dem er sich auf seine Wurzeln zurückbesinnt.");
    film6.setGeoLocations(Arrays.asList(GeoLocations.GEO_NONE));
    film6.setWebsite(toUrl("https://www.arte.tv/de/videos/118208-012-A/raul-paz/"));
    film6.setUrls(toResolutionMap(
        "SMALL","https://arteconcert-a.akamaihd.net/am/concert/118000/118200/118208-012-A_HQ_0_VO_09745388_MP4-800_AMM-CONCERT-NEXT-60691964320037_2WM3YKa4Lb.mp4",
        "NORMAL", "https://arteconcert-a.akamaihd.net/am/concert/118000/118200/118208-012-A_EQ_0_VO_09745386_MP4-1500_AMM-CONCERT-NEXT-60691964320037_2WM2pKa4I8.mp4",
        "HD", "https://arteconcert-a.akamaihd.net/am/concert/118000/118200/118208-012-A_SQ_0_VO_09745389_MP4-2200_AMM-CONCERT-NEXT-60691964320037_2WM3mKa4Rc.mp4"
    ));
    //
    Film film7a = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "Salon Spitz",
        "Kino - Kurzfilme",
        LocalDateTime.of(2024,04,14,01,13,07),
        Duration.parse("PT8M30S"));
    film7a.setBeschreibung("Berlin, 1923: Minna betritt auf Einladung ihrer Freundin Edith das erste Mal einen Frauenliteratursalon. Eingeschüchtert von den anderen Frauen hat sie nicht den Mut, selbst die Bühne zu betreten. Als wäre das nicht schon aufregend genug, dringt auch noch Lars, ein selbsternannter Dichter, in die Intimsphäre des Abends ein ...");
    film7a.setGeoLocations(Arrays.asList(GeoLocations.GEO_DE_FR));
    film7a.setWebsite(toUrl("https://www.arte.tv/de/videos/114555-002-A/salon-spitz/"));
    film7a.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/114000/114500/114555-002-A_HQ_0_VOA_08791484_MP4-800_AMM-PTWEB-80903920433300_2IhUCS5yQB.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/114000/114500/114555-002-A_EQ_0_VOA_08791483_MP4-1500_AMM-PTWEB-80903920433300_2IhUBS5yQB.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/114000/114500/114555-002-A_SQ_0_VOA_08791486_MP4-2200_AMM-PTWEB-80903920433300_2IhSRS5yK7.mp4"
    ));
    Film film7b = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "Salon Spitz (Originalversion mit Untertitel)",
        "Kino - Kurzfilme",
        LocalDateTime.of(2024,04,14,01,13,07),
        Duration.parse("PT8M30S"));
    film7b.setBeschreibung("Berlin, 1923: Minna betritt auf Einladung ihrer Freundin Edith das erste Mal einen Frauenliteratursalon. Eingeschüchtert von den anderen Frauen hat sie nicht den Mut, selbst die Bühne zu betreten. Als wäre das nicht schon aufregend genug, dringt auch noch Lars, ein selbsternannter Dichter, in die Intimsphäre des Abends ein ...");
    film7b.setGeoLocations(Arrays.asList(GeoLocations.GEO_DE_FR));
    film7b.setWebsite(toUrl("https://www.arte.tv/de/videos/114555-002-A/salon-spitz/"));
    film7b.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/114000/114500/114555-002-A_HQ_0_VOA-STF_08791493_MP4-800_AMM-PTWEB-101144248555256_2IhUIS5yQO.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/114000/114500/114555-002-A_EQ_0_VOA-STF_08791492_MP4-1500_AMM-PTWEB-101144248555256_2IhVCS5ySP.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/114000/114500/114555-002-A_SQ_0_VOA-STF_08791495_MP4-2200_AMM-PTWEB-101144248555256_2IhSTS5yKB.mp4"
    ));
    Film film7c = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "Salon Spitz (mit Untertitel)",
        "Kino - Kurzfilme",
        LocalDateTime.of(2024,04,14,01,13,07),
        Duration.parse("PT8M30S"));
    film7c.setBeschreibung("Berlin, 1923: Minna betritt auf Einladung ihrer Freundin Edith das erste Mal einen Frauenliteratursalon. Eingeschüchtert von den anderen Frauen hat sie nicht den Mut, selbst die Bühne zu betreten. Als wäre das nicht schon aufregend genug, dringt auch noch Lars, ein selbsternannter Dichter, in die Intimsphäre des Abends ein ...");
    film7c.setGeoLocations(Arrays.asList(GeoLocations.GEO_DE_FR));
    film7c.setWebsite(toUrl("https://www.arte.tv/de/videos/114555-002-A/salon-spitz/"));
    film7c.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/114000/114500/114555-002-A_HQ_0_VOA-STMA_08791489_MP4-800_AMM-PTWEB-101144331538914_2IhUGS5yQO.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/114000/114500/114555-002-A_EQ_0_VOA-STMA_08791488_MP4-1500_AMM-PTWEB-101144331538914_2IhUFS5yQL.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/114000/114500/114555-002-A_SQ_0_VOA-STMA_08791491_MP4-2200_AMM-PTWEB-101144331538914_2IhSSS5yKA.mp4"
    ));
    //
    Film film8a = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "Kenia: Die Mücken schlagen zurück - ARTE Reportage",
        "Aktuelles und Gesellschaft - Reportagen und Recherchen",
        LocalDateTime.of(2024,9,27,12,00,00),
        Duration.parse("PT12M21S"));
    film8a.setBeschreibung("Alte und neue die Malaria übertragende Mücken entwickelten neue Überlebens- und Angriffs-Strategien. An den Folgen ihrer Stiche sterben zurzeit über 600.000 Menschen pro Jahr. In Kenia scheinen die Wissenschaftler ihren Kampf gegen sie zu verlieren.");
    film8a.setGeoLocations(Arrays.asList(GeoLocations.GEO_NONE));
    film8a.setWebsite(toUrl("https://www.arte.tv/de/videos/118963-000-A/kenia-die-muecken-schlagen-zurueck/"));
    film8a.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/118000/118900/118963-000-A_HQ_0_VA_09253362_MP4-800_AMM-PTWEB-80923512472000_2PIgf10g7F6.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/118000/118900/118963-000-A_EQ_0_VA_09253361_MP4-1500_AMM-PTWEB-80923512472000_2PIg910g7D3.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/118000/118900/118963-000-A_SQ_0_VA_09253364_MP4-2200_AMM-PTWEB-80923512472000_2PIcj10g5qr.mp4"
    ));
    Film film8b = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "Kenia: Die Mücken schlagen zurück - ARTE Reportage (Originalversion)",
        "Aktuelles und Gesellschaft - Reportagen und Recherchen",
        LocalDateTime.of(2024,9,27,12,00,00),
        Duration.parse("PT12M21S"));
    film8b.setBeschreibung("Alte und neue die Malaria übertragende Mücken entwickelten neue Überlebens- und Angriffs-Strategien. An den Folgen ihrer Stiche sterben zurzeit über 600.000 Menschen pro Jahr. In Kenia scheinen die Wissenschaftler ihren Kampf gegen sie zu verlieren.");
    film8b.setGeoLocations(Arrays.asList(GeoLocations.GEO_NONE));
    film8b.setWebsite(toUrl("https://www.arte.tv/de/videos/118963-000-A/kenia-die-muecken-schlagen-zurueck/"));
    film8b.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/118000/118900/118963-000-A_HQ_0_VO_09253367_MP4-800_AMM-PTWEB-80923512472000_2PIhG10g7I2.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/118000/118900/118963-000-A_EQ_0_VO_09253366_MP4-1500_AMM-PTWEB-80923512472000_2PIgh10g7F6.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/118000/118900/118963-000-A_SQ_0_VO_09253369_MP4-2200_AMM-PTWEB-80923512472000_2PIgi10g7F6.mp4"
    ));
    Film film8c = new Film(
        UUID.randomUUID(), 
        Sender.ARTE_DE, 
        "Kenia: Die Mücken schlagen zurück - ARTE Reportage (mit Untertitel)",
        "Aktuelles und Gesellschaft - Reportagen und Recherchen",
        LocalDateTime.of(2024,9,27,12,00,00),
        Duration.parse("PT12M21S"));
    film8c.setBeschreibung("Alte und neue die Malaria übertragende Mücken entwickelten neue Überlebens- und Angriffs-Strategien. An den Folgen ihrer Stiche sterben zurzeit über 600.000 Menschen pro Jahr. In Kenia scheinen die Wissenschaftler ihren Kampf gegen sie zu verlieren.");
    film8c.setGeoLocations(Arrays.asList(GeoLocations.GEO_NONE));
    film8c.setWebsite(toUrl("https://www.arte.tv/de/videos/118963-000-A/kenia-die-muecken-schlagen-zurueck/"));
    film8c.setUrls(toResolutionMap(
        "SMALL","https://arteptweb-a.akamaihd.net/am/ptweb/118000/118900/118963-000-A_HQ_0_VA-STMA_09273186_MP4-800_AMM-PTWEB-101164513616247_2PXE215ktL3.mp4",
        "NORMAL", "https://arteptweb-a.akamaihd.net/am/ptweb/118000/118900/118963-000-A_EQ_0_VA-STMA_09273185_MP4-1500_AMM-PTWEB-101164513616247_2PXE715ktOH.mp4",
        "HD", "https://arteptweb-a.akamaihd.net/am/ptweb/118000/118900/118963-000-A_SQ_0_VA-STMA_09273188_MP4-2200_AMM-PTWEB-101164513616247_2PXE415ktL4.mp4"
    ));
    //
    return Arrays.asList(new Object[][] {
      { "/arte/arte_film_1", 
        new Film[] {film1, film1a}
      },
      { "/arte/arte_film_2", 
        new Film[] {film2}
      },
      { "/arte/arte_film_3", 
        new Film[] {film3, film3a}
      },
      { "/arte/arte_film_4", 
        new Film[] {film4}
      },
      { "/arte/arte_film_5", 
        new Film[] {film5}
      },
      { "/arte/arte_film_6", 
        new Film[] {film6}
      },
      { "/arte/arte_film_7", 
        new Film[] {film7a, film7b, film7c}
      },
      { "/arte/arte_film_8", 
        new Film[] {film8a, film8b, film8c }
      }
    });
  }
  
  private String getVideoInfoUrl() {
    return getWireMockBaseUrlSafe() + this.inputResource + "_videos.json";
  }
  private String getVideoLinkUrl() {
    return getWireMockBaseUrlSafe() + this.inputResource + "_links.json";
  }

  private Set<ArteVideoInfoDto> executeArteVideoInfoTask() {
    Queue<TopicUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new TopicUrlDTO("", getVideoInfoUrl()));
    return new ArteVideoInfoTask(ArteTaskTestBase.createCrawler(), input, 1).invoke();
  }

  private Set<ArteVideoInfoDto> executeArteVideoLinkTask(ArteVideoInfoDto info) {
    Queue<ArteVideoInfoDto> input = new ConcurrentLinkedQueue<>();
    info.setUrl(getVideoLinkUrl());
    input.add(info);
    return new ArteVideoLinkTask(ArteTaskTestBase.createCrawler(), input).invoke();
  }
  
  private Set<Film> executeArteDtoVideo2FilmTask(ArteVideoInfoDto info) {
    Queue<ArteVideoInfoDto> input = new ConcurrentLinkedQueue<>();
    input.add(info);
    return new ArteDtoVideo2FilmTask(ArteTaskTestBase.createCrawler(), input).invoke();
  }

  @Test
  public void testFilmParsing() {
    setupSuccessfulJsonResponse(this.inputResource + "_videos.json", this.inputResource + "_videos.json");
    setupSuccessfulJsonResponse(this.inputResource + "_links.json", this.inputResource + "_links.json");
    setupHeadRequestForFileSize();
    
    // create info
    Set<ArteVideoInfoDto> infos = executeArteVideoInfoTask();
    assertThat(infos, is(not(empty())));

    // get all videolinks
    Set<ArteVideoInfoDto> enriched = executeArteVideoLinkTask(infos.stream().findFirst().get());
    assertThat(enriched, is(not(empty())));

    // convert to film
    Set<Film> actualFilms = executeArteDtoVideo2FilmTask(enriched.stream().findFirst().get());
    assertThat(actualFilms, is(not(empty())));

    assertThat(actualFilms.size(), is(expectedFilms.length));
    for (int i = 0; i < expectedFilms.length; i++) {
      //assertFilm(actualFilms.toArray(new Film[0])[i], expectedFilms[i]);
      String title = expectedFilms[i].getTitel();
      assertFilm(actualFilms.stream().filter(film -> title.equalsIgnoreCase(film.getTitel())).findAny().get(), expectedFilms[i]);
    }
    
  }
  
  public void assertFilm(Film act, Film exp) {
    assertEquals(exp.getBeschreibung(), act.getBeschreibung());
    assertEquals(exp.getDuration(), act.getDuration());
    assertEquals(exp.getSender(), act.getSender());
    assertEquals(exp.getThema(), act.getThema());
    assertEquals(exp.getTime(), act.getTime());
    assertEquals(exp.getTitel(), act.getTitel());
    assertEquals(exp.getWebsite(), act.getWebsite());
    //
    assertEquals(exp.getAudioDescriptions().size(), act.getAudioDescriptions().size());
    for (Resolution resolution : exp.getAudioDescriptions().keySet()) {
      FilmUrl expUrl = exp.getAudioDescriptions().get(resolution);
      FilmUrl actUrl = act.getAudioDescriptions().get(resolution);
      assertEquals(expUrl.getUrl(), actUrl.getUrl());
    }
    //
    assertEquals(exp.getSignLanguages().size(), act.getSignLanguages().size());
    for (Resolution resolution : exp.getSignLanguages().keySet()) {
      FilmUrl expUrl = exp.getSignLanguages().get(resolution);
      FilmUrl actUrl = act.getSignLanguages().get(resolution);
      assertEquals(expUrl.getUrl(), actUrl.getUrl());
    }
    //
    assertEquals(exp.getUrls().size(), act.getUrls().size());
    for (Resolution resolution : exp.getUrls().keySet()) {
      FilmUrl expUrl = exp.getUrls().get(resolution);
      FilmUrl actUrl = act.getUrls().get(resolution);
      assertEquals(expUrl.getUrl(), actUrl.getUrl());
    }
    //
    assertEquals(exp.getSubtitles().size(), act.getSubtitles().size());
    assertThat(exp.getSubtitles(), containsInAnyOrder(act.getSubtitles().toArray()));

  }

  public static URL toUrl(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      // unit test - no exception needed
    }
    return null;
  }
  
  public static Map<Resolution, FilmUrl> toResolutionMap(String... args) {
    if (args.length % 2 != 0) {
      throw new IllegalArgumentException("Argumentanzahl muss gerade sein (key, value, key, value, ...)");
    }

    return IntStream.range(0, args.length / 2)
        .mapToObj(i -> {
          String resName = args[i * 2];
          String url = args[i * 2 + 1];
          try {
            return Map.entry(Resolution.valueOf(resName), new FilmUrl(url, 0L));
          } catch (IllegalArgumentException | MalformedURLException e) {
            throw new RuntimeException("Fehler beim Verarbeiten von: " + resName + " → " + url, e);
          }
        })
        .collect(() -> new EnumMap<>(Resolution.class),
                 (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                 Map::putAll);
  }
}
