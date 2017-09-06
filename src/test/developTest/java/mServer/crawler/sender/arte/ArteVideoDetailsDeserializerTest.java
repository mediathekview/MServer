package mServer.crawler.sender.arte;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.gson.JsonObject;

import mServer.crawler.sender.newsearch.GeoLocations;
import mServer.test.JsonFileReader;

@RunWith(Parameterized.class)
public class ArteVideoDetailsDeserializerTest {
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "/arte/arte_video_details_first_several_minors_geo_defr.json", "Kino - Filme", "Detective Dee und der Fluch des Seeungeheuers", "Raffiniertes Fantasy-Spektakel über die Lehrjahre eines chinesischen Sherlock Holmes: Das China der Tang-Dynastie wird nicht nur von ausländischen Feinden angegriffen, sondern auch von einem geheimnisvollen Seeungeheuer bedroht. Auf den Spuren einer großangelegten Intrige wird das Können des jungen Detektivs Dee (Mark Chao) auf eine harte Probe gestellt.", "http://www.arte.tv/de/videos/068339-000-A/detective-dee-und-der-fluch-des-seeungeheuers", "2017-07-10T18:15:00Z", GeoLocations.GEO_DE_FR },
            { "/arte/arte_video_details_several_majors_minors_geo_null.json", "Geschichte - Die Zeit vor dem 20. Jahrhundert", "Griff nach der Weltherrschaft - Ferdinand Magellan", "Ferdinand Magellan und Sir Francis Drake sind zwei faszinierende Entdeckergestalten der Weltgeschichte. Sie kämpften für ihre Nation um die Vorherrschaft in der \"Neuen Welt\". In dieser Folge: Magellan wollte den Spaniern helfen, die Kontrolle des Gewürzhandels zu übernehmen, indem er eine neue Route zu den Gewürzinseln suchte, die nicht durch portugiesische Gewässer führte.", "http://www.arte.tv/de/videos/041863-001-A/griff-nach-der-weltherrschaft", "2017-08-01T16:25:00Z", GeoLocations.GEO_NONE },
            { "/arte/arte_video_details_first_with_catchuprights_past_geo_sat.json", "Kino - Kurzfilme", "Paare - Friederike Kempter/Mehdi Nebou", "Die erfolgreiche Therapiestunde geht weiter: Paare bei ARTE auf der Couch.\nDiesmal deutsch-französische und internationale Stars, \ndie sich schonungslos vor der Kamera des Therapeuten offenbaren. In dieser Folge: Friederike Kempter/Mehdi Nebou", "http://www.arte.tv/de/videos/071363-001-A/paare", "2017-05-22T11:36:00Z", GeoLocations.GEO_DE_AT_CH_EU },
            { "/arte/arte_video_details_first_without_catchuprights_geo_eudefr.json", "Kultur und Pop - Popkultur", "British Style - Anglicism", "Wie kleidet sich Großbritannien? Mode-Kenner Loïc Prigent veranschaulicht in einer Dokumentation und sechs Kurzbeiträgen, was die Mode Großbritanniens so bunt, erstaunlich, manchmal auch vergnüglich und einzigartig macht. ARTE strahlt die kurzen Dokumentationen vom 16. Juli bis 20. August jeweils Sonntags in der Nacht aus.", "http://www.arte.tv/de/videos/074849-004-A/british-style", "2017-07-16T23:30:00Z", GeoLocations.GEO_DE_FR },
            { "/arte/arte_video_details_no_broadcastprogrammings_geo_all.json", "Aktuelles und Gesellschaft - Reportagen und Recherchen", "Syrien: Die Schlacht um Raqqa - ARTE Reportage", "Die kurdischen Kämpfer der Koalition kämpfen in Raqqa unter dem Banner der sogenannten Demokratischen Kräfte Syriens, einer Armee von 30 000 Soldatinnen und Soldaten, unterstützt von den Luftschlägen der Internationalen Koalition. Erster Etappensieg war die Rückeroberung des Staudamms von Tabqa, 40 Kilometer vor der Stadt. ", "http://www.arte.tv/de/videos/076465-000-A/syrien-die-schlacht-um-raqqa", "2017-06-30T13:00:00Z", GeoLocations.GEO_NONE },
            { "/arte/arte_video_details_no_broadcastprogrammings_nocatchuprights.json", "Fernsehfilme und Serien - Serien", "Absolutely Fabulous 20-Jahre-Special: Identity", "Patsy hat ein Identitätsproblem. Nachdem sie jahrelang immer ihr wahres Alter verleugnet hat, fällt ihr nun nicht mehr ein wie alt sie eigentlich ist. Freundin Edina hilft ihr natürlich gerne bei der Suche nach der eigenen Identität. Deren Tochter Saffy, die aus dem Gefängnis entlassen wurde, hat derweil mit anderen Problemen zu kämpfen...", "http://www.arte.tv/de/videos/076845-002-A/absolutely-fabulous-20-jahre-special-identity", "2017-07-13T14:08:00Z", GeoLocations.GEO_NONE },
            { "/arte/arte_video_details_major_with_catchuprights_past.json", "Wissenschaft - Gesundheit und Medizin", "Alkoholsucht: Wundermittel Baclofen?", "Baclofen ist ein Medikament aus der Gruppe der Muskelrelaxantien. Könnte es auch ein Allheilmittel gegen Alkoholismus sein? Die Therapie ist ein Zufallsprodukt und in der Medizinwelt heftig umstritten. Zur Entstehungszeit der Doku war Baclofen noch nicht zugelassen. Ein Jahr lang hat ein Kamerateam Patienten und Ärzte während der ersten großen klinischen Studie begleitet.", "http://www.arte.tv/de/videos/047927-000-A/alkoholsucht-wundermittel-baclofen", "2017-05-20T20:40:00Z", GeoLocations.GEO_DE_FR },
        });
    }
    
    private final String jsonFile;
    private final String expectedBroadcastBegin;
    private final String expectedTheme;
    private final String expectedTitle;
    private final String expectedWebsite;
    private final String expectedDescription;
    private final GeoLocations geo;

    public ArteVideoDetailsDeserializerTest(String aJsonFile, String aTheme, String aTitle, String aDescription, String aWebsite, String aExpectedBroadcastBegin, GeoLocations aGeo) {
        this.jsonFile = aJsonFile;
        this.expectedBroadcastBegin = aExpectedBroadcastBegin;
        this.expectedTheme = aTheme;
        this.expectedTitle = aTitle;
        this.expectedDescription = aDescription;
        this.expectedWebsite = aWebsite;
        this.geo = aGeo;
    }
    
    @Test
    public void testDeserialize() {
        
        JsonObject jsonObject = JsonFileReader.readJson(jsonFile);
        
        Calendar today = Calendar.getInstance();
        today.set(2017, 6, 11); // 11.07.2017 als heute verwenden
        
        ArteVideoDetailsDeserializer target = new ArteVideoDetailsDeserializer(today);
        ArteVideoDetailsDTO actual = target.deserialize(jsonObject, ArteVideoDetailsDTO.class, null);
        
        assertThat(actual, notNullValue());
        assertThat(actual.getTheme(), equalTo(expectedTheme));
        assertThat(actual.getTitle(), equalTo(expectedTitle));
        assertThat(actual.getDescription(), equalTo(expectedDescription));
        assertThat(actual.getWebsite(), equalTo(expectedWebsite));
        assertThat(actual.getBroadcastBegin(), equalTo(expectedBroadcastBegin));
        assertThat(actual.getGeoLocation(), equalTo(geo));
    }
    
}
