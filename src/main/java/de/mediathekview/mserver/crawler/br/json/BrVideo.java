package de.mediathekview.mserver.crawler.br.json;

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;

public record BrVideo(Resolution resolution, FilmUrl url) {
}
