package mServer.tool;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.mediathekview.mlib.tool.Log;

/**
 * Schreibt den Filmlistenhash bzw. die Filmlisten ID in die Datei <code>filmliste.hash</code>.
 * @author nicklas
 *
 */
public class HashFileWriter {
	private static final String FILE_NAME = "filmliste.id";
	private Path baseDir;

	/**
	 * @param baseDirPath Der Pfad zum Verzeichnis in das geschrieben werden soll.
	 */
	public HashFileWriter(String baseDirPath) {
		baseDir = Paths.get(baseDirPath);
	}

	/**
	 * Schreibt die gegebene ID in die Filmlist Hash Datei.
	 * @param id Die zu schreibende ID.
	 */
	public void writeHash(String id) {
		try (BufferedWriter fileWriter = Files.newBufferedWriter(baseDir.resolve(FILE_NAME), StandardCharsets.UTF_8)) {
			fileWriter.write(id);
		} catch (IOException ioException) {
			Log.errorLog(494461668, ioException, "Der Filmlisten Hash konnte nicht geschrieben werden.");
		}
	}

}
