package mServer.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class HashFileWriterTest {
	private static final String FILE_NAME_FILMLISTE_HASH = "filmliste.id";
	private static final Path basePath = Path.of(".");

	@Test
	public void testWriteHash() throws IOException {
		String id = OffsetDateTime.now().toInstant().toString();
		new HashFileWriter(basePath.toString()).writeHash(id);
		assertThat("Das schreiben der Test Filmlisten ID hat nicht geklappt.",
				Files.readAllLines(basePath.resolve(FILE_NAME_FILMLISTE_HASH)).getFirst(),
				Matchers.equalTo(id));
	}

	@AfterEach
	public void deleteIfExist() throws IOException {
		Path filmlistIdPath = basePath.resolve(FILE_NAME_FILMLISTE_HASH);
		if (Files.exists(filmlistIdPath)) {
			Files.delete(filmlistIdPath);
		}
	}
}
