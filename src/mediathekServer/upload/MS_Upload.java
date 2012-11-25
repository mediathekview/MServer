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
package mediathekServer.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import mediathek.controller.filmeLaden.importieren.DatenUrlFilmliste;
import mediathek.tool.GuiFunktionen;
import mediathekServer.daten.MS_DatenSuchen;
import mediathekServer.daten.MS_DatenUpload;
import mediathekServer.tool.MS_Daten;
import mediathekServer.tool.MS_Konstanten;
import mediathekServer.tool.MS_Log;

public class MS_Upload {

    public static final String UPLOAD_ART_FTP = "ftp";
    public static final String UPLOAD_ART_COPY = "copy";
    private static SimpleDateFormat sdf_zeit = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat sdf_datum = new SimpleDateFormat("dd.MM.yyyy");

    public void upload(MS_DatenSuchen aktDatenSuchen) {
        String filmDateiName = aktDatenSuchen.getZielDateiName();
        String filmDateiPfad = MS_Daten.getVerzeichnisFilme();
        Iterator<MS_DatenUpload> it = MS_Daten.listeUpload.iterator();
        while (it.hasNext()) {
            MS_DatenUpload datenUpload = it.next();
            if (datenUpload.arr[MS_Konstanten.UPLOAD_ART_NR].equals(UPLOAD_ART_COPY)) {
                // ==============================================================
                // ==============================================================
                // kopieren
                if (copy(filmDateiPfad, filmDateiName, datenUpload)) {
                    melden(datenUpload.getUrlFilmliste(filmDateiName), datenUpload.getPrio());
                }
                // ==============================================================
            } else if (datenUpload.arr[MS_Konstanten.UPLOAD_ART_NR].equals(UPLOAD_ART_FTP)) {
                // ==============================================================
                // ==============================================================
                // ftp
                if (MS_UploadFtp.uploadFtp(datenUpload.arr[MS_Konstanten.UPLOAD_SERVER_NR], datenUpload.arr[MS_Konstanten.UPLOAD_PORT_NR], datenUpload.arr[MS_Konstanten.UPLOAD_USER_NR],
                        datenUpload.arr[MS_Konstanten.UPLOAD_PWD_NR], filmDateiPfad, filmDateiName,
                        datenUpload)) {
                    melden(datenUpload.getUrlFilmliste(filmDateiName), datenUpload.getPrio());
                }
                // ==============================================================
            }
        }
        MS_Log.systemMeldung("Upload Ok");
    }

    private static boolean copy(String filmDateiPfad, String filmDateiName, MS_DatenUpload datenUpload) {
        boolean ret = false;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            new File(datenUpload.getDestDir()).mkdirs();
            inChannel = new FileInputStream(GuiFunktionen.addsPfad(filmDateiPfad, filmDateiName)).getChannel();
            outChannel = new FileOutputStream(datenUpload.getFilmlisteDestPfadName(filmDateiName)).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            ret = true;
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(747452360, MS_Upload.class.getName(), "copy", ex);
        } finally {
            try {
                if (inChannel != null) {
                    inChannel.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (Exception ex) {
                MS_Log.fehlerMeldung(252160987, MS_Upload.class.getName(), "copy", ex);
            }
        }
        if (ret) {
            // Liste der Filmlisten auktualisieren
            // DatenFilmUpdateServer(String url, String prio, String zeit, String datum, String anzahl) {
            DatenUrlFilmliste dfus = new DatenUrlFilmliste(datenUpload.getUrlFilmliste(filmDateiName), "1", sdf_zeit.format(new Date()), sdf_datum.format(new Date()));
            File f = MS_ListeFilmlisten.filmlisteEintragen(datenUpload.get_Url_Datei_ListeFilmlisten(), dfus);
            if (f != null) {
                try {
                    inChannel = new FileInputStream(f).getChannel();
                    outChannel = new FileOutputStream(datenUpload.getListeFilmlistenDestPfadName()).getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    ret = true;
                } catch (Exception ex) {
                    MS_Log.fehlerMeldung(698741230, MS_Upload.class.getName(), "copy", ex);
                } finally {
                    try {
                        if (inChannel != null) {
                            inChannel.close();
                        }
                        if (outChannel != null) {
                            outChannel.close();
                        }
                    } catch (Exception ex) {
                        MS_Log.fehlerMeldung(718296540, MS_Upload.class.getName(), "copy", ex);
                    }
                }
            }
        }
        return ret;
    }

    private static void melden(String urlFilmliste, String prio) {
        try {
            String pwd = MS_Daten.system[MS_Konstanten.SYSTEM_UPDATE_MELDEN_PWD_NR].trim();
            String url = MS_Daten.system[MS_Konstanten.SYSTEM_UPDATE_MELDEN_URL_NR].trim();
            if (!pwd.equals("") && !url.equals("") && !urlFilmliste.equals("")) {
                // nur dann gibts was zum Melden
                String zeit = sdf_zeit.format(new Date());
                String datum = sdf_datum.format(new Date());
                MS_Log.systemMeldung("URL: " + urlFilmliste);
                MS_Log.systemMeldung("melden an Server: " + url);
                MS_Log.systemMeldung("Datum: " + datum + "  Zeit: " + zeit);
                // wget http://zdfmediathk.sourceforge.net/update.php?pwd=xxxxxxx&zeit=$ZEIT&datum=$DATUM&server=http://176.28.14.91/mediathek1/$2"
                String urlMelden = url
                        + "?pwd=" + MS_Daten.system[MS_Konstanten.SYSTEM_UPDATE_MELDEN_PWD_NR]
                        + "&zeit=" + zeit
                        + "&datum=" + datum
                        + (prio.trim().equals("") ? "" : "&prio=" + prio)
                        + "&server=" + urlFilmliste;
                int timeout = 10000;
                URLConnection conn = null;
                conn = new URL(urlMelden).openConnection();
                conn.setRequestProperty("User-Agent", MS_Daten.getUserAgent());
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
                InputStreamReader inReader = new InputStreamReader(conn.getInputStream(), MS_Konstanten.KODIERUNG_UTF);
                inReader.read();
                inReader.close();
                MS_Log.systemMeldung("Ok");
            }
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(301256907, MS_Upload.class.getName(), "melden", ex);
        }
    }
}
