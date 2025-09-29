package de.mediathekview.mlib.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

class MediathekviewGZIPOutputStrean extends GZIPOutputStream {
  MediathekviewGZIPOutputStrean(final OutputStream out) throws IOException {
    super(out);
    def.setLevel(Deflater.BEST_COMPRESSION);
  }
}
