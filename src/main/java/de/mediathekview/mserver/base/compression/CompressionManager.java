package de.mediathekview.mserver.base.compression;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.tukaani.xz.LZMA2Options;

/** A util class to work with compressed files. */
public class CompressionManager {
  private static CompressionManager instance;

  private CompressionManager() {
    super();
  }

  public static CompressionManager getInstance() {
    if (instance == null) {
      instance = new CompressionManager();
    }
    return instance;
  }

  public OutputStream compress(
      final CompressionType aCompressionType, final OutputStream aOutputStream) throws IOException {
    switch (aCompressionType) {
      case XZ:
        return XZCompressorOutputStream.builder().setOutputStream(new BufferedOutputStream(aOutputStream, 512000)).setLzma2Options(new LZMA2Options(9)).get();
      case GZIP:
        return new MediathekviewGZIPOutputStrean(new BufferedOutputStream(aOutputStream, 512000));
      case BZIP:
        // This uses already the best compression.
        return new BZip2CompressorOutputStream(new BufferedOutputStream(aOutputStream, 512000));
      default:
        throw new IllegalArgumentException(
            String.format("The type \"%s\" isn't supported.", aCompressionType.name()));
    }
  }

  /**
   * Compresses a file and uses the old name and appends the file appender based on the {@link
   * CompressionType}.
   *
   * @param aCompressionType The type in which should be compressed.
   * @param aSourceFile The file to compress.
   * @throws IOException Throws an IOException when the file to be compressed does not exists.
   */
  public void compress(final CompressionType aCompressionType, final Path aSourceFile)
      throws IOException {
    compress(aCompressionType, aSourceFile, aSourceFile);
  }

  /**
   * Compresses a file.
   *
   * @param aCompressionType The type in which should be compressed.
   * @param aSourceFile The file to compress.
   * @param aTargetPath The path for the compressed file.
   * @throws IOException Throws an IOException when the file to be compressed does not exists.
   */
  public void compress(
      final CompressionType aCompressionType, final Path aSourceFile, final Path aTargetPath)
      throws IOException {
    final Path targetPath =
        aTargetPath.getFileName().toString().endsWith(aCompressionType.getFileEnding())
            ? aTargetPath
            : aTargetPath.resolveSibling(
                aTargetPath.getFileName().toString() + aCompressionType.getFileEnding());

    try (final BufferedInputStream input = new BufferedInputStream(Files.newInputStream(aSourceFile), 512000);
         final OutputStream output = compress(aCompressionType, Files.newOutputStream(targetPath))) {
      fastChannelCopy(Channels.newChannel(input), Channels.newChannel(output));
    }
  }

  /**
   * Uncompresses a {@link InputStream}.
   *
   * @param aCompressionType The type in which should be compressed.
   * @param aInputStream The {@link InputStream} to uncompress.
   * @return A uncompressed {@link InputStream}
   * @throws IOException Throws an IOException when the file to be decompressed does not exists.
   */
  public InputStream decompress(
      final CompressionType aCompressionType, final InputStream aInputStream) throws IOException {
    switch (aCompressionType) {
      case XZ:
        return new XZCompressorInputStream(aInputStream);
      case GZIP:
        return new GZIPInputStream(aInputStream);
      case BZIP:
        return new BZip2CompressorInputStream(aInputStream);
      default:
        throw new IllegalArgumentException(
            String.format("The type \"%s\" isn't supported.", aCompressionType.name()));
    }
  }

  /**
   * Decompresses a file and uses the old name and removes the file appender based on the {@link
   * CompressionType}.
   *
   * @param aCompressionType The type in which should be compressed.
   * @param aSourceFile The source File to be decompressed
   * @throws IOException Throws an IOException when the file to be decompressed does not exists.
   */
  public void decompress(final CompressionType aCompressionType, final Path aSourceFile)
      throws IOException {
    decompress(
        aCompressionType,
        aSourceFile,
        aSourceFile.resolveSibling(
            aSourceFile.getFileName().toString().replace(aCompressionType.getFileEnding(), "")));
  }

  /**
   * Decompresses a file.
   *
   * @param aCompressionType The type in which should be compressed.
   * @param aSourceFile The file to decompress.
   * @param aTargetPath The path for the decompressed file.
   * @throws IOException Throws an IOException when the file to be decompressed does not exists.
   */
  public void decompress(
      final CompressionType aCompressionType, final Path aSourceFile, final Path aTargetPath)
      throws IOException {
    try (final InputStream input = decompress(aCompressionType, Files.newInputStream(aSourceFile));
         final OutputStream output = new BufferedOutputStream(Files.newOutputStream(aTargetPath))) {
      fastChannelCopy(Channels.newChannel(input), Channels.newChannel(output));
    }
  }

  private void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest)
      throws IOException {
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
}
