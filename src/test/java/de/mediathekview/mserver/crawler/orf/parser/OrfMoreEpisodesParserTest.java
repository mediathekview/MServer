package de.mediathekview.mserver.crawler.orf.parser;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.testhelper.FileReader;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrfMoreEpisodesParserTest {
  @Test
  void parseDocumentWithEpisodes() {
    TopicUrlDTO[] expectedFilms = new TopicUrlDTO[] {
            new TopicUrlDTO("Biester", "https://tvthek.orf.at/profile/Biester/13895917/Biester-Folge-9/14207236"),
            new TopicUrlDTO("Biester", "https://tvthek.orf.at/profile/Biester/13895917/Biester-Folge-8/14207235"),
            new TopicUrlDTO("Biester", "https://tvthek.orf.at/profile/Biester/13895917/Biester-Folge-7/14207234"),
            new TopicUrlDTO("Biester", "https://tvthek.orf.at/profile/Biester/13895917/Biester-Folge-6/14207233"),
            new TopicUrlDTO("Biester", "https://tvthek.orf.at/profile/Biester/13895917/Biester-Folge-5/14207232"),
            new TopicUrlDTO("Biester", "https://tvthek.orf.at/profile/Biester/13895917/Biester-Folge-4/14207231"),
            new TopicUrlDTO("Biester", "https://tvthek.orf.at/profile/Biester/13895917/Biester-Folge-3/14207230"),
            new TopicUrlDTO("Biester", "https://tvthek.orf.at/profile/Biester/13895917/Biester-Folge-2/14207229"),
            new TopicUrlDTO("Biester", "https://tvthek.orf.at/profile/Biester/13895917/Alle-Folgen-jetzt-Biester-1-10/14207227"),
            new TopicUrlDTO("Biester", "https://tvthek.orf.at/profile/Biester/13895917/Biester-Folge-10/14207252"),
    };

    final Document document = Jsoup.parse(FileReader.readFile("/orf/orf_film_more_episodes.html"));

    OrfMoreEpisodesParser target = new OrfMoreEpisodesParser();
    final List<TopicUrlDTO> actual = target.parse(document, "Biester");

    assertEquals(10, actual.size());
    MatcherAssert.assertThat(actual, Matchers.containsInAnyOrder(expectedFilms));
  }
}
