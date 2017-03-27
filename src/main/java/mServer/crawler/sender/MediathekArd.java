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
package mServer.crawler.sender;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MSStringBuilder;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.GetUrl;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.tool.M3U8Utils;


public class MediathekArd extends MediathekReader {
    private static final Logger LOG = LogManager.getLogger(MediathekArd.class);
    private final static String SENDERNAME = Const.ARD;
    private final static String THEMA_TAGE = "TAGE";
    private static final String ADRESSE_THEMA = "http://www.ardmediathek.de/tv";
    private static final String MUSTER_URL_THEMA = "<a href=\"/tv/sendungen-a-z?buchstabe=";
    private static final String MUSTER_FEED_SUCHEN = "<div class=\"media mediaA\">";
    private static final String M3U8_PATTERN_START = "_quality\":\"auto\",\"_server\":\"\",\"_cdn\":\"flashls\",\"_stream\":\"";
	private static final String M3U8_PATTERN_END = "\"";
	private static final String TEXT_START_HTTP = "http";
	private static final String URL_GET_PARAMETER = "\\?.*";
    private MSStringBuilder seiteFeed = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
    
    public MediathekArd(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, 8, 50, startPrio);
    }

    @Override
    void addToList() {
        listeThemen.clear();
        addThema();
        listeThemen.addUrl(new String[]{THEMA_TAGE, ""});
        if (Config.getStop() || listeThemen.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            listeSort(listeThemen, 1);
            for (int t = 0; t < getMaxThreadLaufen(); ++t) {
                Thread th = new ThemaLaden();
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }


    private void addThema() {
        listeThemen.clear();
        MSStringBuilder seite = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        meldungStart();
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seite = getUrlIo.getUri(SENDERNAME, ADRESSE_THEMA, StandardCharsets.UTF_8, 5 /* versuche */, seite, "" /* Meldung */);
        if (seite.length() == 0) {
            Log.sysLog("ARD: Versuch 2");
            warten(2 * 60 /*Sekunden*/);
            seite = getUrlIo.getUri(SENDERNAME, ADRESSE_THEMA, StandardCharsets.UTF_8, 5 /* versuche */, seite, "" /* Meldung */);
            if (seite.length() == 0) {
                Log.errorLog(104689736, "wieder nichts gefunden");
            }
        }
        int pos = 0;
        String url = "";
        while (!Config.getStop() && (pos = seite.indexOf(MUSTER_URL_THEMA, pos)) != -1) {
            try {
                pos += MUSTER_URL_THEMA.length();
                int pos1 = pos;
                int pos2 = seite.indexOf("\"", pos);
                if (pos1 != -1 && pos2 != -1) {
                    url = seite.substring(pos1, pos2);
                }
                if (!url.isEmpty()) {
                    url = "http://www.ardmediathek.de/tv/sendungen-a-z?buchstabe=" + url;
                    feedSuchen1(url);
                }
            } catch (Exception ex) {
                Log.errorLog(698732167, ex, "kein Thema");
            }
        }
    }


    private void feedSuchen1(String strUrlFeed) {
        GetUrl getUrlIo = new GetUrl(getWartenSeiteLaden());
        seiteFeed = getUrlIo.getUri(SENDERNAME, strUrlFeed, StandardCharsets.UTF_8, 2/*max Versuche*/, seiteFeed, "");
        if (seiteFeed.length() == 0) {
            Log.errorLog(207956317, "Leere Seite: " + strUrlFeed);
            return;
        }
        int pos;
        String url, thema;
        pos = seiteFeed.indexOf(MUSTER_FEED_SUCHEN);
        pos += MUSTER_FEED_SUCHEN.length();
        while (!Config.getStop() && (pos = seiteFeed.indexOf(MUSTER_FEED_SUCHEN, pos)) != -1) {
            try {
                pos += MUSTER_FEED_SUCHEN.length();
                url = seiteFeed.extract("<a href=\"/tv/", "\"", pos);
                if (!url.isEmpty()) {
                    url = "http://www.ardmediathek.de/tv/" + url;
                    thema = seiteFeed.extract("<h4 class=\"headline\">", "<", pos);
                    if (thema.isEmpty()) {
                        thema = seiteFeed.extract("title=\"", "\"", pos);
                        Log.errorLog(132326564, "Thema: " + strUrlFeed);
                    }
                    String[] add = new String[]{url, thema};
                    listeThemen.addUrl(add);
                }
            } catch (Exception ex) {
                Log.errorLog(732154698, ex, "Weitere Seiten suchen");
            }
        }
    }

    private void warten(long i) {
        // Sekunden warten
        try {
            // war wohl nix, warten und dann nochmal
            // timeout: the maximum time to wait in milliseconds.
            Thread.sleep(i * 1000);
        } catch (Exception ex) {
            Log.errorLog(369502367, ex, "2. Versuch");
        }
    }

    private class ThemaLaden extends Thread {
        private static final String THEMA_ALPHA_CENTAURI = "alpha-Centauri";
        private static final String MUSTER_ADD_TAGE = "<span class=\"date\">";
        private static final String MUSTER_FILM_SUCHEN1 = "<div class=\"mediaCon\">";
        private static final String MUSTER_START_FILM_SUCHEN1 = "Beiträge der Sendung";
        private final ArrayList<String> liste = new ArrayList<>();
        private MSStringBuilder seite1 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite3 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!Config.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    if (link[0].equals(THEMA_TAGE)) {
                        addTage();
                    } else {
                        filmSuchen1(link[0] /* url */, link[1], true);
                    }
                }
            } catch (Exception ex) {
                Log.errorLog(487326921, ex);
            }
            meldungThreadUndFertig();
        }

        private void addTage() {
            // http://www.ardmediathek.de/tv/sendungVerpasst?tag=0 ... 6
            for (int i = 0; i <= 6; ++i) {
                if (Config.getStop()) {
                    break;
                }
                String urlTage = "http://www.ardmediathek.de/tv/sendungVerpasst?tag=" + i;
                final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                seite1 = getUrl.getUri(SENDERNAME, urlTage, StandardCharsets.UTF_8, 2, seite1, "");
                if (seite1.length() == 0) {
                    Log.errorLog(765323214, "Leere Seite: " + urlTage);
                    return;
                }
                int pos = 0;
                String url, datum, zeit = "", titel, dauer, urlSendung, thema;
                long d = 0;
                while (!Config.getStop() && (pos = seite1.indexOf(MUSTER_ADD_TAGE, pos)) != -1) {
                    zeit = seite1.extract("<span class=\"date\">", "<", pos) + ":00";
                    pos += MUSTER_ADD_TAGE.length();

                    url = seite1.extract("documentId=", "&", pos);
                    if (url.contains("\"")) {
                        url = url.substring(0, url.indexOf('\"'));
                    }
                    if (!url.isEmpty()) {
                        url = url.replace("&amp;", "&");
                        thema = seite1.extract("<span class=\"titel\">", "<", pos);
                        if (thema.endsWith("Uhr") && thema.contains(",")) {
                            // tagesschau, 09:00 Uhr
                            thema = thema.substring(0, thema.indexOf(','));
                        }
                        datum = seite1.extract("<title>Videos (TV-Sendungen) des Senders Das Erste vom", "- ARD").trim();
                        titel = seite1.extract("<h4 class=\"headline\">", "<", pos);
                        dauer = seite1.extract("<p class=\"subtitle\">", "<", pos);
                        try {
                            dauer = dauer.replace("Min.", "").trim();
                            dauer = dauer.replace("| UT", "").trim();
                            d = Long.parseLong(dauer) * 60;
                        } catch (Exception ignored) {
                        }
                        if (d == 0) {
                            Log.errorLog(915263621, "Dauer==0: " + urlTage);
                        }
                        urlSendung = seite1.extract("<a href=\"/tv/", "\"", pos);
                        if (!urlSendung.isEmpty()) {
                            urlSendung = "http://www.ardmediathek.de/tv/" + urlSendung;
                            urlSendung = urlSendung.replace("&amp;", "&");
                        }

                        filmSuchen2(url, thema, titel, d, datum, zeit, urlSendung);
                    }
                }
            }

        }

        private void filmSuchen1(String strUrlFeed, String thema, boolean weiter) {
            final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
            seite1 = getUrl.getUri(SENDERNAME, strUrlFeed, StandardCharsets.UTF_8, 2, seite1, "");
            if (seite1.length() == 0) {
                Log.errorLog(765323214, "Leere Seite: " + strUrlFeed);
                return;
            }
            int pos;
            String url, datum, zeit = "", titel, dauer, urlSendung;
            long d = 0;
            int count = 0;
            if ((pos = seite1.indexOf(MUSTER_START_FILM_SUCHEN1)) != -1) {
                pos += MUSTER_START_FILM_SUCHEN1.length();
            } else {
                return;
            }
             while (!Config.getStop() && (pos = seite1.indexOf(MUSTER_FILM_SUCHEN1, pos)) != -1)
            {
                ++count;
                if (!CrawlerTool.loadLongMax() && count > 5 && !thema.equalsIgnoreCase(THEMA_ALPHA_CENTAURI))
                {
                        break;
                }
                pos += MUSTER_FILM_SUCHEN1.length();
                url = seite1.extract("documentId=", "&", pos);
                if (url.contains("\""))
                {
                    url = url.substring(0, url.indexOf('"'));
                }
                if (url.equals(""))
                {
                    continue;
                }
                url = url.replace("&amp;", "&");
                datum = seite1.extract("<p class=\"dachzeile\">", "<", pos);
                datum = datum.replace("Uhr", "").trim();
                if (datum.contains("|"))
                {
                    zeit = datum.substring(datum.indexOf("|") + 1).trim();
                    zeit = zeit + ":00";
                    datum = datum.substring(0, datum.indexOf("|")).trim();
                }
                titel = seite1.extract("<h4 class=\"headline\">", "<", pos);
                dauer = seite1.extract("<p class=\"subtitle\">", "<", pos);
                try
                {
                    dauer = dauer.replace("Min.", "").trim();
                    dauer = dauer.replace("| UT", "").trim();
                    d = Long.parseLong(dauer) * 60;
                } catch (Exception ex)
                {
                    LOG.debug("Die dauer konnte nicht als long geparsed werden.",ex);
                }
                if (d == 0)
                {
                    Log.errorLog(915263621, "Dauer==0: " + strUrlFeed);
                }
                urlSendung = seite1.extract("<a href=\"/tv/", "\"", pos);
                if (!urlSendung.isEmpty())
                {
                    urlSendung = "http://www.ardmediathek.de/tv/" + urlSendung;
                    urlSendung = urlSendung.replace("&amp;", "&");
                }

                filmSuchen2(url, thema, titel, d, datum, zeit, urlSendung);
            }
            if (!Config.getStop() && weiter
                    && (CrawlerTool.loadLongMax() || thema.equalsIgnoreCase("alpha-Centauri")))
            {
                // dann gehts weiter
                int maxWeiter = 0;
                int maxTh = 10;
                String urlWeiter = strUrlFeed + "&mcontents=page.";
                for (int i = 2; i < maxTh; ++i)
                {
                    ///tv/Abendschau/Sendung?documentId=14913430&amp;bcastId=14913430&amp;mcontents=page.2"
                    if (seite1.indexOf("&amp;mcontents=page." + i) != -1)
                    {
                        maxWeiter = i;
                    } else
                    {
                        break;
                    }
                }
                for (int i = 2; i < maxTh; ++i)
                {
                    if (Config.getStop())
                    {
                        break;
                    }
                    if (i <= maxWeiter)
                    {
                        filmSuchen1(urlWeiter + i, thema, false);
                    } else
                    {
                        break;
                    }

                }
            }
}

        private void filmSuchen2(String urlFilm_, String thema, String titel, long dauer, String datum, String zeit, String urlSendung) {
            // URL bauen: http://www.ardmediathek.de/play/media/21528242?devicetype=pc&features=flash
            try {
                String urlFilm = "http://www.ardmediathek.de/play/media/" + urlFilm_ + "?devicetype=pc&features=flash";
                meldung(urlFilm);
                final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                seite2 = getUrl.getUri(SENDERNAME, urlFilm, StandardCharsets.UTF_8, 2, seite2, "");
                if (seite2.length() == 0) {
                    Log.errorLog(915263621, "Leere Seite: " + urlFilm);
                    return;
                }
                String url, urlMid = "", urlKl = "", urlHD = "";
                String urlTest = "";
                liste.clear();

                url = getUrl(seite2); // neuer Weg
                seite2.extractList("{\"_quality\":3,\"_server\":\"\",\"_cdn\":\"default\",\"_stream\":\"", "\"", liste);
                seite2.extractList("_quality\":3,\"_stream\":[\"", "\"", liste);
                seite2.extractList("\"_quality\":3,\"_server\":\"\",\"_cdn\":\"akamai\",\"_stream\":\"", "\"", liste);
                if (seite2.indexOf("quality\":3") >= 0) {
                    if (liste.size() <= 0) {
                        // Fehler
                    }
                }
                for (String s : liste) {
                    if (s.startsWith("http")) {
                        urlHD = s;
                        break;
                    }
                }
                liste.clear();

				seite2.extractList("\"_quality\":2", "\"_stream\":\"", "\"", liste);
				seite2.extractList("\"_quality\":2", "\"_stream\":[\"", "\"", liste);
				for (String s : liste) {
					if (s.startsWith("http")) {
						if (url.isEmpty()) {
							url = s;
						} else {
							urlMid = s;
						}
						break;
					}
				}
				liste.clear();
				seite2.extractList("\"_quality\":1", "\"_stream\":\"", "\"", liste);
				seite2.extractList("\"_quality\":1", "\"_stream\":[\"", "\"", liste);
				for (String s : liste) {
					if (s.startsWith("http")) {
						urlKl = s;
						break;
					}
				}

				// No Url found? Try M3U8 / HLS:
				if (isAllTextsEmpty(url, urlHD, urlMid, urlKl)) {
					Map<Qualities, String> urls = searchForUrlsWithM3U8(seite2);
					if (urls.containsKey(Qualities.SMALL)) {
						urlKl = urls.get(Qualities.SMALL);
					}
					if (urls.containsKey(Qualities.NORMAL)) {
						urlMid = urls.get(Qualities.NORMAL);
					}
					if (urls.containsKey(Qualities.HD)) {
						urlHD = urls.get(Qualities.HD);
					}
				}

				if (!urlTest.isEmpty() && (urlTest.equals(url) || urlTest.equals(urlKl))) {
					urlHD = ""; // dann ists kein HD
				}
				if (url.isEmpty()) {
					url = urlMid;
					urlMid = "";
				}
				if (url.isEmpty()) {
					url = urlKl;
					urlKl = "";
				}
				if (url.isEmpty() && !urlHD.isEmpty()) {
					url = urlHD;
					urlHD = "";
				}
				if (urlKl.isEmpty()) {
					urlKl = urlMid;
				}
				String subtitle = seite2.extract("subtitleUrl\":\"", "\"");
				if (!subtitle.isEmpty()) {
					if (!subtitle.startsWith("http://www.ardmediathek.de")) {
						subtitle = "http://www.ardmediathek.de" + subtitle;
					}
				}
				if (!url.isEmpty()) {

					// http://http-stream.rbb-online.de/rbb/rbbreporter/rbbreporter_20151125_solange_ich_tanze_lebe_ich_WEB_L_16_9_960x544.mp4?url=5
					if (url.contains("?url=")) {
						url = url.substring(0, url.indexOf("?url="));
					}
					if (urlKl.contains("?url=")) {
						urlKl = url.substring(0, urlKl.indexOf("?url="));
					}
					if (urlHD.contains("?url=")) {
						urlHD = url.substring(0, urlHD.indexOf("?url="));
					}

					String beschreibung = beschreibung(urlSendung);
					DatenFilm f = new DatenFilm(SENDERNAME, thema, urlSendung, titel, url, ""/* urlRtmp */, datum, zeit,
							dauer, beschreibung);
					if (!urlKl.isEmpty()) {
						CrawlerTool.addUrlKlein(f, urlKl, "");
					}
					if (!urlHD.isEmpty() && !urlHD.equals(url)) {
						CrawlerTool.addUrlHd(f, urlHD, "");
					}
					if (!subtitle.isEmpty()) {
						CrawlerTool.addUrlSubtitle(f, subtitle);
					}
					addFilm(f);
				} else {
					filmSuchen_old(urlSendung, thema, titel, dauer, datum, zeit);
					// MSLog.fehlerMeldung(784512369, "keine URL: " + urlFilm);
				}
			} catch (Exception ex) {
				Log.errorLog(762139874, ex);
			}
		}

        private String getUrl(MSStringBuilder seite) {
            String ret = "";
            seite.extractList("\"_quality\":2,\"_stream\":[", "]", liste);
			for (String s : liste) {
				// "http://mvideos.daserste.de/videoportal/Film/c_550000/557945/format653790.mp4","http://mvideos.daserste.de/videoportal/Film/c_550000/557945/format653793.mp4"
				s = s.replace("\"", "");
				if (s.contains(",")) {
					String[] ar = s.split(",");
					for (String ss : ar) {
						if (ss.startsWith("http")) {
							ret = ss;
						}
					}
				}
				if (!ret.isEmpty()) {
					break;
				}
			}
			// if (!ret.isEmpty()) {
			// System.out.println("gefunden!!");
			// }
			liste.clear();
			return ret;
		}
            
        private void filmSuchen_old(String urlSendung, String thema, String titel, long dauer, String datum, String zeit) {
            try {
                meldung(urlSendung);
                final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
                seite2 = getUrl.getUri(SENDERNAME, urlSendung, StandardCharsets.UTF_8, 1, seite2, "");
                if (seite2.length() == 0) {
                    Log.errorLog(612031478, "Leere Seite: " + urlSendung);
                    return;
                }
                String url = seite2.extract("</li><li data-ctrl-", "http://", ".m3u8");
                if (!url.isEmpty()) {
                    url = "http://" + url + ".m3u8";
//                    System.out.println(url);
                }
                if (!url.isEmpty()) {
                    String beschreibung = beschreibung(urlSendung);
                    DatenFilm f = new DatenFilm(SENDERNAME, thema, urlSendung, titel, url, ""/*urlRtmp*/, datum, zeit, dauer, beschreibung);
                    addFilm(f);
                } else {
                    Log.errorLog(974125698, "keine URL: " + urlSendung);
                }
            } catch (Exception ex) {
                Log.errorLog(102054784, ex);
            }
        }


        private String beschreibung(String strUrlFeed) {
            final GetUrl getUrl = new GetUrl(getWartenSeiteLaden());
            seite3 = getUrl.getUri(SENDERNAME, strUrlFeed, StandardCharsets.UTF_8, 1, seite3, "");
            if (seite3.length() == 0) {
                Log.errorLog(784512036, "Leere Seite: " + strUrlFeed);
                return "";
            } else
                return seite3.extract("<p class=\"subtitle\">", "<p class=\"teasertext\" itemprop=\"description\">", "<");
        }
        
        private boolean isAllTextsEmpty(String... aTexts) {
		Boolean isAllTextsEmpty = null;
		for (String text : aTexts) {
			isAllTextsEmpty = (isAllTextsEmpty == null || isAllTextsEmpty) && StringUtils.isEmpty(text);
		}
		return isAllTextsEmpty == null || isAllTextsEmpty;
	}
}

	

	/**
	 * Searches the Seite for a quality auto to get a M3U8 URL. If the URL is
	 * from WRD it searches for the URLs of the MP4 files.
	 *
	 * @param aSeiteStringExtractor
	 *            The Seite.
	 * @return A Map containing the URLs and Qualities which was found. An empty
	 *         Map if nothing was found.
	 */
	private Map<Qualities, String> searchForUrlsWithM3U8(final MSStringBuilder aSeiteStringExtractor) {
		Map<Qualities, String> urls = new EnumMap<>(Qualities.class);

		ArrayList<String> patternMatches = new ArrayList<>();
		aSeiteStringExtractor.extractList(M3U8_PATTERN_START, M3U8_PATTERN_END, patternMatches);

		String m3u8Url = null;
		for (String patternMatch : patternMatches) {
			if (patternMatch.startsWith(TEXT_START_HTTP)) {
				m3u8Url = patternMatch;
				break;
			}
		}

		if (m3u8Url != null) {
			m3u8Url = m3u8Url.replaceAll(URL_GET_PARAMETER, "");
			if (m3u8Url.contains(M3U8Utils.M3U8_WDR_URL_BEGIN)) {
				urls.putAll(M3U8Utils.gatherUrlsFromWdrM3U8(m3u8Url));
			} else {
				urls.put(Qualities.NORMAL, m3u8Url);
			}
		}

		return urls;
	}


}
