package de.mediathekview.mlib.daten;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ListeFilmeTest {

  @Test
  public void testUpdateListeIndex() {
    ListeFilme list = new ListeFilme();
    list.add(createTestFilm(Const.ZDF, "ZDFinfo Doku", "Ermittler! Fatale Verbindungen (S2025/E05)", "https://nrodlzdf-a.akamaihd.net/none/zdf/25/03/250303_ermittler_fatale_verbindungen_inf/1/250303_ermittler_fatale_verbindungen_inf_3360k_p36v17.mp4"));
    ListeFilme listOld = new ListeFilme();
    listOld.add(createTestFilm(Const.ZDF, "ZDFinfo Doku", "Ermittler! Fatale Verbindungen (S2025/E05)", "https://nrodlzdf-a.akamaihd.net/none/zdf/25/03/250303_ermittler_fatale_verbindungen_inf/1/250303_ermittler_fatale_verbindungen_inf_3360k_p36v17.mp4"));
    listOld.add(createTestFilm(Const.ZDF, "ZDFinfo Doku", "identische Url nicht hinzugefügt", "https://nrodlzdf-a.akamaihd.net/none/zdf/25/03/250303_ermittler_fatale_verbindungen_inf/1/250303_ermittler_fatale_verbindungen_inf_3360k_p36v17.mp4"));
    listOld.add(createTestFilm(Const.ZDF, "ZDFinfo Doku", "gleiche Url mit anderen Host nicht hinzugefügt", "https://rodlzdf-a.akamaihd.net/none/zdf/25/03/250303_ermittler_fatale_verbindungen_inf/1/250303_ermittler_fatale_verbindungen_inf_3360k_p36v17.mp4"));
    listOld.add(createTestFilm(Const.ZDF, "ZDFinfo Doku", "andere Url hinzugefügt", "https://nrodlzdf-a.akamaihd.net/none/zdf/25/02/250303_ermittler_fatale_verbindungen_inf/1/250303_ermittler_fatale_verbindungen_inf_3360k_p36v17.mp4"));

    list.updateListe(listOld, true, false);
    assertEquals(2, list.size());
  }
  @Test
  public void testUpdateListeUrl() {
    ListeFilme list = new ListeFilme();
    list.add(createTestFilm(Const.ZDF, "ZDFinfo Doku", "Ermittler! Fatale Verbindungen (S2025/E05)", "https://nrodlzdf-a.akamaihd.net/none/zdf/25/03/250303_ermittler_fatale_verbindungen_inf/1/250303_ermittler_fatale_verbindungen_inf_3360k_p36v17.mp4"));
    ListeFilme listOld = new ListeFilme();
    listOld.add(createTestFilm(Const.ZDF, "ZDFinfo Doku", "Ermittler! Fatale Verbindungen (S2025/E05)", "https://nrodlzdf-a.akamaihd.net/none/zdf/25/03/250303_ermittler_fatale_verbindungen_inf/1/250303_ermittler_fatale_verbindungen_inf_3360k_p36v17.mp4"));
    listOld.add(createTestFilm(Const.ZDF, "ZDFinfo Doku", "Ermittler! Fatale Verbindungen (S2025/E05)", "https://nrodlzdf-a.akamaihd.net/none/zdf/25/03/250303_ermittler_fatale_verbindungen_inf/1/250303_ermittler_fatale_verbindungen_inf_3360k_p36v17.mp4"));
    listOld.add(createTestFilm(Const.ZDF, "ZDFinfo Doku", "Ermittler! Fatale Verbindungen (S2025/E05)", "https://nrodlzdf-a.akamaihd.net/none/zdf/25/03/250303_ermittler_fatale_verbindungen_inf/1/250303_ermittler_fatale_verbindungen_inf_3360k_p36v17.mp4"));
    listOld.add(createTestFilm(Const.ZDF, "ZDFinfo Doku", "andere Episode", "https://nrodlzdf-a.akamaihd.net/none/zdf/25/02/250303_ermittler_fatale_verbindungen_inf/1/250303_ermittler_fatale_verbindungen_inf_3360k_p36v17.mp4"));

    list.updateListe(listOld, false, false);
    assertEquals(2, list.size());
  }

  private static DatenFilm createTestFilm(String sender, String topic, String title,
                                          String filmUrl) {
    DatenFilm film = new DatenFilm(sender, topic, "url", title, filmUrl, "", "", "", 12,
            "");
    film.arr[DatenFilm.FILM_GROESSE] = "10";

    return film;
  }

}