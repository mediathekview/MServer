package de.mediathekview.mserver.daten;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A class that holds a thread safe map of {@link Film} with there UUIDs and some additional
 * information.
 */
public class Filmlist {
  private final ConcurrentHashMap<UUID, Film> films;
  private final ConcurrentHashMap<UUID, Podcast> podcasts;
  private final ConcurrentHashMap<UUID, Livestream> livestreams;
  private LocalDateTime creationDate;
  private UUID listId;

  public Filmlist() {
    this(UUID.randomUUID(), LocalDateTime.now());
  }

  public Filmlist(final UUID aListId, final LocalDateTime aCreationDate) {
    super();
    films = new ConcurrentHashMap<>();
    podcasts = new ConcurrentHashMap<>();
    livestreams = new ConcurrentHashMap<>();
    listId = aListId;
    creationDate = aCreationDate;
  }

  public void add(final Film aFilm) {
    films.put(aFilm.getUuid(), aFilm);
  }

  public void add(final Livestream aLivestream) {
    livestreams.put(aLivestream.getUuid(), aLivestream);
  }

  public void add(final Podcast aPodcast) {
    podcasts.put(aPodcast.getUuid(), aPodcast);
  }

  public void addAllFilms(final Collection<Film> aFilms) {
    aFilms.forEach(this::add);
  }

  public void addAllLivestreams(final Collection<Livestream> aLivestreams) {
    aLivestreams.forEach(this::add);
  }

  public void addAllPodcasts(final Collection<Podcast> aPodcasts) {
    aPodcasts.forEach(this::add);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof final Filmlist filmlist)) {
      return false;
    }
    return Objects.equals(getFilms(), filmlist.getFilms())
        && Objects.equals(getPodcasts(), filmlist.getPodcasts())
        && Objects.equals(getLivestreams(), filmlist.getLivestreams())
        && Objects.equals(getCreationDate(), filmlist.getCreationDate())
        && Objects.equals(getListId(), filmlist.getListId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getFilms(), getPodcasts(), getLivestreams(), getCreationDate(), getListId());
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public ConcurrentMap<UUID, Film> getFilms() {
    return films;
  }

  public List<Film> getFilmsSorted(final Comparator<AbstractMediaResource<?>> aComparator) {
    final List<Film> sortedFilms = new ArrayList<>(films.values());
    sortedFilms.sort(aComparator);
    return sortedFilms;
  }

  public UUID getListId() {
    return listId;
  }

  public ConcurrentMap<UUID, Livestream> getLivestreams() {
    return livestreams;
  }

  public List<Livestream> getLivestreamsSorted(
      final Comparator<AbstractMediaResource<?>> aComperator) {
    final List<Livestream> sortedLivestreams = new ArrayList<>(livestreams.values());
    sortedLivestreams.sort(aComperator);
    return sortedLivestreams;
  }

  public ConcurrentMap<UUID, Podcast> getPodcasts() {
    return podcasts;
  }

  public List<Podcast> getPodcastsSorted(final Comparator<AbstractMediaResource<?>> aComperator) {
    final List<Podcast> sortedPodcasts = new ArrayList<>(podcasts.values());
    sortedPodcasts.sort(aComperator);
    return sortedPodcasts;
  }

  public List<AbstractMediaResource<?>> getSorted(final Comparator<AbstractMediaResource<?>> aComperator) {
    final List<AbstractMediaResource<?>> sortedResources = new ArrayList<>(films.values());
    sortedResources.addAll(podcasts.values());
    sortedResources.addAll(livestreams.values());
    sortedResources.sort(aComperator);
    return sortedResources;
  }

  /**
   * Merges this film list with the given film list and returns the difference list.
   *
   * @param aFilmlist The film list to merge into this.
   * @return The difference list.
   */
  public Filmlist merge(final Filmlist aFilmlist) {
    final Filmlist differenceList = new Filmlist(UUID.randomUUID(), creationDate);

    aFilmlist.films.entrySet().stream()
        .filter(e -> !films.containsKey(e.getKey()))
        .forEachOrdered(e -> differenceList.films.put(e.getKey(), e.getValue()));

    aFilmlist.podcasts.entrySet().stream()
        .filter(e -> !podcasts.containsKey(e.getKey()))
        .forEachOrdered(e -> differenceList.podcasts.put(e.getKey(), e.getValue()));

    aFilmlist.livestreams.entrySet().stream()
        .filter(e -> !livestreams.containsKey(e.getKey()))
        .forEachOrdered(e -> differenceList.livestreams.put(e.getKey(), e.getValue()));

    films.putAll(differenceList.films);
    podcasts.putAll(differenceList.podcasts);
    livestreams.putAll(differenceList.livestreams);

    return differenceList;
  }

  public void setCreationDate(final LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public void setListId(final UUID listId) {
    this.listId = listId;
  }
}
