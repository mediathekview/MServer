package de.mediathekview.mserver.base.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.daten.GsonDurationAdapter;
import de.mediathekview.mserver.daten.GsonLocalDateTimeAdapter;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service zum Speichern einzelner Filme aus einer Filmlist in die DB. Nutzt
 * Batch-UPSERT und einen vorhandenen ExecutorService für Parallelität.
 */
public class FilmDBService {
  private static final Logger LOG = LogManager.getLogger(FilmDBService.class);
  private final DataSource dataSource;
  private final Gson gson;
  private final ExecutorService executorService;
  private final int batchSize;

  public FilmDBService(ExecutorService executorService, int batchSize) {
    this.dataSource = PostgreSQLDataSourceProvider.get();
    this.executorService = executorService;
    this.batchSize = batchSize;

    this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeAdapter())
        .registerTypeAdapter(Duration.class, new GsonDurationAdapter()).create();
  }
  
  
  public void update(String sql) {
    try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
      LOG.debug("updated {} rows", ps.executeUpdate());
    } catch (Exception e) {
      LOG.error(e);
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////  
  
  public void deleteFilms(Collection<Film> abandonedFilmlist) {
    try {
      List<Future<List<Film>>> futures = new ArrayList<>();
      List<Film> allVideos = abandonedFilmlist.stream()
          .sorted(Comparator.comparing(Film::getId))
          .toList();
      for (int i = 0; i < allVideos.size(); i += batchSize) {
        int from = i;
        int to = Math.min(i + batchSize, allVideos.size());
        List<Film> batch = allVideos.subList(from, to);
        futures.add(executorService.submit(() -> {
          List<Film> newVideos = new ArrayList<>();
          String sql = "DELETE FROM filme WHERE id = ?";
          try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (Film video : batch) {
              ps.setString(1, video.getId());
              ps.addBatch();
            }
            ps.executeBatch();
          } catch (SQLException e) {
            LOG.error(e);
          }
          return newVideos;
        }));
      }
      List<Film> result = new ArrayList<>();
      for (Future<List<Film>> f : futures) {
        result.addAll(f.get());
      }
      LOG.debug("deleted {}", abandonedFilmlist.size());

    } catch (Exception e) {
      LOG.error(e);
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////  

  public Optional<Filmlist> readFilmlistFromDB() {
    return readFilmlistFromDB("");
  }

  public Optional<Filmlist> readFilmlistFromDB(String where) {
    long start = System.currentTimeMillis();
    LOG.debug("import filmlist from DB");
    int readCounter = 0;
    Filmlist list = new Filmlist();
    try (Connection con = dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement("SELECT data FROM filme " + where + " ORDER BY data ->> 'sender', data ->> 'thema', data ->> 'titel'");
        ) {
       ps.setFetchSize(50000);
       try (ResultSet rs = ps.executeQuery()) {
         while (rs.next()) {
             String json = rs.getString("data");
             list.add(gson.fromJson(json, Film.class));
             readCounter++;
         }
       }
       LOG.debug("done reading in {} sec for {} elements resulting in {} elements", ((System.currentTimeMillis()-start)/1000), readCounter, list.getFilms().size());
       return Optional.of(list);
    } catch (Exception e) {
      LOG.error(e);
    }
    return Optional.empty();
  }
  
  /////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////
  
  public <T> List<T> filterNewVideos(List<T> videos, Function<T, String> idExtractor) {
    if(!PostgreSQLDataSourceProvider.isEnabled()) {
      return videos;
    }
    try {
      List<Future<List<T>>> futures = new ArrayList<>();
      // sort to avoid deadlocks
      List<T> allVideos = videos.stream()
          .sorted(Comparator.comparing(idExtractor))
          .toList();
      for (int i = 0; i < allVideos.size(); i += batchSize) {
        int from = i;
        int to = Math.min(i + batchSize, allVideos.size());
        List<T> batch = allVideos.subList(from, to);
        futures.add(executorService.submit(() -> {
          List<T> newVideos = new ArrayList<>();
          // update every 7 days
          String sql = "UPDATE filme SET last_seen = now() WHERE id = ? AND last_seen - last_update <= interval '7' DAY";

          try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

            for (T video : batch) {
              String id = idExtractor.apply(video);
              if (id != null) {
                ps.setString(1, id);
                ps.addBatch();
              } else {
                LOG.error("filterNewVideos - Missing ID for Film {}", video);
              }
            }
            int[] rs = ps.executeBatch();
            for (int rsIndex = 0; rsIndex < rs.length; rsIndex++) {
              if (rs[rsIndex] == 0) {
                newVideos.add(batch.get(rsIndex));
              }
            }

          } catch (SQLException e) {
            LOG.error(e);
          }
          return newVideos;
        }));
      }
      List<T> result = new ArrayList<>();
      for (Future<List<T>> f : futures) {
        result.addAll(f.get());
      }
      LOG.debug("Filtered {} (in {} out {})",(videos.size()-result.size()), videos.size(), result.size());
      return result;
    } catch (Exception e) {
      LOG.error("{}", e);
      return videos;
    }
  }
  
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public HashSet<String> getAllVideoUrls() {
    HashSet<String> allVideoUrls = new HashSet<String>();
    String sql = """
        SELECT
          data ->> 'sender' sender,
          data -> 'urls' -> 'SMALL' ->> 'url' aSmall,
          data -> 'urls' -> 'NORMAL' ->> 'url' aNormal,
          data -> 'urls' -> 'HD' ->> 'url' aHD
        FROM filme
          """;
    try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          allVideoUrls.add(rs.getString(1)+rs.getString(2));
          allVideoUrls.add(rs.getString(1)+rs.getString(3));
          allVideoUrls.add(rs.getString(1)+rs.getString(4));
        }
      }
    } catch (SQLException e) {
      LOG.error("getAllVideoUrls failed", e);
    }
    return allVideoUrls;
  }
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Speichert alle Filme einer Filmlist parallel in der DB.
   */
  public void saveAll(Filmlist filmlist) throws Exception {
    if(!PostgreSQLDataSourceProvider.isEnabled()) {
      return;
    }
    // Map in List konvertieren
    List<Film> films = new ArrayList<>(filmlist.getFilms().values());
    films = makeUniqueIds(films);
    AtomicInteger successCounter = new AtomicInteger(0);
    List<Future<?>> futures = new ArrayList<>();

    for (int i = 0; i < films.size(); i += batchSize) {
      int from = i;
      int to = Math.min(i + batchSize, films.size());
      List<Film> batch = films.subList(from, to);

      futures.add(executorService.submit(() -> {
        try {
          successCounter.addAndGet(saveBatch(batch));
        } catch (SQLException | IOException e) {
          LOG.error(e);
        }
      }));
    }

    for (Future<?> f : futures) {
      f.get();
    }
    
    LOG.info("Stored {} films in DB", successCounter.get());
  }

  /**
   * Speichert einen Batch von Filmen als Upsert in der DB.
   */
  private int saveBatch(List<Film> films) throws SQLException, IOException {
    int successCounter = 0;

    String sql = """
            INSERT INTO filme (id, data)
            VALUES (?, ?::jsonb)
            ON CONFLICT (id) DO UPDATE
            SET data = EXCLUDED.data,
                last_update = now()
        """;

    try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

      for (Film film : films) {
        if(film.getId() != null) {
          ps.setString(1, film.getId());
          ps.setString(2, gson.toJson(film));
          ps.addBatch();
          successCounter++;
        } else {
          LOG.error("saveBatch - Missing ID for film {}", film);
        }
      }

      ps.executeBatch();
    }
    return successCounter;
  }

  private static List<Film> makeUniqueIds(List<Film> films) {
    Map<String, AtomicInteger> idCount = new HashMap<>();

    return films.stream().map(film -> {
      String originalId = film.getId();
      AtomicInteger count = idCount.computeIfAbsent(originalId, k -> new AtomicInteger(0));

      int c = count.getAndIncrement();
      if (c == 0) {
        return film; // erste ID bleibt unverändert
      } else {
        // Duplikat → neue ID mit Suffix #1, #2 ...
        film.setId(originalId + "#" + c);
        return film;
      }
    }).collect(Collectors.toList());
  }
}
