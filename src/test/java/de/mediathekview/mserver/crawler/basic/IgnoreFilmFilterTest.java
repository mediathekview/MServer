package de.mediathekview.mserver.crawler.basic;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class IgnoreFilmFilterTest extends WireMockTestBase {
  File externalFile;
  
  @Before
  public void setUp() {
      try {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        externalFile = folder.newFile( "testfile1.txt" );
        FileWriter fw1 = new FileWriter( externalFile );
        BufferedWriter bw1 = new BufferedWriter( fw1 );
        bw1.write( "content for testfile1\n\n\n\n");
        bw1.close();
      }
      catch( IOException ioe ) {
          System.err.println( 
              "error creating temporary test file in " +
              this.getClass().getSimpleName() );
      }
      setupSuccessfulResponse("/list/ignoreFilmlistAsHttp.txt", "ignoreFilmlist.txt");
  }
  
  @Test
  public void filterTest() {
    final IgnoreFilmFilter ignoreFilmFilterForClasspath = new IgnoreFilmFilter("ignoreFilmlist.txt");
    final IgnoreFilmFilter ignoreFilmFilterForExternal = new IgnoreFilmFilter(externalFile.getAbsolutePath());
    final IgnoreFilmFilter ignoreFilmFilterForHttp = new IgnoreFilmFilter(this.getWireMockBaseUrlSafe()+"/list/ignoreFilmlistAsHttp.txt");
    //
    assertThat(ignoreFilmFilterForClasspath.size(), equalTo(4));
    assertThat(ignoreFilmFilterForExternal.size(), equalTo(1));
    assertThat(ignoreFilmFilterForHttp.size(), equalTo(4));
    //
    final Film filmPositiv0 = new Film(UUID.randomUUID(), Sender.FUNK, "title A", "", LocalDateTime.now(), Duration.ofSeconds(120));
    final Film filmPositiv1 = new Film(UUID.randomUUID(), Sender.FUNK, "title A (Audiodescription)", "", LocalDateTime.now(), Duration.ofSeconds(120));
    final Film filmPositiv2 = new Film(UUID.randomUUID(), Sender.FUNK, "TiTlE a", "", LocalDateTime.now(), Duration.ofSeconds(120));
    final Film filmNegativ = new Film(UUID.randomUUID(), Sender.FUNK, "title B", "", LocalDateTime.now(), Duration.ofSeconds(120));
    final Film filmNegativ1 = new Film(UUID.randomUUID(), Sender.FUNK, "title", "", LocalDateTime.now(), Duration.ofSeconds(120));
    //
    assertThat(ignoreFilmFilterForClasspath.ignoreFilm(filmPositiv0), equalTo(true));
    assertThat(ignoreFilmFilterForClasspath.ignoreFilm(filmPositiv1), equalTo(true));
    assertThat(ignoreFilmFilterForClasspath.ignoreFilm(filmPositiv2), equalTo(true));
    assertThat(ignoreFilmFilterForClasspath.ignoreFilm(filmNegativ), equalTo(false));
    assertThat(ignoreFilmFilterForClasspath.ignoreFilm(filmNegativ1), equalTo(false));
  }

}
