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
package de.mediathekview.mlib.filmlisten;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.jidesoft.utils.SystemInfo;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class WriteFilmlistJson {

    private void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
        while (src.read(buffer) != -1) {
            buffer.flip();
            dest.write(buffer);
            buffer.compact();
        }

        buffer.flip();

        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
    }

    protected JsonGenerator getJsonGenerator(OutputStream os) throws IOException {
        JsonFactory jsonF = new JsonFactory();
        JsonGenerator jg = jsonF.createGenerator(os, JsonEncoding.UTF8);
        //jg.useDefaultPrettyPrinter(); // enable indentation just to make debug/testing easier

        return jg;
    }

    /**
     * Write film data and compress with LZMA2.
     *
     * @param datei      file path
     * @param listeFilme film data
     */
    public void filmlisteSchreibenJsonCompressed(String datei, ListeFilme listeFilme) {
        final String tempFile = datei + "_temp";
        filmlisteSchreibenJson(tempFile, listeFilme);

        try {
            Log.sysLog("Komprimiere Datei: " + datei);
            if (datei.endsWith(Const.FORMAT_XZ)) {
                final Path xz = testNativeXz();
                if (xz != null) {
                    Process p = new ProcessBuilder(xz.toString(), "-9", tempFile).start();
                    final int exitCode = p.waitFor();
                    if (exitCode == 0) {
                        Files.move(Paths.get(tempFile + ".xz"), Paths.get(datei), StandardCopyOption.REPLACE_EXISTING);
                    }
                } else
                    compressFile(tempFile, datei);
            }

            Files.deleteIfExists(Paths.get(tempFile));
        } catch (IOException | InterruptedException ex) {
            Log.sysLog("Komprimieren fehlgeschlagen");
        }
    }

    public void filmlisteSchreibenJson(String datei, ListeFilme listeFilme) {
        try {
            Log.sysLog("Filme schreiben (" + listeFilme.size() + " Filme) :");

            Log.sysLog("   --> Start Schreiben nach: " + datei);
            String sender = "", thema = "";

            if (SystemInfo.isMacOSX()) {
                //Hotfix for OSX 10.12.4 update
                final Path f = Paths.get(datei);
                final Path parentDirectory = f.getParent();
                if (!Files.exists(parentDirectory))
                    Files.createDirectory(parentDirectory);
            }

            try (FileOutputStream fos = new FileOutputStream(datei);
                 JsonGenerator jg = getJsonGenerator(fos)) {

                jg.writeStartObject();
                // Infos zur Filmliste
                jg.writeArrayFieldStart(ListeFilme.FILMLISTE);
                for (int i = 0; i < ListeFilme.MAX_ELEM; ++i) {
                    jg.writeString(listeFilme.metaDaten[i]);
                }
                jg.writeEndArray();
                // Infos der Felder in der Filmliste
                jg.writeArrayFieldStart(ListeFilme.FILMLISTE);
                for (int i = 0; i < DatenFilm.JSON_NAMES.length; ++i) {
                    jg.writeString(DatenFilm.COLUMN_NAMES[DatenFilm.JSON_NAMES[i]]);
                }
                jg.writeEndArray();
                //Filme schreiben
                for (DatenFilm datenFilm : listeFilme) {
                    datenFilm.arr[DatenFilm.FILM_NEU] = Boolean.toString(datenFilm.isNew()); // damit wirs beim nächsten Programmstart noch wissen

                    jg.writeArrayFieldStart(DatenFilm.TAG_JSON_LIST);
                    for (int i = 0; i < DatenFilm.JSON_NAMES.length; ++i) {
                        int m = DatenFilm.JSON_NAMES[i];
                        if (m == DatenFilm.FILM_SENDER) {
                            if (datenFilm.arr[m].equals(sender)) {
                                jg.writeString("");
                            } else {
                                sender = datenFilm.arr[m];
                                jg.writeString(datenFilm.arr[m]);
                            }
                        } else if (m == DatenFilm.FILM_THEMA) {
                            if (datenFilm.arr[m].equals(thema)) {
                                jg.writeString("");
                            } else {
                                thema = datenFilm.arr[m];
                                jg.writeString(datenFilm.arr[m]);
                            }
                        } else {
                            jg.writeString(datenFilm.arr[m]);
                        }
                    }
                    jg.writeEndArray();
                }
                jg.writeEndObject();
                Log.sysLog("   --> geschrieben!");
            }
        } catch (Exception ex) {
            Log.errorLog(846930145, ex, "nach: " + datei);
        }
    }

    private Path testNativeXz() {
        final String[] paths = {"/usr/bin/xz", "/opt/local/bin/xz", "/usr/local/bin/xz"};

        Path xz = null;

        for (String path : paths) {
            xz = Paths.get(path);
            if (Files.isExecutable(xz)) {
                break;
            }
        }

        return xz;
    }

    private void compressFile(String inputName, String outputName) throws IOException {
        try (InputStream input = new FileInputStream(inputName);
             FileOutputStream fos = new FileOutputStream(outputName);
             final OutputStream output = new XZOutputStream(fos, new LZMA2Options());
             final ReadableByteChannel inputChannel = Channels.newChannel(input);
             final WritableByteChannel outputChannel = Channels.newChannel(output)) {

            fastChannelCopy(inputChannel, outputChannel);
        } catch (IOException ignored) {
        }
    }
}
