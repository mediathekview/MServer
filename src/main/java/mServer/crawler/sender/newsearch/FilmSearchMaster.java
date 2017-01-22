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
package mServer.crawler.sender.newsearch;

import mSearch.daten.DatenFilm;
import mServer.crawler.sender.*;

import java.util.*;
import java.util.concurrent.*;

public class FilmSearchMaster
{
    private static final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();

    private Collection<Film> filmList;

    private ConcurrentMap<Sender,ForkJoinTask<VideoDTO>> tasks;




    private void addTask(Sender aSender,RecursiveTask aTask)
    {
        tasks.put(aSender,aTask);
    }

    public FilmSearchMaster() {
        tasks = new ConcurrentHashMap<Sender,ForkJoinTask<VideoDTO>>();
        addTask(Sender.OLD,new OldRunnerTask());
        addTask(Sender.ZDF,new ZDFSearchTask(15));
    }


    public Collection<DatenFilm> loadAll() {
return null;
    }

    public void loadSender(String... aSender) {

    }

}
