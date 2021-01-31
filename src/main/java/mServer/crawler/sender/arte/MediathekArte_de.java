/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mServer.crawler.sender.arte;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MediathekArte_de extends MediathekReader {

  /*
     * Informationen zu den ARTE-URLs:
     * {} sind nur Makierungen, dass es Platzhalter sind, sie gehören nicht zur URL.
     * 
     * Allgemeine URL eines Films:  (050169-002-A = ID des Films); (die-spur-der-steine = Titel)
     * http://www.arte.tv/de/videos/{050169-002-A}/{die-spur-der-steine}
     * 
     * Alle Sendungen: (Deutsch = DE; Französisch = FR)
     * https://api.arte.tv/api/opa/v3/videos?channel={DE}
     * 
     * Informationen zum Film: (050169-002-A = ID des Films); (de für deutsch / fr für französisch)
     * https://api.arte.tv/api/player/v1/config/{de}/{050169-002-A}?platform=ARTE_NEXT
     * 
     * Zweite Quelle für Informationen zum Film: (050169-002-A = ID des Films); (de für deutsch / fr für französisch)
     * https://api.arte.tv/api/opa/v3/programs/{de}/{050169-002-A}
     * 
     * Hintergrundinfos zum Laden der Filme nach Kategorien im langen Lauf:
     * 1. statische Informationen über verfügbare Kategorien laden: URL_STATIC_CONTENT
     * 2. für jede Kategorie die Unterkategorien ermitteln: URL_CATEGORY
     * 3. für jede Unterkategorie die enthaltenen ProgramId ermitteln: URL_SUBCATEGORY
     * 4. für alle ProgramIds die Videoinformationen laden (wie kurze Variante)
   */
  private static final Logger LOG = LogManager.getLogger(MediathekArte_de.class);
  protected final static String SENDERNAME = Const.ARTE_DE;
  private static final String ARTE_API_TAG_URL_PATTERN = "https://api.arte.tv/api/opa/v3/videos?channel=%s&arteSchedulingDay=%s";

  private static final String URL_SUBCATEGORY
          = "https://www.arte.tv/guide/api/emac/v3/%s/web/data/MOST_RECENT_SUBCATEGORY/?subCategoryCode=%s&page=%s&limit=50";

  private static final String[] SUBCATEGORIES = new String[]{
    "WEB", "AUT",
    "AJO", "AUV", "KUL", "DCY", "ENQ", "JUN",
    "ACC", "CMG", "FLM", "CMU", "MCL",
    "CHU", "FIC", "SES",
    "ART", "POP", "IDE",
    "ADS", "BAR", "CLA", "JAZ", "MUA", "MUD", "OPE", "MUE", "HIP", "MET",
    "ENB", "ENN", "SAN", "TEC",
    "ATA", "EVA", "NEA", "VIA",
    "CIV", "LGP", "XXE"
  };

  private static final DateTimeFormatter ARTE_API_DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final boolean PARSE_SUBCATEGORY_SUB_PAGES = false; // Flag, ob Unterseiten der Unterkategorien verarbeitet werden soll

  protected String LANG_CODE = "de";
  protected String URL_CONCERT = "http://concert.arte.tv/de/videos/all";
  protected String URL_CONCERT_NOT_CONTAIN = "-STF";
  protected String TIME_1 = "<li>Sendetermine:</li>";
  protected String TIME_2 = "um";

  public MediathekArte_de(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME,/* threads */ 2, /* urlWarten */ 200, startPrio);
  }

  public MediathekArte_de(FilmeSuchen ssearch, int startPrio, String name) {
    super(ssearch, name,/* threads */ 2, /* urlWarten */ 200, startPrio);
  }

  //===================================
  // public
  //===================================
  @Override
  public void addToList() {
    meldungStart();
    if (Config.getStop()) {
      meldungThreadUndFertig();
    } else {
      if (CrawlerTool.loadLongMax()) {
        addCategories();
        meldungAddMax(listeThemen.size());

        for (int t = 0; t < getMaxThreadLaufen(); ++t) {
          Thread th = new CategoryLoader(this.getSendername());
          th.setName(getSendername() + t);
          th.start();
        }

      } else {
        addTage();
        meldungAddMax(listeThemen.size());
        for (int t = 0; t < getMaxThreadLaufen(); ++t) {
          Thread th = new ThemaLaden();
          th.setName(getSendername() + t);
          th.start();
        }
      }
    }
  }

  private void addCategories() {
    for (String subCategory : SUBCATEGORIES) {
      String subCategoryUrl = String.format(URL_SUBCATEGORY, LANG_CODE.toLowerCase(), subCategory, 1);
      listeThemen.add(new String[]{subCategory, subCategoryUrl});
    }
  }

  private void addTage() {
    // http://www.arte.tv/guide/de/plus7/videos?day=-2&page=1&isLoading=true&sort=newest&country=DE
    for (int i = 0; i <= 14; ++i) {
      String u = String.format(ARTE_API_TAG_URL_PATTERN, LANG_CODE.toUpperCase(), LocalDate.now().minusDays(i).format(ARTE_API_DATEFORMATTER));
      listeThemen.add(new String[]{u});
    }
    for (int i = 1; i <= 21; ++i) {
      String u = String.format(ARTE_API_TAG_URL_PATTERN, LANG_CODE.toUpperCase(), LocalDate.now().plusDays(i).format(ARTE_API_DATEFORMATTER));
      listeThemen.add(new String[]{u});
    }
  }

  class ThemaLaden extends Thread {

    private final Gson gson;

    public ThemaLaden() {
      gson = new GsonBuilder().registerTypeAdapter(ListeFilme.class, new ArteDatenFilmDeserializer(LANG_CODE, getSendername())).create();
    }

    @Override
    public void run() {
      try {
        meldungAddThread();
        String[] link;
        while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
          meldungProgress(link[0]);
          addFilmeForTag(link[0]);
        }
      } catch (Exception ex) {
        Log.errorLog(894330854, ex, "");
      }
      meldungThreadUndFertig();
    }

    private void addFilmeForTag(String aUrl) {

      ListeFilme loadedFilme = ArteHttpClient.executeRequest(SENDERNAME, LOG, gson, aUrl, ListeFilme.class);
      if (loadedFilme != null) {
        loadedFilme.forEach(film -> addFilm(film));
      }
    }
  }

  /**
   * Lädt die Filme für jede Kategorie
   */
  class CategoryLoader extends Thread {

    private final String sender;

    public CategoryLoader(String sender) {
      this.sender = sender;
    }

    @Override
    public void run() {
      try {
        meldungAddThread();
        String[] link;
        while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
          meldungProgress(link[0] + "/" + link[1] /* url */);
          loadSubCategory(link[0], link[1]);
        }
      } catch (Exception ex) {
        Log.errorLog(894330854, ex, "");
      }
      meldungThreadUndFertig();
    }

    private void loadSubCategory(String aCategory, String aUrl) {
      Gson gson = new GsonBuilder().registerTypeAdapter(ArteCategoryFilmsDTO.class, new ArteCategoryFilmListDeserializer()).create();

      // erste Seite laden
      int i = 2;
      ArteCategoryFilmsDTO dto = loadSubCategoryPage(gson, aUrl);
      if (dto != null) {
        ArteCategoryFilmsDTO nextDto = dto;
        while (PARSE_SUBCATEGORY_SUB_PAGES && nextDto != null && nextDto.hasNextPage()) {

          // weitere Seiten laden und zu programId-liste des ersten DTO hinzufügen
          String url = String.format(URL_SUBCATEGORY, LANG_CODE.toLowerCase(), aCategory, i);
          nextDto = loadSubCategoryPage(gson, url);
          if (nextDto != null) {
            nextDto.getProgramIds().forEach(programId -> dto.addProgramId(programId));
          }

          i++;
        }

        // alle programIds verarbeiten
        ListeFilme loadedFilme = loadPrograms(dto);
        loadedFilme.forEach((film) -> addFilm(film));
        Log.sysLog(String.format("%s: Subcategory %s: %d Filme", sender, aCategory, loadedFilme.size()));
      }
    }

    private ListeFilme loadPrograms(ArteCategoryFilmsDTO dto) {
      ListeFilme listeFilme = new ListeFilme();

      Collection<DatenFilm> futureFilme = new ArrayList<>();
      dto.getProgramIds().forEach(programId -> {
        try {
          Set<DatenFilm> films = new ArteProgramIdToDatenFilmCallable(programId, LANG_CODE, getSendername()).call();
          for (DatenFilm film : films) {
            futureFilme.add(film);
          }
        } catch (Exception exception) {
          LOG.error("Es ist ein Fehler beim lesen der Arte Filme aufgetreten.", exception);
        }
      });

      final List<DatenFilm> list = futureFilme.parallelStream()
              .filter(Objects::nonNull)
              .collect(Collectors.toList());
      listeFilme.addAll(list);
      list.clear();

      return listeFilme;
    }

    private ArteCategoryFilmsDTO loadSubCategoryPage(Gson gson, String aUrl) {
      return ArteHttpClient.executeRequest(SENDERNAME, LOG, gson, aUrl, ArteCategoryFilmsDTO.class);
    }
  }
}
