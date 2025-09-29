package de.mediathekview.mlib.daten;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Represents a found film. */
public class Film extends Podcast {
  private static final long serialVersionUID = -7834270191129532291L;
  private final Set<URL> subtitles;
  private Map<Resolution, FilmUrl> audioDescriptions;
  private Map<Resolution, FilmUrl> signLanguages;

  public Film(
      final UUID aUuid,
      final Sender aSender,
      final String aTitel,
      final String aThema,
      final LocalDateTime aTime,
      final Duration aDauer) {
    super(aUuid, aSender, aTitel, aThema, aTime, aDauer);
    audioDescriptions = new EnumMap<>(Resolution.class);
    signLanguages = new EnumMap<>(Resolution.class);
    subtitles = new HashSet<>();
  }

  public Film(final Film copyObj) {
    super(copyObj);
    audioDescriptions = copyObj.audioDescriptions;
    signLanguages = copyObj.signLanguages;
    subtitles = copyObj.subtitles;
  }

  /** DON'T USE! - ONLY FOR GSON! */
  public Film() {
    super();
    audioDescriptions = new EnumMap<>(Resolution.class);
    signLanguages = new EnumMap<>(Resolution.class);
    subtitles = new HashSet<>();
  }

  @Override
  public AbstractMediaResource<FilmUrl> merge(final AbstractMediaResource<FilmUrl> objToMergeWith) {
    addAllGeoLocations(objToMergeWith.getGeoLocations());
    addAllUrls(
        objToMergeWith.getUrls().entrySet().stream()
            .filter(
                urlEntry ->
                    !getUrls().containsKey(urlEntry.getKey())
                        || urlEntry.getValue().getFileSize()
                            > getUrls().get(urlEntry.getKey()).getFileSize())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    return this;
  }

  public void addAllSubtitleUrls(final Set<URL> urlsToAdd) {
    subtitles.addAll(urlsToAdd);
  }

  public void addAudioDescription(final Resolution aQuality, final FilmUrl aUrl) {
    if (aQuality != null && aUrl != null) {
      audioDescriptions.put(aQuality, aUrl);
    }
  }

  public void addSignLanguage(final Resolution aQuality, final FilmUrl aUrl) {
    if (aQuality != null && aUrl != null) {
      signLanguages.put(aQuality, aUrl);
    }
  }

  public void addSubtitle(final URL aSubtitleUrl) {
    if (aSubtitleUrl != null) {
      subtitles.add(aSubtitleUrl);
    }
  }

  public FilmUrl getAudioDescription(final Resolution aQuality) {
    return audioDescriptions.get(aQuality);
  }

  public Map<Resolution, FilmUrl> getAudioDescriptions() {
    if (audioDescriptions.isEmpty()) {
      return new EnumMap<>(Resolution.class);
    }
    return new EnumMap<>(audioDescriptions);
  }

  public void setAudioDescriptions(Map<Resolution, FilmUrl> audioDescriptions) {
    this.audioDescriptions = audioDescriptions;
  }

  public FilmUrl getSignLanguage(final Resolution aQuality) {
    return signLanguages.get(aQuality);
  }

  public Map<Resolution, FilmUrl> getSignLanguages() {
    if (signLanguages.isEmpty()) {
      return new EnumMap<>(Resolution.class);
    }
    return new EnumMap<>(signLanguages);
  }

  public void setSignLanguages(Map<Resolution, FilmUrl> signLanguages) {
    this.signLanguages = signLanguages;
  }

  public Collection<URL> getSubtitles() {
    return new ArrayList<>(subtitles);
  }

  @Override
  public String toString() {
    return "Film{"
        + "audioDescriptions="
        + audioDescriptions
        + ", signLanguages="
        + signLanguages
        + ", subtitles="
        + subtitles
        + "} "
        + super.toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Film)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public boolean hasUT() {
    return !subtitles.isEmpty();
  }
  
  public static void addAllToFilmlist(final Filmlist source,final Filmlist target) {
    target.addAllFilms(source.getFilms().values());
    target.addAllLivestreams(source.getLivestreams().values());
    target.addAllPodcasts(source.getPodcasts().values());
  }
  
  public static Filmlist mergeTwoFilmlists(final Filmlist aThis, final Filmlist aFilmlist) {
    final Filmlist toBeAdded = new Filmlist(UUID.randomUUID(), LocalDateTime.now());
    final Filmlist diff = new Filmlist(UUID.randomUUID(), LocalDateTime.now());
    // add all from old list not in the new list
    Map<Integer, Film> indexTTSD = buildIndexTitelThemaSenderDuration(aThis);
    aFilmlist.getFilms().entrySet().stream()
        .filter(e -> !indexTTSD.containsKey(Objects.hash(e.getValue().getSenderName(),e.getValue().getTitel(), e.getValue().getThema(), e.getValue().getDuration())))
        .forEachOrdered(e -> toBeAdded.getFilms().put(e.getKey(), e.getValue()));
    // the diff list contains all new entries (fresh list) which are not already in the old list
    Map<Integer, Film> indexaFilmlist = buildIndexTitelThemaSenderDuration(aFilmlist);
    aThis.getFilms().entrySet().stream()
    .filter(e -> !indexaFilmlist.containsKey(Objects.hash(e.getValue().getSenderName(),e.getValue().getTitel(), e.getValue().getThema(), e.getValue().getDuration())))
    .forEachOrdered(e -> diff.getFilms().put(e.getKey(), e.getValue()));
    // add the history to the current list
    aThis.getFilms().putAll(toBeAdded.getFilms());
    //
    // the same for podcast
    aFilmlist.getPodcasts().entrySet().stream().parallel()
        .filter(e -> !containsPodcast(aThis,e.getValue()))
        .forEachOrdered(e -> toBeAdded.getPodcasts().put(e.getKey(), e.getValue()));
    aThis.getPodcasts().entrySet().stream().parallel()
    .filter(e -> !containsPodcast(aFilmlist,e.getValue()))
    .forEachOrdered(e -> diff.getPodcasts().put(e.getKey(), e.getValue()));
    aThis.getPodcasts().putAll(toBeAdded.getPodcasts());
    //
    return diff;
  }
  
  private static Map<Integer, Film> buildIndexTitelThemaSenderDuration(Filmlist aList) {
    Map<Integer, Film> index = new HashMap<>(aList.getFilms().size());
    aList.getFilms().values().forEach( entry -> {
      index.put(Objects.hash(entry.getSenderName(),entry.getTitel(), entry.getThema(), entry.getDuration()), entry);
    });
    return index;
  }
  public static boolean containsFilm(Filmlist athis, Film film) {
    Optional<Film> check = athis.getFilms().entrySet().stream()
        .filter(entry -> 
            film.getTitel().equalsIgnoreCase(entry.getValue().getTitel()) &&
            film.getThema().equalsIgnoreCase(entry.getValue().getThema()) &&
            film.getSender().equals(entry.getValue().getSender()) &&
            film.getDuration().equals(entry.getValue().getDuration()))
        .map(Map.Entry::getValue)
        .findFirst();
        
    return check.isPresent();
  }
  
  public static boolean containsPodcast(Filmlist athis, Podcast prodcast) {
    Optional<Podcast> check = athis.getPodcasts().entrySet().stream()
        .filter(entry -> 
        prodcast.getTitel().equalsIgnoreCase(entry.getValue().getTitel()) &&
        prodcast.getThema().equalsIgnoreCase(entry.getValue().getThema()) &&
        prodcast.getSender().equals(entry.getValue().getSender()) &&
        prodcast.getDuration().equals(entry.getValue().getDuration()))
        .map(Map.Entry::getValue)
        .findFirst();
        
    return check.isPresent();
  }
}
