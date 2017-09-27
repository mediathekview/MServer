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

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import java.io.IOException;
import java.util.List;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.hr.HrSendungDeserializer;
import mServer.crawler.sender.hr.HrSendungOverviewDeserializer;
import mServer.crawler.sender.hr.HrSendungenDto;
import mServer.crawler.sender.hr.HrSendungenListDeserializer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MediathekHr extends MediathekReader {

    public final static String SENDERNAME = Const.HR;
    
    private final static String URL_SENDUNGEN = "http://www.hr-fernsehen.de/sendungen-a-z/index.html";
    
    /**
     *
     * @param ssearch
     * @param startPrio
     */
    public MediathekHr(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ 200, startPrio);
    }

    /**
     *
     */
    @Override
    public void addToList() {
        meldungStart();
        
        List<HrSendungenDto> dtos;
        
        // TODO Parallelisierung
        try {
            final Document document = Jsoup.connect(URL_SENDUNGEN).get();
            HrSendungenListDeserializer deserializer = new HrSendungenListDeserializer();
            HrSendungOverviewDeserializer overviewDeserializer = new HrSendungOverviewDeserializer();
            HrSendungDeserializer sendungDeserializer = new HrSendungDeserializer();

            dtos = deserializer.deserialize(document);
            meldungAddMax(dtos.size());
            
            dtos.forEach(dto -> {
                try {
                    if (!Config.getStop()) {
                        if (dto.getTheme().contains("hessenschau")) {
                            // TODO...

                        } else {
                            Document overviewDocument = Jsoup.connect(dto.getUrl()).get();
                            meldungProgress(dto.getUrl());
                            List<String> detailUrls = overviewDeserializer.deserialize(overviewDocument);
                            detailUrls.forEach(detailUrl -> {
                                if (!Config.getStop()) {
                                    try {
                                        Document detailDocument = Jsoup.connect(detailUrl).get();
                                        DatenFilm film = sendungDeserializer.deserialize(dto.getTheme(), detailUrl, detailDocument);

                                        if (film != null) {
                                            String subtitle = film.getUrl().replace(".mp4", ".xml");
                                            if (urlExists(subtitle)) {
                                                CrawlerTool.addUrlSubtitle(film, subtitle);
                                            }
                                            addFilm(film);
                                        }
                                    } catch (IOException ex1) {
                                        Log.errorLog(894651554, ex1);
                                    }
                                }
                            });
                        }
                    }
                } catch (IOException ex1) {
                    Log.errorLog(894651554, ex1);
                }
                
            });
        } catch (IOException ex) {
            Log.errorLog(894651554, ex);
        }
        meldungThreadUndFertig();
    }
}
