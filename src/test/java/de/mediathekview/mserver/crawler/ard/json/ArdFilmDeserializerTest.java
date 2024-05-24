package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.crawler.ard.ArdFilmDto;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class ArdFilmDeserializerTest {

  private final String jsonFile;
  private final String expectedTopic;
  private final String expectedTitle;
  private final String expectedDescription;
  private final LocalDateTime expectedDateTime;
  private final Duration expectedDuration;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedADUrlSmall;
  private final String expectedADUrlNormal;
  private final String expectedADUrlHd;
  private final String expectedDGSUrlSmall;
  private final String expectedDGSUrlNormal;
  private final String expectedDGSUrlHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;
  private final ArdFilmInfoDto[] relatedFilms;
  private final Optional<Sender> additionalSender;
  private final int expectedFilmCount;

  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");

  public ArdFilmDeserializerTest(
      final String jsonFile,
      final String expectedTopic,
      final String expectedTitle,
      final String expectedDescription,
      final LocalDateTime expectedDateTime,
      final Duration expectedDuration,
      final String expectedUrlSmall,
      final String expectedUrlNormal,
      final String expectedUrlHd,
      final String expectedADUrlSmall,
      final String expectedADUrlNormal,
      final String expectedADUrlHd,
      final String expectedDGSUrlSmall,
      final String expectedDGSUrlNormal,
      final String expectedDGSUrlHd,
      final String expectedSubtitle,
      final GeoLocations expectedGeo,
      final ArdFilmInfoDto[] relatedFilms,
      final Optional<Sender> additionalSender) {
    this.jsonFile = jsonFile;
    this.expectedTopic = expectedTopic;
    this.expectedTitle = expectedTitle;
    this.expectedDescription = expectedDescription;
    this.expectedDateTime = expectedDateTime;
    this.expectedDuration = expectedDuration;
    this.expectedUrlSmall = expectedUrlSmall;
    this.expectedUrlNormal = expectedUrlNormal;
    this.expectedUrlHd = expectedUrlHd;
    this.expectedADUrlSmall = expectedADUrlSmall;
    this.expectedADUrlNormal = expectedADUrlNormal;
    this.expectedADUrlHd = expectedADUrlHd;
    this.expectedDGSUrlSmall = expectedDGSUrlSmall;
    this.expectedDGSUrlNormal = expectedDGSUrlNormal;
    this.expectedDGSUrlHd = expectedDGSUrlHd;
    this.expectedSubtitle = expectedSubtitle;
    this.expectedGeo = expectedGeo;
    this.relatedFilms = relatedFilms;
    this.additionalSender = additionalSender;
    expectedFilmCount = 1;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            /*jsonFile*/ "/ard/ard_item_DGS_AD.json",
            /*topic*/ "Sesamstraße Magazin",
            /*title*/ "Die schlaflose Eule x",
            /*description*/ "2893. Ernie & Jan Delay bringen den müden Bert mit ihrem Lied um seinen nächtlichen Schlaf. In einer neuen Sesamstraßen Reihe nehmen uns Kinder mit an ihre \"Lieblingsorte\". Heute besuchen wir mit Elli (4), Jonathan (5) und Mattis (8) das nächtliche Miniaturwunderland. Außerdem hat der Prinz Probleme damit, Dornröschen zu wecken und Krümelmonster bringt Abby zur Verzweiflung. Susi Schraube erfindet\n.....",
            /*date*/ LocalDateTime.parse("2024-02-19T07:45"),
            /*duration*/ Duration.parse("PT20M26S"),
            /*small*/ "https://mediandr-a.akamaihd.net/progressive_geo/2021/0928/TV-20210928-1420-0200.ln.mp4",
            /*normal*/ "https://mediandr-a.akamaihd.net/progressive_geo/2021/0928/TV-20210928-1420-0200.hq.mp4",
            /*hd*/ "https://mediandr-a.akamaihd.net/progressive_geo/2021/0928/TV-20210928-1420-0200.hd.mp4",
            /*ADsmall*/ "https://mediandr-a.akamaihd.net/progressive_geo/2021/1001/TV-20211001-1625-4000.ln.mp4",
            /*ADnormal*/ "https://mediandr-a.akamaihd.net/progressive_geo/2021/1001/TV-20211001-1625-4000.hq.mp4",
            /*ADhd*/ "https://mediandr-a.akamaihd.net/progressive_geo/2021/1001/TV-20211001-1625-4000.hd.mp4",
            /*DGSsmall*/ "https://mediandr-a.akamaihd.net/progressive_geo/2022/0104/TV-20220104-0902-5000.ln.mp4",
            /*DGSnormal*/ "https://mediandr-a.akamaihd.net/progressive_geo/2022/0104/TV-20220104-0902-5000.hq.mp4",
            /*DGShd*/ "https://mediandr-a.akamaihd.net/progressive_geo/2022/0104/TV-20220104-0902-5000.hd.mp4",
            /*sub*/ "https://api.ardmediathek.de/player-service/subtitle/ebutt/urn:ard:subtitle:eaa2ed13a677cd00",
            /*hd*/ GeoLocations.GEO_DE,
            /*related*/ new ArdFilmInfoDto[0],
            /*sender*/ Optional.of(Sender.KIKA)
          },
          {
            /*jsonFile*/ "/ard/ard_item_DGS_UT_AD.json",
            /*topic*/ "Tatort",
            /*title*/ "Der Fluch des Geldes",
            /*description*/ "Spielfilm Deutschland 2024 +++    \"Der Fluch des Geldes\" beginnt da, wo \"Die Kälte der Erde\" endete. Die Hauptkommissare streiten sich, denn Leo Hölzer musste entdecken, dass sein Partner Adam Schürk im Besitz der Beute aus einem Bankraub seines verstorbenen Vaters ist. +++    Mit Vladimir Burlakov, Daniel Sträßer, Susanne Bormann, Omar El-Saeidi, Jasmina Al Zihairi u.a. | Buch: Hendrik Hölzemann \n.....",
            /*date*/ LocalDateTime.parse("2024-01-28T20:15"),
            /*duration*/ Duration.parse("PT1H28M33S"),
            /*small*/ "https://pd-videos.daserste.de/int/2024/01/18/3322bac1-6935-4101-8e41-380d70eff67e/JOB_430813_sendeton_640x360-50p-1200kbit.mp4",
            /*normal*/ "https://pd-videos.daserste.de/int/2024/01/18/3322bac1-6935-4101-8e41-380d70eff67e/JOB_430813_sendeton_1280x720-50p-3200kbit.mp4",
            /*hd*/ "https://pd-videos.daserste.de/int/2024/01/18/3322bac1-6935-4101-8e41-380d70eff67e/JOB_430813_sendeton_1920x1080-50p-5000kbit.mp4",
            /*ADsmall*/ "https://pd-videos.daserste.de/int/2024/01/18/3322bac1-6935-4101-8e41-380d70eff67e/JOB_430814_internationalerton_640x360-50p-1200kbit.mp4",
            /*ADnormal*/ "https://pd-videos.daserste.de/int/2024/01/18/3322bac1-6935-4101-8e41-380d70eff67e/JOB_430814_internationalerton_1280x720-50p-3200kbit.mp4",
            /*ADhd*/ "https://pd-videos.daserste.de/int/2024/01/18/3322bac1-6935-4101-8e41-380d70eff67e/JOB_430814_internationalerton_1920x1080-50p-5000kbit.mp4",
            /*DGSsmall*/ "https://pd-videos.daserste.de/int/2024/01/24/03247ab1-4dcc-427e-b577-a6ca25c1dffe/JOB_432151_sendeton_640x360-50p-1200kbit.mp4",
            /*DGSnormal*/ "https://pd-videos.daserste.de/int/2024/01/24/03247ab1-4dcc-427e-b577-a6ca25c1dffe/JOB_432151_sendeton_1280x720-50p-3200kbit.mp4",
            /*DGShd*/ "https://pd-videos.daserste.de/int/2024/01/24/03247ab1-4dcc-427e-b577-a6ca25c1dffe/JOB_432151_sendeton_1920x1080-50p-5000kbit.mp4",
            /*sub*/ "https://api.ardmediathek.de/player-service/subtitle/webvtt/urn:ard:subtitle:7b0043ec0b358eb8.vtt",
            /*hd*/ GeoLocations.GEO_NONE,
            /*related*/ new ArdFilmInfoDto[] {
                new ArdFilmInfoDto(
                    "Y3JpZDovL3dkci5kZS9CZWl0cmFnLThlNjczODVlLWZhZTktNDMwYi1iNzI1LTA0NjU1ZmRmMDljZQ",
                    String.format(ArdConstants.ITEM_URL, "Y3JpZDovL3dkci5kZS9CZWl0cmFnLThlNjczODVlLWZhZTktNDMwYi1iNzI1LTA0NjU1ZmRmMDljZQ"),
                    0),
                new ArdFilmInfoDto(
                    "Y3JpZDovL3dkci5kZS9CZWl0cmFnLXNvcGhvcmEtZWRmMTRhM2UtNmM3Ny00NGZhLTg1ZWYtYTJkYmZmNzM0NTg5",
                    String.format(ArdConstants.ITEM_URL, "Y3JpZDovL3dkci5kZS9CZWl0cmFnLXNvcGhvcmEtZWRmMTRhM2UtNmM3Ny00NGZhLTg1ZWYtYTJkYmZmNzM0NTg5"),
                    0)
              },
            /*sender*/ Optional.of(Sender.ARD)
          },
          {
            /*jsonFile*/ "/ard/ard_item_STD_BR.json",
            /*topic*/ "PULS Reportage",
            /*title*/ "Van-Urlaub für 1 Euro: Wie geht das?",
            /*description*/ "Bei einer Relocation kann man sich für 1 Euro einen Mietwagen ausleihen und muss ihn dafür nur pünktlich zum nächsten Abholort bringen. Leah mietet sich einen Luxus-Van, mit dem sie in drei Tagen nach Italien fährt: Taugt's wirklich als Urlaub?",
            /*date*/ LocalDateTime.parse("2024-05-01T15:00"),
            /*duration*/ Duration.parse("PT20M47S"),
            /*small*/ "https://cdn-storage.br.de/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_29S/_-OS/_2Ff5y4f9K1S/19712f56-684f-4bba-95aa-ae5a6331d67b_E.mp4",
            /*normal*/ "https://cdn-storage.br.de/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_29S/_-OS/_2Ff5y4f9K1S/19712f56-684f-4bba-95aa-ae5a6331d67b_X.mp4",
            /*hd*/ "https://cdn-storage.br.de/MUJIuUOVBwQIbtCCBLzGiLC1uwQoNA4p_29S/_-OS/_2Ff5y4f9K1S/19712f56-684f-4bba-95aa-ae5a6331d67b_HD.mp4",
            /*ADsmall*/ "",
            /*ADnormal*/ "",
            /*ADhd*/ "",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "",
            /*hd*/ GeoLocations.GEO_NONE,
            /*related*/ new ArdFilmInfoDto[] {
                new ArdFilmInfoDto(
                    "Y3JpZDovL2JyLmRlL3ZpZGVvLzkwZTA1Y2Y5LTA4ZDEtNGU4Zi1iNTQyLWNiYjIyYzcyZDA0Mw",
                    String.format(ArdConstants.ITEM_URL, "Y3JpZDovL2JyLmRlL3ZpZGVvLzkwZTA1Y2Y5LTA4ZDEtNGU4Zi1iNTQyLWNiYjIyYzcyZDA0Mw"),
                    0)
              },
            /*sender*/ Optional.of(Sender.BR)
          },
          {
            /*jsonFile*/ "/ard/ard_item_STD_DasErste.json",
            /*topic*/ "tagesschau",
            /*title*/ "tagesschau 11:10 Uhr, 01.05.2024",
            /*description*/ "Bundesweit Kundgebungen zum Tag der Arbeit für bessere Arbeitsbedingungen, Bereits erste Demonstrationen in Berlin und Hamburg, Polizei in Georgiens Hauptstadt Tiflis geht massiv gegen pro-europäische Demonstranten vor, US-Einsatzkräfte räumen mit Großaufgebot besetztes Gebäude der Columbia Universität in New York, US-Bestsellerautor Paul Auster stirbt im Alter von 77 Jahren, Walpurgisnacht im Har\n.....",
            /*date*/ LocalDateTime.parse("2024-05-01T11:10"),
            /*duration*/ Duration.parse("PT5M6S"),
            /*small*/ "https://media.tagesschau.de/video/2024/0501/TV-20240501-1117-3700.webm.h264.mp4",
            /*normal*/ "https://media.tagesschau.de/video/2024/0501/TV-20240501-1117-3700.webxl.h264.mp4",
            /*hd*/ "https://media.tagesschau.de/video/2024/0501/TV-20240501-1117-3700.webxxl.h264.mp4",
            /*ADsmall*/ "",
            /*ADnormal*/ "",
            /*ADhd*/ "",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "https://api.ardmediathek.de/player-service/subtitle/webvtt/urn:ard:subtitle:c09c9cee3bf53db8.vtt",
            /*hd*/ GeoLocations.GEO_NONE,
            /*related*/ new ArdFilmInfoDto[] {
                new ArdFilmInfoDto(
                    "Y3JpZDovL3RhZ2Vzc2NoYXUuZGUvNTBjOTc0OGUtMTIwYi00MjllLWI2ODEtZTkyMTY5ODEyNGI0X2dhbnplU2VuZHVuZw",
                    String.format(ArdConstants.ITEM_URL, "Y3JpZDovL3RhZ2Vzc2NoYXUuZGUvNTBjOTc0OGUtMTIwYi00MjllLWI2ODEtZTkyMTY5ODEyNGI0X2dhbnplU2VuZHVuZw"),
                    0)
              },
            /*sender*/ Optional.of(Sender.ARD),
          },
          {
            /*jsonFile*/ "/ard/ard_item_STD_HR.json",
            /*topic*/ "hallo hessen",
            /*title*/ "hallo hessen – Teil 1 vom 30.04.2024",
            /*description*/ "Der Mai steht vor der Tür und wir haben für Sie die perfekten Wandertipps. Los geht es heute mit dem herrlichen Rheingau. Außerdem wollen wir passend dazu auch die Grillsaison eröffnen. Und ein hessischer Erdbeerbauer verrät uns alles Wissenswerte um die roten Superfrüchtchen.",
            /*date*/ LocalDateTime.parse("2024-04-30T16:00"),
            /*duration*/ Duration.parse("PT46M"),
            /*small*/ "https://hrardmediathek-a.akamaihd.net/odinson/hallo-hessen/SVID-BC6574CF-5A5A-4D00-B8D3-44D241F14519/ebe1419f-59b2-47de-a430-74bde4fb25df/L489595_sendeton_640x360-50p-1200kbit.mp4",
            /*normal*/ "https://hrardmediathek-a.akamaihd.net/odinson/hallo-hessen/SVID-BC6574CF-5A5A-4D00-B8D3-44D241F14519/ebe1419f-59b2-47de-a430-74bde4fb25df/L489595_sendeton_1280x720-50p-3200kbit.mp4",
            /*hd*/ "https://hrardmediathek-a.akamaihd.net/odinson/hallo-hessen/SVID-BC6574CF-5A5A-4D00-B8D3-44D241F14519/ebe1419f-59b2-47de-a430-74bde4fb25df/L489595_sendeton_1920x1080-50p-5000kbit.mp4",
            /*ADsmall*/ "",
            /*ADnormal*/ "",
            /*ADhd*/ "",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "",
            /*hd*/ GeoLocations.GEO_NONE,
            /*related*/ new ArdFilmInfoDto[0],
            /*sender*/ Optional.of(Sender.HR),
          },
          {
            /*jsonFile*/ "/ard/ard_item_STD_NDR.json",
            /*topic*/ "NDR Info",
            /*title*/ "NDR Info 14:00 | 30.04.2024",
            /*description*/ "Die Nachrichten für den Norden: Suche nach Arian eingestellt / Viele Pflege-Ausbildungsplätze in Schleswig-Holstein unbesetzt",
            /*date*/ LocalDateTime.parse("2024-04-30T14:00"),
            /*duration*/ Duration.parse("PT10M19S"),
            /*small*/ "https://mediandr-a.akamaihd.net/progressive/2024/0430/TV-20240430-1400-3611.ln.mp4",
            /*normal*/ "https://mediandr-a.akamaihd.net/progressive/2024/0430/TV-20240430-1400-3611.hd.mp4",
            /*hd*/ "https://mediandr-a.akamaihd.net/progressive/2024/0430/TV-20240430-1400-3611.1080.mp4",
            /*ADsmall*/ "",
            /*ADnormal*/ "",
            /*ADhd*/ "",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "https://api.ardmediathek.de/player-service/subtitle/webvtt/urn:ard:subtitle:ea9ad6b71df1b8ed.vtt",
            /*hd*/ GeoLocations.GEO_NONE,
            /*related*/ new ArdFilmInfoDto[0],
            /*sender*/ Optional.of(Sender.NDR),
          },
          {
            /*jsonFile*/ "/ard/ard_item_STD_ONE.json",
            /*topic*/ "Murdoch Mysteries",
            /*title*/ "Folge 4: Geisterstunde (S01/E04) - (Originalversion)",
            /*description*/ "Murdoch schließt sich mit seinem Helden Arthur Conan Doyle zusammen, um einen Mord aufzuklären, der während einer Séance unter der Leitung des Mediums Sarah Pensall aufgedeckt wurde. Es scheint, dass das Opfer Ida Winston, Mitglied einer paranormalen Wächtergruppe, nicht von Sarahs Fähigkeiten überzeugt war. Murdoch fragt sich, ob Sarah Ida getötet hat, weil sie kurz davorstand, als Betrügerin ent\n.....",
            /*date*/ LocalDateTime.parse("2024-05-01T04:15"),
            /*duration*/ Duration.parse("PT46M4S"),
            /*small*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk12/310/3106351/3106351_57089636.mp4",
            /*normal*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk12/310/3106351/3106351_57089638.mp4",
            /*hd*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk12/310/3106351/3106351_57089634.mp4",
            /*ADsmall*/ "",
            /*ADnormal*/ "",
            /*ADhd*/ "",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "",
            /*hd*/ GeoLocations.GEO_DE,
            /*related*/ new ArdFilmInfoDto[0],
            /*sender*/ Optional.of(Sender.ONE),
          },
          {
            /*jsonFile*/ "/ard/ard_item_STD_RBB.json",
            /*topic*/ "Blue Moon",
            /*title*/ "Blue Moon vom 30.04.2024",
            /*description*/ "Den Rucksack auskippen, bloßstellende Fotos posten oder Beleidigungen nachrufen - fast jedes sechste Schulkind ist bei uns von Mobbing betroffen. Aber auch im Büro, an der Uni oder im Verein gibt es Mobbing! Diese Woche sprechen wir bei Fritz über das Thema Mobbing, alle Inhalte und Hilfsangebote findet ihr hier: fritz.de/mobbing. Claudia Kamieth will deshalb heute im Blue Moon von euch wissen: Wu\n.....",
            /*date*/ LocalDateTime.parse("2024-04-30T22:15"),
            /*duration*/ Duration.parse("PT1H43M15S"),
            /*small*/ "https://rbbmediapmdp-a.akamaihd.net/content/dd/f9/ddf9c4f0-2da1-45b9-812f-873f213ccbd5/eb2ed184-078d-11ef-b0da-02420a000df3_hd1080-avc360.mp4",
            /*normal*/ "https://rbbmediapmdp-a.akamaihd.net/content/dd/f9/ddf9c4f0-2da1-45b9-812f-873f213ccbd5/eb2ed184-078d-11ef-b0da-02420a000df3_hd1080-avc720.mp4",
            /*hd*/ "https://rbbmediapmdp-a.akamaihd.net/content/dd/f9/ddf9c4f0-2da1-45b9-812f-873f213ccbd5/eb2ed184-078d-11ef-b0da-02420a000df3_hd1080-avc1080.mp4",
            /*ADsmall*/ "",
            /*ADnormal*/ "",
            /*ADhd*/ "",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "",
            /*hd*/ GeoLocations.GEO_NONE,
            /*related*/ new ArdFilmInfoDto[0],
            /*sender*/ Optional.of(Sender.RBB),
          },
          {
            /*jsonFile*/ "/ard/ard_item_STD_AD_DasErste.json",
            /*topic*/ "Mord mit Aussicht",
            /*title*/ "Folge 3: Die Bestechlichen (S05/E03)",
            /*description*/ "Marie zögert noch bei Gisberts Angebot, zu ihm auf den Schweine-Hof zu ziehen. Willkommene Ablenkung bietet Marie eine Leiche im Heuballen auf Müller Schlichtings Wiese.  Der Tote ist Albert Appel, der mit seinem Bruder Kai einen Bauernhof betreibt und außerdem Schiedsrichter der Regionalliga ist. Die Ermittlungen führen in die Welt des Fußballs und zweier rivalisierender Clubs. Das Auftauchen ein\n.....",
            /*date*/ LocalDateTime.parse("2024-04-30T20:15"),
            /*duration*/ Duration.parse("PT47M20S"),
            /*small*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk12/310/3101569/3101569_56955169.mp4",
            /*normal*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk12/310/3101569/3101569_56955171.mp4",
            /*hd*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk12/310/3101569/3101569_56955167.mp4",
            /*ADsmall*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk12/310/3101569/3101569_57101825.mp4",
            /*ADnormal*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk12/310/3101569/3101569_57101827.mp4",
            /*ADhd*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk12/310/3101569/3101569_57101823.mp4",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "https://api.ardmediathek.de/player-service/subtitle/webvtt/urn:ard:subtitle:a1d11ac623c7d120.vtt",
            /*hd*/ GeoLocations.GEO_NONE,
            /*related*/ new ArdFilmInfoDto[0],
            /*sender*/ Optional.of(Sender.ARD),
          },
          {
            /*jsonFile*/ "/ard/ard_item_STD_AD_MDR.json",
            /*topic*/ "Auf schmaler Spur",
            /*title*/ "Reichsbahn-Oldies im Trend",
            /*description*/ "Mit gerade mal 27 Jahren ist Tobias Sambill Geschäftsführer des „Salzland Railservice“ in Bernburg. Seine Geschäftsidee: Er betreibt Güterverkehr mit alten Reichsbahnloks und ist ganz verrückt nach „Ludmilla“, Baujahr 1974, 3.000 PS Leistung. Ludmilla ist der Spitzname für Dieselloks der Baureihe 130, die ab 1970 aus der Sowjetunion zur Reichsbahn kamen.  Auch in Löbau ist der Nachwuchs schon in d\n.....",
            /*date*/ LocalDateTime.parse("2024-05-01T13:20"),
            /*duration*/ Duration.parse("PT29M31S"),
            /*small*/ "https://odgeomdr-a.akamaihd.net/mp4dyn2/a/FCMS-a66d00f7-05d6-4280-8dbe-9c81a47c8667-41dd60577440_a6.mp4",
            /*normal*/ "https://odgeomdr-a.akamaihd.net/mp4dyn2/a/FCMS-a66d00f7-05d6-4280-8dbe-9c81a47c8667-be7c2950aac6_a6.mp4",
            /*hd*/ "",
            /*ADsmall*/ "https://odgeomdr-a.akamaihd.net/mp4dyn2/2/FCMS-25d93e81-d91a-40f7-92a7-90b28b675943-41dd60577440_25.mp4",
            /*ADnormal*/ "https://odgeomdr-a.akamaihd.net/mp4dyn2/2/FCMS-25d93e81-d91a-40f7-92a7-90b28b675943-be7c2950aac6_25.mp4",
            /*ADhd*/ "",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "https://api.ardmediathek.de/player-service/subtitle/ebutt/urn:ard:subtitle:7d1c01087f8cae77",
            /*hd*/ GeoLocations.GEO_DE,
            /*related*/ new ArdFilmInfoDto[0],
            /*sender*/ Optional.of(Sender.MDR),
          },
          {
            /*jsonFile*/ "/ard/ard_item_STD_AD_SWR.json",
            /*topic*/ "Traumziele",
            /*title*/ "Im Herzen Italiens - Von den Abruzzen nach Kalabrien",
            /*description*/ "Der Apennin ist das Rückgrat und die Seele Italiens. Orte voller Geschichte und wilde Natur prägen das Land.",
            /*date*/ LocalDateTime.parse("2024-05-01T14:15"),
            /*duration*/ Duration.parse("PT43M55S"),
            /*small*/ "https://pdodswr-a.akamaihd.net/swrfernsehen/geo/de/traumziele/2029736.ml.mp4",
            /*normal*/ "https://pdodswr-a.akamaihd.net/swrfernsehen/geo/de/traumziele/2029736.xl.mp4",
            /*hd*/ "https://pdodswr-a.akamaihd.net/swrfernsehen/geo/de/traumziele/2029736.xxl.mp4",
            /*ADsmall*/ "https://pdodswr-a.akamaihd.net/swrfernsehen/geo/de/traumziele/2029736.audio_description.ml.mp4",
            /*ADnormal*/ "https://pdodswr-a.akamaihd.net/swrfernsehen/geo/de/traumziele/2029736.audio_description.xl.mp4",
            /*ADhd*/ "https://pdodswr-a.akamaihd.net/swrfernsehen/geo/de/traumziele/2029736.audio_description.xxl.mp4",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "",
            /*hd*/ GeoLocations.GEO_DE,
            /*related*/ new ArdFilmInfoDto[0],
            /*sender*/ Optional.of(Sender.SWR),
          }, 
          {
            /*jsonFile*/ "/ard/ard_item_ignore_OV.json",
            /*topic*/ "You shall not lie - Tödliche Geheimnisse",
            /*title*/ "Folge 4: Der Verrat (S01/E04)",
            /*description*/ "Zwischen Macarena und Iván, der jetzt von ihrer Schwangerschaft weiß, kommt es zu einer dramatischen Auseinandersetzung auf See. Erschöpft schwimmt der 18-Jährige an Land und beschuldigt sie, ihn vom Boot gestoßen zu haben. Die Lehrerin muss nun nicht nur die Folgen ihres Seitensprungs fürchten, sondern auch eine Anzeige wegen versuchten Totschlags.  +++    Sechsteilige Thrillerserie, Spanien 2021\n.....",
            /*date*/ LocalDateTime.parse("2022-10-15T02:50"),
            /*duration*/ Duration.parse("PT42M18S"),
            /*small*/ "https://pd-videos.daserste.de/de/2022/10/11/1cb6c5d8-c0f4-4868-ac42-2a06b7a3381f/JOB_461836_sendeton_640x360-50p-1200kbit.mp4",
            /*normal*/ "https://pd-videos.daserste.de/de/2022/10/11/1cb6c5d8-c0f4-4868-ac42-2a06b7a3381f/JOB_461836_sendeton_1280x720-50p-3200kbit.mp4",
            /*hd*/ "https://pd-videos.daserste.de/de/2022/10/11/1cb6c5d8-c0f4-4868-ac42-2a06b7a3381f/JOB_461836_sendeton_1920x1080-50p-5000kbit.mp4",
            /*ADsmall*/ "https://pd-videos.daserste.de/de/2022/10/11/1cb6c5d8-c0f4-4868-ac42-2a06b7a3381f/JOB_461837_internationalerton_640x360-50p-1200kbit.mp4",
            /*ADnormal*/ "https://pd-videos.daserste.de/de/2022/10/11/1cb6c5d8-c0f4-4868-ac42-2a06b7a3381f/JOB_461837_internationalerton_1280x720-50p-3200kbit.mp4",
            /*ADhd*/ "https://pd-videos.daserste.de/de/2022/10/11/1cb6c5d8-c0f4-4868-ac42-2a06b7a3381f/JOB_461837_internationalerton_1920x1080-50p-5000kbit.mp4",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "https://api.ardmediathek.de/player-service/subtitle/webvtt/urn:ard:subtitle:efab8bf55007171e.vtt",
            /*hd*/ GeoLocations.GEO_DE,
            /*related*/ new ArdFilmInfoDto[0],
            /*sender*/ Optional.of(Sender.ARD),
          },
          {
            /*jsonFile*/ "/ard/ard_item_OV.json",
            /*topic*/ "Murdoch Mysteries",
            /*title*/ "Folge 12: Der küssende Bandit (S04/E12) - (Originalversion)",
            /*description*/ "Während sich Dr. Ogden auf ihre Hochzeit vorbereitet, muss Murdoch versuchen, den 'Küssenden Banditen' aufzuhalten, einen umstrittenen Bankräuber, der schnell zum Volkshelden aufsteigt.",
            /*date*/ LocalDateTime.parse("2024-04-24T22:50"),
            /*duration*/ Duration.parse("PT45M44S"),
            /*small*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk12/310/3103788/3103788_57016296.mp4",
            /*normal*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk12/310/3103788/3103788_57016298.mp4",
            /*hd*/ "https://wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk12/310/3103788/3103788_57016294.mp4",
            /*ADsmall*/ "",
            /*ADnormal*/ "",
            /*ADhd*/ "",
            /*DGSsmall */ "",
            /*DGSnormal */ "",
            /*DGShd */ "",
            /*sub*/ "",
            /*hd*/ GeoLocations.GEO_DE,
            /*related*/ new ArdFilmInfoDto[0],
            /*sender*/ Optional.of(Sender.ONE),
          }
        });
  }

  @Test
  public void test() {

    final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

    final ArdFilmDeserializer target = new ArdFilmDeserializer(createCrawler());
    final List<ArdFilmDto> actualFilms = target.deserialize(jsonElement, null, null);
    final ArdFilmDto[] films = actualFilms.toArray(new ArdFilmDto[] {});
    if (additionalSender.get().equals(Sender.KIKA)) {
      // ignore kika
      assertThat(actualFilms.size(), equalTo(0));
    } else {
      assertThat(actualFilms.size(), equalTo(expectedFilmCount));
      AssertFilm.assertEquals(
          films[0].getFilm(),
          additionalSender.orElse(Sender.ARD),
          expectedTopic,
          expectedTitle,
          expectedDateTime,
          expectedDuration,
          expectedDescription,
          "", // website
          new GeoLocations[] {expectedGeo},
          expectedUrlSmall,
          expectedUrlNormal,
          expectedUrlHd,
          expectedDGSUrlSmall,
          expectedDGSUrlNormal,
          expectedDGSUrlHd,
          expectedADUrlSmall,
          expectedADUrlNormal,
          expectedADUrlHd,
          expectedSubtitle);
      assertThat(films[0].getRelatedFilms(), Matchers.containsInAnyOrder(relatedFilms));
    }
  }

  protected ArdCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ArdCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
