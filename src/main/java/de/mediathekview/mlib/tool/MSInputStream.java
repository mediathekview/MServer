/*    
 *    MediathekView
 *    Copyright (C) 2013   W. Xaver
 *    W.Xaver[at]googlemail.com
 *    http://zdfmediathk.sourceforge.net/
 * 
 *    org.apache.hadoop.tools.util.ThrottledInputStream
 *    unter http://www.apache.org/licenses/LICENSE-2.0
 *    diente als Vorlage
 *    
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.mediathekview.mlib.tool;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.tool.Log;

import java.io.IOException;
import java.io.InputStream;

public class MSInputStream extends InputStream {

    private final InputStream iStream;
    private long maxBytePerSec = 0;
    private final long startZeit = System.currentTimeMillis();
    private long bytesGelesen = 0;
    private long gesamtVerpennt = 0;
    private static final long warten_ms = 50;

    public MSInputStream(InputStream in) {
        iStream = in;
        maxBytePerSec = Config.bandbreite;
        if (maxBytePerSec < 0) {
            maxBytePerSec = 0;
        }
    }

    @Override
    public int read() throws IOException {
        pause();
        int einByte = iStream.read();
        if (einByte != -1) {
            bytesGelesen++;
        }
        return einByte;
    }

    @Override
    public int read(byte[] b) throws IOException {
        pause();
        int anzByte = iStream.read(b);
        if (anzByte != -1) {
            bytesGelesen += anzByte;
        }
        return anzByte;
    }

    private synchronized void pause() throws IOException {
        if (maxBytePerSec == 0) {
            return;
        }
        if (getBandbreite() > maxBytePerSec) {
            try {
                wait(warten_ms);
                gesamtVerpennt += warten_ms;
            } catch (InterruptedException ex) {
                Log.errorLog(591237096,   ex);
            }
        }
    }

    public long getBandbreite() {
        long dauer = (System.currentTimeMillis() - startZeit) / 1000;
        if (dauer == 0) {
            return bytesGelesen;
        } else {
            return bytesGelesen / dauer;
        }
    }

    public long getTotalSleepTime() {
        return gesamtVerpennt;
    }

    @Override
    public String toString() {
        return "Download: "
                + "gelesen: " + (bytesGelesen > 0 ? bytesGelesen / 1000 : 0) + " kB, "
                + "Bandbreite: " + (getBandbreite() > 0 ? getBandbreite() / 1000 : 0) + " kB/s "
                + ", Wartezeit: " + (gesamtVerpennt > 0 ? gesamtVerpennt / 1000 : 0) + " s";
    }
}
