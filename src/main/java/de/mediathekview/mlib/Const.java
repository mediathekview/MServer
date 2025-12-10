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
package de.mediathekview.mlib;

import de.mediathekview.mlib.tool.Functions;

public class Const {

    @Deprecated public static final String VERSION = "13";
    public static final String VERSION_FILMLISTE = "3";
    public static final String PROGRAMMNAME = "MSearch";
    public static final String USER_AGENT_DEFAULT = Const.PROGRAMMNAME + Functions.getProgVersionString();
    // MediathekView URLs
    public static final String ADRESSE_FILMLISTEN_SERVER_DIFF = "http://res.mediathekview.de/diff.xml";
    public static final String ADRESSE_FILMLISTEN_SERVER_AKT = "http://res.mediathekview.de/akt.xml";
    // Dateien/Verzeichnisse
    public static final int STRING_BUFFER_START_BUFFER = 8 * 1024 * 8; // 8 KiB
    public static final String FORMAT_ZIP = ".zip";
    public static final String FORMAT_XZ = ".xz";
    public static final String RTMP_PRTOKOLL = "rtmp";
    public static final String RTMP_FLVSTREAMER = "-r ";
    public static final int ALTER_FILMLISTE_SEKUNDEN_FUER_AUTOUPDATE = 3 * 60 * 60; // beim Start des Programms wir die Liste geladen wenn sie älter ist als ..
    public static final String TIME_MAX_AGE_FOR_DIFF = "09"; // Uhrzeit ab der die Diffliste alle Änderungen abdeckt, die Filmliste darf also nicht vor xx erstellt worden sein
    public static final int MAX_BESCHREIBUNG = 400; // max länge der Beschreibung in Zeichen -> mehr gibts aber jetzt nicht mehr!

    public static final String DREISAT = "3Sat";
    public static final String ARD = "ARD";
     public static final String ARD_ALPHA = "ARD-alpha";
    public static final String ARTE_DE = "ARTE.DE";
    public static final String ARTE_EN = "ARTE.EN";
    public static final String ARTE_ES = "ARTE.ES";
    public static final String ARTE_FR = "ARTE.FR";
    public static final String ARTE_IT = "ARTE.IT";
    public static final String ARTE_PL = "ARTE.PL";
    public static final String BR = "BR";
    public static final String DW = "DW";
    public static final String HR = "HR";
    public static final String KIKA = "KiKA";
    public static final String MDR = "MDR";
    public static final String NDR = "NDR";
    public static final String ORF = "ORF";
    public static final String ONE = "ONE";
    public static final String PHOENIX = "PHOENIX";
    public static final String RBB = "RBB";
    public static final String RBTV = "rbtv";
    public static final String SR = "SR";
    public static final String SRF = "SRF";
    public static final String SRF_PODCAST = "SRF.Podcast";
    public static final String SWR = "SWR";
    public static final String TAGESSCHAU24 = "tagesschau24";
    public static final String WDR = "WDR";
    public static final String ZDF = "ZDF";
    public static final String ZDF_INFO = "ZDFinfo";
    public static final String ZDF_NEO = "ZDFneo";
    public static final String ZDF_TIVI = "ZDF-tivi";

    public static final String[] SENDER = {DREISAT, ARD, ARTE_DE, ARTE_FR, BR, DW, HR, KIKA, MDR, NDR, ORF, PHOENIX, RBB, SR, SRF, SRF_PODCAST, SWR, WDR, ZDF, ZDF_TIVI};

}
