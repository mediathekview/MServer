package mServer.crawler.sender.arte;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MediathekArte extends MediathekReader {

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
  private static final Logger LOG = LogManager.getLogger(MediathekArte.class);
  private static final String ARTE_API_TAG_URL_PATTERN = "https://api.arte.tv/api/opa/v3/videos?channel=%s&arteSchedulingDay=%s";

  private static final String URL_CATEGORY = "https://www.arte.tv/api/rproxy/emac/v4/%s/web/pages/%s";

  private static final String[] CATEGORIES = {
          "ARS",
          "DOR",
          "CIN",
          "SER",
          "ACT",
          "CPO",
          "SCI",
          "DEC",
          "HIS"
  };

  private static final String COLLECTION_URL = "https://api.arte.tv/api/opa/v3/programs/%s/%s";

  private static final DateTimeFormatter ARTE_API_DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public static final String ARTE_EN = "ARTE.EN";
  public static final String ARTE_ES = "ARTE.ES";
  public static final String ARTE_IT = "ARTE.IT";
  public static final String ARTE_PL = "ARTE.PL";

  private final Map<String, String> senderLanguages = new HashMap<>();

  public MediathekArte(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, Const.ARTE_DE,/* threads */ 2, /* urlWarten */ 200, startPrio);
  }


  public MediathekArte(FilmeSuchen ssearch, int startPrio, String name) {
    super(ssearch, name,/* threads */ 2, /* urlWarten */ 200, startPrio);
  }

  @Override
  protected synchronized void meldungStart() {
    super.meldungStart();

    senderLanguages.put(Const.ARTE_DE, "de");
  //  senderLanguages.put(Const.ARTE_FR, "fr");
    /*if (LocalDate.now().getDayOfYear() % 2 == 0) {
      senderLanguages.put(ARTE_EN, "en");
      senderLanguages.put(ARTE_ES, "es");
    } else {
      senderLanguages.put(ARTE_IT, "it");
      senderLanguages.put(ARTE_PL, "pl");
    }*/

    // starte Sprachen Sender, da es sonst zu doppelten Sendern kommen kann
    senderLanguages.keySet().forEach(sender -> mlibFilmeSuchen.melden(sender, getMax(), getProgress(), ""));
  }

  @Override
  protected synchronized void meldungThreadUndFertig() {
    // der MediathekReader ist erst fertig wenn nur noch ein Thread läuft
    // dann zusätzliche Sender, die der Crawler bearbeitet, beenden
    if (getThreads() <= 1) {
      senderLanguages.keySet().stream()
              // DE nicht beenden, das erfolgt durch den Aufruf der Basisklasse
              .filter(sender -> !sender.equals(Const.ARTE_DE))
              .forEach(sender -> mlibFilmeSuchen.meldenFertig(sender));
    }

    super.meldungThreadUndFertig();
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
          Thread th = new CategoryLoader();
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
    senderLanguages.forEach((sender, langCode) -> {
      for (String category : CATEGORIES) {
        String categoryUrl = String.format(URL_CATEGORY, langCode.toLowerCase(), category);
        listeThemen.add(new String[]{sender, langCode, category, categoryUrl});
      }
    });
  }

  private void addTage() {
    senderLanguages.forEach((sender, langCode) -> {
      // http://www.arte.tv/guide/de/plus7/videos?day=-2&page=1&isLoading=true&sort=newest&country=DE
      for (int i = 0; i <= 14; ++i) {
        String u = String.format(ARTE_API_TAG_URL_PATTERN, langCode.toUpperCase(), LocalDate.now().minusDays(i).format(ARTE_API_DATEFORMATTER));
        listeThemen.add(new String[]{sender, u});
      }
      for (int i = 1; i <= 21; ++i) {
        String u = String.format(ARTE_API_TAG_URL_PATTERN, langCode.toUpperCase(), LocalDate.now().plusDays(i).format(ARTE_API_DATEFORMATTER));
        listeThemen.add(new String[]{sender, u});
      }
    });
  }

  class ThemaLaden extends Thread {

    private final Map<String, Gson> senderGsonMap;

    public ThemaLaden() {
      senderGsonMap = new HashMap<>();
      senderLanguages.forEach((sender, language) -> senderGsonMap.put(sender, new GsonBuilder().registerTypeAdapter(ListeFilme.class, new ArteDatenFilmDeserializer(language, sender)).create()));
    }

    @Override
    public void run() {
      try {
        meldungAddThread();
        String[] link;
        while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
          meldungProgress(link[1]);
          addFilmeForTag(link[0], link[1]);
        }
      } catch (Exception ex) {
        Log.errorLog(894330854, ex, "");
      }
      meldungThreadUndFertig();
    }

    private void addFilmeForTag(String sender, String aUrl) {

      ListeFilme loadedFilme = ArteHttpClient.executeRequest(sender, LOG, senderGsonMap.get(sender), aUrl, ListeFilme.class);
      if (loadedFilme != null) {
        loadedFilme.forEach(film -> addFilm(film));
      }
    }
  }

  /**
   * Lädt die Filme für jede Kategorie
   */
  class CategoryLoader extends Thread {


    @Override
    public void run() {
      try {
        meldungAddThread();
        String[] link;
        while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
          meldungProgress(link[2] + "/" + link[3] /* url */);
          loadCategory(link[0], link[1], link[2], link[3]);
        }
      } catch (Exception ex) {
        Log.errorLog(894330854, ex, "");
      }
      meldungThreadUndFertig();
    }

    private void loadCategory(String sender, String langCode, String aCategory, String aUrl) {
      Gson gson = new GsonBuilder()
              .registerTypeAdapter(ArteCategoryFilmsDTO.class, new ArteCategoryFilmListDeserializer())
              .create();
      Gson gsonCollectionParent = new GsonBuilder()
              .registerTypeAdapter(ArteCategoryFilmsDTO.class, new ArteCollectionParentDeserializer())
              .create();
      Gson gsonCollectionChild = new GsonBuilder()
              .registerTypeAdapter(ArteCategoryFilmsDTO.class, new ArteCollectionChildDeserializer())
              .create();

      ArteCategoryFilmsDTO dto = loadSubCategoryPage(gson, sender, aUrl);
      if (dto != null) {
        loadCollections(sender, langCode, gsonCollectionParent, gsonCollectionChild, dto);
        Log.sysLog(String.format("%s: %d, %d", aCategory, dto.getProgramIds().size(), dto.getCollectionIds().size()));
        // alle programIds verarbeiten
        ListeFilme loadedFilme = loadPrograms(sender, langCode, dto);
        loadedFilme.forEach(film -> addFilm(film));
        Log.sysLog(String.format("%s: Subcategory %s: %d Filme", sender, aCategory, loadedFilme.size()));
      }
    }

    private void loadCollections(String sender, String langCode, Gson gsonParent, Gson gsonChild, ArteCategoryFilmsDTO dto) {
      dto.getCollectionIds().forEach(collectionId -> {
        final String url = String.format(COLLECTION_URL, langCode, collectionId);
        try {
          final ArteCategoryFilmsDTO parentDto = ArteHttpClient.executeRequest(sender, LOG, gsonParent, url, ArteCategoryFilmsDTO.class);
          if (parentDto != null) {
            parentDto.getCollectionIds().forEach(childCollectionId -> {
              final String urlChild = String.format(COLLECTION_URL, langCode, childCollectionId);
              final ArteCategoryFilmsDTO collectionDto = ArteHttpClient.executeRequest(sender, LOG, gsonChild, urlChild, ArteCategoryFilmsDTO.class);
              if (collectionDto != null) {
                collectionDto.getProgramIds().forEach(dto::addProgramId);
              }
            });
          }
        } catch (Exception e) {
          Log.errorLog(894330855, e, url);
        }
      });
    }

    private ListeFilme loadPrograms(String sender, String langCode, ArteCategoryFilmsDTO dto) {
      ListeFilme listeFilme = new ListeFilme();

      Collection<DatenFilm> futureFilme = new ArrayList<>();
      dto.getProgramIds().forEach(programId -> {
        try {
          Set<DatenFilm> films = new ArteProgramIdToDatenFilmCallable(programId, langCode, sender).call();
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

    private ArteCategoryFilmsDTO loadSubCategoryPage(Gson gson, String sender, String aUrl) {
      return ArteHttpClient.executeRequest(sender, LOG, gson, aUrl, ArteCategoryFilmsDTO.class);
    }
  }
}
