package de.mediathekview.mserver.crawler.funk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.funk.json.FunkChannelDeserializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class FunkChannelDeserializerTest {

  private final String jsonFile;
  private final PagedElementListDTO<FunkChannelDTO> correctResults;

  public FunkChannelDeserializerTest(
      final String jsonFile, final PagedElementListDTO<FunkChannelDTO> correctResults) {
    this.jsonFile = jsonFile;
    this.correctResults = correctResults;
  }

  @Parameterized.Parameters
  public static Object[][] data() {
    final PagedElementListDTO<FunkChannelDTO> channelList = new PagedElementListDTO<>();
    channelList.setNextPage(
        Optional.of(
            "https://www.funk.net/api/v4.0/channels/?page=1&size=100&sort=updateDate,desc"));
    channelList.addElements(
        Arrays.asList(
            new FunkChannelDTO("12034", "Deutschland3000 - ‘ne gute Stunde mit Eva Schulz"),
            new FunkChannelDTO("12026", "Same"),
            new FunkChannelDTO("12030", "DIE DA OBEN!"),
            new FunkChannelDTO("12004", "100percentme"),
            new FunkChannelDTO("12014", "OZON"),
            new FunkChannelDTO("11956", "alwaysxcaro"),
            new FunkChannelDTO("11097", "Boah Bergmann!"),
            new FunkChannelDTO("11943", "S.O.S. - Sick of Silence"),
            new FunkChannelDTO("11961", "PlusPlusPlus"),
            new FunkChannelDTO("940", "Kostas Kind"),
            new FunkChannelDTO("11942", "dimxoo"),
            new FunkChannelDTO("11090", "Kurzgesagt"),
            new FunkChannelDTO("11790", "DRUCK"),
            new FunkChannelDTO("11853", "reporter"),
            new FunkChannelDTO("1094", "Coldmirror"),
            new FunkChannelDTO("11953", "Okay"),
            new FunkChannelDTO("800", "Bohemian Browser Ballett"),
            new FunkChannelDTO("968", "offen un&#39; ehrlich"),
            new FunkChannelDTO("779", "Auf einen Kaffee mit Moritz Neumeier"),
            new FunkChannelDTO("11984", "Shapira Shapira"),
            new FunkChannelDTO("856", "Game Two"),
            new FunkChannelDTO("828", "Die Frage"),
            new FunkChannelDTO("1423", "Das schaffst du nie!"),
            new FunkChannelDTO("898", "iam.serafina"),
            new FunkChannelDTO("12027", "Vibes - Hugs & Hypes"),
            new FunkChannelDTO("6442", "Eugen Spanck"),
            new FunkChannelDTO("12018", "Franziska Schreiber"),
            new FunkChannelDTO("12023", "Kopfstimme"),
            new FunkChannelDTO("12011", "Patchwork Gangsta"),
            new FunkChannelDTO("11977", "funk Politik"),
            new FunkChannelDTO("12003", "Fayvourite Horse"),
            new FunkChannelDTO("12015", "flowingbody"),
            new FunkChannelDTO("1381", "Girl Cave"),
            new FunkChannelDTO("11954", "Bingo Flamingo"),
            new FunkChannelDTO("12009", "Korner"),
            new FunkChannelDTO("12005", "YeboahsVLOGS"),
            new FunkChannelDTO("1052", "WUMMS"),
            new FunkChannelDTO("12008", "Mordlust"),
            new FunkChannelDTO("11998", "littlerebelle"),
            new FunkChannelDTO("863", "GERMANIA"),
            new FunkChannelDTO("11997", "Goergy"),
            new FunkChannelDTO("11384", "STRG_F"),
            new FunkChannelDTO("12000", "REWIND"),
            new FunkChannelDTO("11991", "Doppelpunkt"),
            new FunkChannelDTO("11999", "nisi156"),
            new FunkChannelDTO("11996", "iss bunter"),
            new FunkChannelDTO("807", "Bongo Boulevard"),
            new FunkChannelDTO("11990", "Softie"),
            new FunkChannelDTO("11992", "Rebecca Gubitzer"),
            new FunkChannelDTO("912", "Jäger & Sammler"),
            new FunkChannelDTO("11995", "GLOWnatur"),
            new FunkChannelDTO("786", "Auf Klo"),
            new FunkChannelDTO("1164", "Doctor Who"),
            new FunkChannelDTO("1199", "The Job Lot"),
            new FunkChannelDTO("3299", "Threesome"),
            new FunkChannelDTO("11955", "Fashion Future Berlin"),
            new FunkChannelDTO("11985", "#Move2"),
            new FunkChannelDTO("884", "Rayk Anders"),
            new FunkChannelDTO("11981", "Hookline"),
            new FunkChannelDTO("11867", "Klicknapped"),
            new FunkChannelDTO("738", "Kliemannsland"),
            new FunkChannelDTO("11982", "Mädelsabende"),
            new FunkChannelDTO("742", "Wishlist"),
            new FunkChannelDTO("793", "B.A."),
            new FunkChannelDTO("814", "Datteltäter"),
            new FunkChannelDTO("821", "Der Wedding kommt"),
            new FunkChannelDTO("842", "Finalclash"),
            new FunkChannelDTO("877", "Guten Morgen, Internet!"),
            new FunkChannelDTO("905", "INFORMR"),
            new FunkChannelDTO("926", "Simon Will"),
            new FunkChannelDTO("947", "LiDiRo"),
            new FunkChannelDTO("996", "maiLab"),
            new FunkChannelDTO("1031", "Walulis"),
            new FunkChannelDTO("1045", "World Wide Wohnzimmer"),
            new FunkChannelDTO("1059", "Y-Kollektiv"),
            new FunkChannelDTO("1066", "musstewissen"),
            new FunkChannelDTO("1073", "King of Westberg"),
            new FunkChannelDTO("1171", "funk Life"),
            new FunkChannelDTO("1332", "follow me.reports"),
            new FunkChannelDTO("1570", "Deutschland3000"),
            new FunkChannelDTO("7751", "plan&los"),
            new FunkChannelDTO("8423", "MrWissen2go"),
            new FunkChannelDTO("8920", "Country Girls"),
            new FunkChannelDTO("9613", "Koch ma!"),
            new FunkChannelDTO("10040", "Phil Laude"),
            new FunkChannelDTO("11146", "Hit and Run"),
            new FunkChannelDTO("11377", "OFFSCREEN - die Serie"),
            new FunkChannelDTO("11545", "Bubbles"),
            new FunkChannelDTO("11566", "Carola Christmas"),
            new FunkChannelDTO("11664", "Cinema Strikes Back"),
            new FunkChannelDTO("11783", "FREAKS"),
            new FunkChannelDTO("11881", "Pen&Paper"),
            new FunkChannelDTO("11916", "die wohngemeinschaft"),
            new FunkChannelDTO("11917", "WEIL ISSO"),
            new FunkChannelDTO("11926", "Schruppert"),
            new FunkChannelDTO("11940", "Manu Thiele"),
            new FunkChannelDTO("11964", "WACH"),
            new FunkChannelDTO("11965", "Straight Family"),
            new FunkChannelDTO("11966", "mynoupa"),
            new FunkChannelDTO("11967", "Einigkeit & Rap & Freiheit")));
    return new Object[][] {{"/funk/FunkChannels.json", channelList}};
  }

  @Test
  public void testDeserialize() throws URISyntaxException, IOException {
    final Type funkChannelsType = new TypeToken<PagedElementListDTO<FunkChannelDTO>>() {}.getType();
    final MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");
    final MServerBasicConfigDTO senderConfig = rootConfig.getSenderConfig(Sender.FUNK);
    senderConfig.setMaximumSubpages(2);
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(funkChannelsType, new FunkChannelDeserializer(senderConfig))
            .create();

    final PagedElementListDTO<FunkChannelDTO> channelList =
        gson.fromJson(
            Files.newBufferedReader(Paths.get(getClass().getResource(jsonFile).toURI())),
            funkChannelsType);
    assertThat(channelList, equalTo(correctResults));
  }
}
