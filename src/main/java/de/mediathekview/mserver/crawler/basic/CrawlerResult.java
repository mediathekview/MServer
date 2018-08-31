package de.mediathekview.mserver.crawler.basic;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Livestream;
import de.mediathekview.mlib.daten.Podcast;

public class CrawlerResult implements Serializable {
	private static final long serialVersionUID = 5587455362918804065L;
	private Set<Film> films;
	private Set<Livestream> livestreams;
	private Set<Podcast> podcasts;

	public CrawlerResult() {
		films = ConcurrentHashMap.newKeySet();
		livestreams = ConcurrentHashMap.newKeySet();
		podcasts = ConcurrentHashMap.newKeySet();
	}

	public boolean addAllFilm(Collection<Film> films) {
		return films.addAll(films);
	}

	public boolean addAllLivestream(Collection<Livestream> livestreams) {
		return livestreams.addAll(livestreams);
	}

	public boolean addAllPodcast(Collection<Podcast> podcasts) {
		return podcasts.addAll(podcasts);
	}

	public boolean addFilm(Film film) {
		return films.add(film);
	}

	public boolean addLivestream(Livestream livestream) {
		return livestreams.add(livestream);
	}

	public boolean addPodcast(Podcast podcast) {
		return podcasts.add(podcast);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CrawlerResult)) {
			return false;
		}
		CrawlerResult other = (CrawlerResult) obj;
		if (films == null) {
			if (other.films != null) {
				return false;
			}
		} else if (!films.equals(other.films)) {
			return false;
		}
		if (livestreams == null) {
			if (other.livestreams != null) {
				return false;
			}
		} else if (!livestreams.equals(other.livestreams)) {
			return false;
		}
		if (podcasts == null) {
			if (other.podcasts != null) {
				return false;
			}
		} else if (!podcasts.equals(other.podcasts)) {
			return false;
		}
		return true;
	}

	public Set<Film> getFilms() {
		return new HashSet<>(films);
	}

	public Set<Livestream> getLivestreams() {
		return new HashSet<>(livestreams);
	}

	public Set<Podcast> getPodcasts() {
		return new HashSet<>(podcasts);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((films == null) ? 0 : films.hashCode());
		result = prime * result + ((livestreams == null) ? 0 : livestreams.hashCode());
		result = prime * result + ((podcasts == null) ? 0 : podcasts.hashCode());
		return result;
	}

	public void merge(CrawlerResult other) {
		films.addAll(other.getFilms());
		livestreams.addAll(other.getLivestreams());
		podcasts.addAll(other.getPodcasts());

	}

}
