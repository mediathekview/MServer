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

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.hr.HrSendungOverviewCallable;
import mServer.crawler.sender.hr.HrSendungenDto;
import mServer.crawler.sender.hr.HrSendungenListDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MediathekHr extends MediathekReader {

    public static final String SENDERNAME = Const.HR;
    
    private static final String URL_SENDUNGEN = "http://www.hr-fernsehen.de/sendungen-a-z/index.html";
    
    private static final Logger LOG = LogManager.getLogger(MediathekHr.class);
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
        
        List<HrSendungenDto> dtos = new ArrayList<>();
        
        try {
            final Document document = Jsoup.connect(URL_SENDUNGEN).get();
            HrSendungenListDeserializer deserializer = new HrSendungenListDeserializer();

            dtos = deserializer.deserialize(document);
        } catch (IOException ex) {
            Log.errorLog(894651554, ex);
        }
        
        meldungAddMax(dtos.size());

        Collection<Future<ListeFilme>> futureFilme = new ArrayList<>();

        dtos.forEach(dto -> {

            ExecutorService executor = Executors.newCachedThreadPool();
            futureFilme.add(executor.submit(new HrSendungOverviewCallable(dto)));
            meldungProgress(dto.getUrl());
        });

        futureFilme.forEach(e -> {

            try {
                ListeFilme filmList = e.get();
                if(filmList != null) {
                    filmList.forEach(film -> {
                        if(film != null) {
                            addFilm(film);
                        }
                    });
                }
            } catch(Exception exception)
            {
                LOG.error("Es ist ein Fehler beim lesen der HR Filme aufgetreten.",exception);
            }
        });

        meldungThreadUndFertig();
    }
}
