package mServer.tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class HashFileWriterTest {
	private static final String FILE_NAME_FILMLISTE_HASH = "filmliste.id";
	private static final Path basePath = Paths.get(HashFileWriterTest.class.getResource("").getPath());

	@Test
	public void testWriteHash() throws IOException {
		String id = OffsetDateTime.now().toInstant().toString();
		new HashFileWriter(basePath.toString()).writeHash(id);
		Assert.assertThat("Das schreiben der Test Filmlisten ID hat nicht geklappt.",
				Files.readAllLines(basePath.resolve(FILE_NAME_FILMLISTE_HASH), StandardCharsets.UTF_8).get(0),
				Matchers.equalTo(id));
	}

	@After
	public void deleteIfExist() throws IOException {
		Path filmlistIdPath = basePath.resolve(FILE_NAME_FILMLISTE_HASH);
		if (Files.exists(filmlistIdPath)) {
			Files.delete(filmlistIdPath);
		}
	}
}
