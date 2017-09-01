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
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.Log;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmPoint;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.zdf.DownloadDTO;
import mServer.crawler.sender.zdf.VideoDTO;
import mServer.crawler.sender.zdf.ZDFSearchTask;
import mServer.tool.MserverDatumZeit;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.*;

public class MediathekZdf extends MediathekReader
{
    private static final Logger LOG = LogManager.getLogger(MediathekZdf.class);
    public final static Sender SENDER = Sender.ZDF;
    private ForkJoinPool forkJoinPool;

    public MediathekZdf(FilmeSuchen ssearch, int startPrio)
    {
        super(ssearch, SENDER.getName(), 0 /* threads */, 150 /* urlWarten */, startPrio);
        setName("MediathekZdf");
    }

    private final Phaser phaser = new Phaser();

    @Override
    public void addToList()
    {
        meldungStart();
        meldungAddThread();

        int days = CrawlerTool.loadLongMax() ? 300 : 20;

        final ZDFSearchTask newTask = new ZDFSearchTask(days);
        forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 4);
        forkJoinPool.execute(newTask);
        Collection<VideoDTO> filmList = newTask.join();

        EtmPoint perfPoint = EtmManager.getEtmMonitor().createPoint("MediathekZdf.convertVideoDTO");

        if (!filmList.isEmpty())
        {
            // Convert new DTO to old DatenFilm class
            Log.sysLog("convert VideoDTO to DatenFilm started...");
            filmList.parallelStream().forEach((video) ->
            {
                VideoDtoDatenFilmConverterAction action = new VideoDtoDatenFilmConverterAction(video);
                forkJoinPool.execute(action);
            });

            filmList.clear();
        }

        boolean wasInterrupted = false;
        while (!phaser.isTerminated())
        {
            try
            {
                if (Config.getStop())
                {
                    wasInterrupted = true;
                    phaser.forceTermination();
                    shutdownAndAwaitTermination(forkJoinPool, 5, TimeUnit.SECONDS);
                } else
                {
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException ignored)
            {
            }
        }

        //explicitely shutdown the pool
        shutdownAndAwaitTermination(forkJoinPool, 60, TimeUnit.SECONDS);

        perfPoint.collect();
        if (wasInterrupted)
        {
            Log.sysLog("VideoDTO conversion interrupted.");
        } else
        {
            Log.sysLog("convert VideoDTO to DatenFilm finished.");
        }

        meldungThreadUndFertig();
    }

    void shutdownAndAwaitTermination(ExecutorService pool, long delay, TimeUnit delayUnit)
    {
        pool.shutdown();
        try
        {
            if (!pool.awaitTermination(delay, delayUnit))
            {
                pool.shutdownNow();
                if (!pool.awaitTermination(delay, delayUnit))
                {
                    Log.sysLog("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie)
        {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("serial")
    private class VideoDtoDatenFilmConverterAction extends RecursiveAction
    {

        private final VideoDTO video;

        public VideoDtoDatenFilmConverterAction(VideoDTO aVideoDTO)
        {
            video = aVideoDTO;
            phaser.register();
        }

        @Override
        protected void compute()
        {
            if (video != null)
            {
                try
                {
                    DownloadDTO download = video.getDownloadDto();

                    Collection<GeoLocations> geoLocations = new ArrayList<>();
                    geoLocations.add(download.getGeoLocation());

                    Film film = new Film(UUID.randomUUID(),
                            geoLocations,
                            SENDER,
                            video.getTitle(),
                            video.getTopic(),
                            MserverDatumZeit.parseDateTime(video.getDate(),
                                    video.getTime()),
                            Duration.of(video.getDuration(), ChronoUnit.SECONDS),
                            new URI(video.getWebsiteUrl()));

                    if (StringUtils.isNotBlank(video.getDescription()))
                    {
                        film.setBeschreibung(video.getDescription());
                    }
                    if (download.hasUrl(Qualities.HD))
                    {
                        film.addUrl(Qualities.HD, CrawlerTool.stringToFilmUrl(download.getUrl(Qualities.HD)));
                    }
                    if (download.hasUrl(Qualities.SMALL))
                    {
                        film.addUrl(Qualities.SMALL, CrawlerTool.stringToFilmUrl(download.getUrl(Qualities.SMALL)));
                    }

                    if (StringUtils.isNotBlank(download.getSubTitleUrl()))
                    {
                        film.addSubtitle(new URI(download.getSubTitleUrl()));
                    }

                    try
                    {
                        CrawlerTool.improveAufloesung(film);
                    } catch (URISyntaxException uriSyntaxEception)
                    {
                        LOG.error("Beim verbessern der Auflösung ist ein Fehler aufgetreten", uriSyntaxEception);
                    }

                    //don´t use addFilm here
                    if (mlibFilmeSuchen.listeFilmeNeu.addFilmVomSender(film))
                    {
                        // dann ist er neu
                        FilmeSuchen.listeSenderLaufen.inc(film.getSender().getName(), RunSender.Count.FILME);
                    }

                } catch (Exception ex)
                {
                    Log.errorLog(496583211, ex, "add film failed: " + video.getWebsiteUrl());
                }
            }
            phaser.arriveAndDeregister();
        }
    }


}
