package de.mediathekview.mserver.crawler.orf.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class OrfHelperTest {

  private final String expectedTheme;
  private final String inputTheme;

  public OrfHelperTest(String inputTheme, String expectedTheme) {
    this.expectedTheme = expectedTheme;
    this.inputTheme = inputTheme;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"Hallo Österreich", "Hallo Österreich"},
          {"Adj'Isten magyarok", "Adj'Isten magyarok"},
          {"ZIB 1", "ZIB 1"},
          {"ZIB 18", "ZIB 18"},
          {"Sport 20", "Sport 20"},
          {"ZIB Flash 19:55", "ZIB Flash"},
          {"ZIB 17:00", "ZIB"},
          {"ZIB 9:00", "ZIB"},
          {"Guten Morgen Österreich 8:30", "Guten Morgen Österreich"},
          {"Guten Morgen Österreich 08:00", "Guten Morgen Österreich"},
          {"Thema hier : hier kommt was anderes", "Thema hier"}
        });
  }

  @Test
  public void parseTheme() {

    final String actualTheme = OrfHelper.parseTheme(inputTheme);
    assertThat(actualTheme, equalTo(expectedTheme));
  }
}
