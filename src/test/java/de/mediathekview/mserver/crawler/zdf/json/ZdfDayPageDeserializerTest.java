package de.mediathekview.mserver.crawler.zdf.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.zdf.ZdfEntryDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ZdfDayPageDeserializerTest {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "/zdf/zdf_day_page_single.json",
            new ZdfEntryDto[]{
                new ZdfEntryDto("https://api.zdf.de/content/documents/olympia-im-technikwahn-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_technologien_neu_spo"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/gestrandet-102.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/tivi/150908_meerjungfrau_folge11_ham"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/im-dialog-vom-23022018-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180223_phx_dialog"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/augstein--blome-vom-23022018-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180223_phx_bib_augstein"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/menschen---das-magazin-vom-24-februar-2018-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_sendung_mdm"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/die-orakel-krake-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/tivi/150908_meerjungfrau_folge10_ham"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/siegerehrung-maenner-staffel-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_siegerehrung_staffel_spo"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/siegerehrung-vom-parallelslalom-der-frauen-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_sieger_snowboard_spo")
            },
            Optional.empty()
        },
        {
            "/zdf/zdf_day_page_multiple1.json",
            new ZdfEntryDto[]{
                new ZdfEntryDto("https://api.zdf.de/content/documents/plan-b-die-multi-kulti-macher-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_integration_plb"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/grippewelle-weitet-sich-aus-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_delgado_lsp"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/lausitz-fuerchtet-wirtschaftlichen-ruin-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_kelch_lsp"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/einblick-in-bayerns-heimatministerium-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_poel_lsp"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/neonazis-auf-dem-rueckzug-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_hass_lsp"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/hammer-der-woche-glasfaserkabel-doppelt-verlegt-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_hammer_lsp"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/laenderspiegel-vom-24-februar-2018-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_sendung_lsp"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/deutschlandreise-nach-pellworm-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_bernd_lsp"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/christian-ehrhoff-fahnentraeger-bei-schlussfeier-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_ehrhoff_spo"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/solange-du-wild-bist-102.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/tivi/140408_wilde_kerle_folge26_dwk"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/leons-hoehenflug-102.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/tivi/140408_wilde_kerle_folge25_dwk"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/porsche-modellauto-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_auto_bfr"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/silberbecher-106.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_silberbecher_bfr"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/halskette-102.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_kette_bfr"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/petroleumleuchter-102.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_leuchter_bfr"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/bares-fuer-rares-vom-27-september-2016-102.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_sendung_bfr"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/brillantring-126.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_ring_bfr"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/leica-kamera-102.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_kamera_bfr"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/snowboard-parallel-riesenslalom-in-der-zusammenfassung-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180224_snowboard_neu_sst"),
                new ZdfEntryDto("https://api.zdf.de/content/documents/nick-raeumt-auf-100.json",
                    "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/tivi/170717_folge_35_raeumt_auf_nic")
            },
            Optional.of(WireMockTestBase.MOCK_URL_BASE
                + "/search/documents?hasVideo=true&q=*&types=page-video&sortOrder=desc&from=2018-02-24T12%3A00%3A00.000%2B01%3A00&sortBy=date&to=2018-02-24T18%3A00%3A00.878%2B01%3A00&page=2")
        }
    });
  }

  private final ZdfDayPageDeserializer target;

  private final String jsonFile;
  private final ZdfEntryDto[] expectedEntries;
  private final Optional<String> expectedNextPageUrl;

  public ZdfDayPageDeserializerTest(final String aJsonFile, final ZdfEntryDto[] aExpectedEntries,
      final Optional<String> aExpectedNextPageUrl) {
    target = new ZdfDayPageDeserializer();

    jsonFile = aJsonFile;
    expectedEntries = aExpectedEntries;
    expectedNextPageUrl = aExpectedNextPageUrl;
  }

  @Test
  public void deserializeTest() {
    final JsonObject json = JsonFileReader.readJson(jsonFile);

    ZdfDayPageDto actual = target.deserialize(json, ZdfDayPageDto.class, null);

    assertThat(actual, notNullValue());
    assertThat(actual.getNextPageUrl(), equalTo(expectedNextPageUrl));
    assertThat(actual.getEntries().size(), equalTo(expectedEntries.length));
    assertThat(actual.getEntries(), Matchers.containsInAnyOrder(expectedEntries));
  }
}
