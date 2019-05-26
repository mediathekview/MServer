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
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.newsearch.ZDFEntryDTO;

import java.util.function.Predicate;

public class MediathekZdf extends AbstractMediathekZdf {
  private static final String SENDERNAME = Const.ZDF;

  public MediathekZdf(FilmeSuchen ssearch, int startPrio) {
    super(SENDERNAME, ssearch, startPrio);
  }

  @Override
  protected String getApiHost() {
    return "api.zdf.de";
  }

  @Override
  protected String getApiBaseUrl() {
    return "https://api.zdf.de";
  }

  @Override
  protected String getBaseUrl() {
    return "https://www.zdf.de";
  }

  @Override
  protected Predicate<? super ZDFEntryDTO> createEntryFilter() {
    return zdfEntryDTO -> !zdfEntryDTO.getTvService().equals("3sat");
  }
}
