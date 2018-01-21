package de.mediathekview.mserver.base.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class UrlUtilsTestGetUrlParameterValue {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      { null, "test", Optional.empty() },
      { "", "test", Optional.empty() },
      { "https://www.testurl.de/resource?query=3", "query", Optional.of("3") },
      { "https://www.testurl.de/resource?query=result&top=4", "query", Optional.of("result") },
      { "https://www.testurl.de/resource?query=result&top=4", "top", Optional.of("4") },
      { "https://www.testurl.de/resource?query=result&top=4", "notfound", Optional.empty() },
    });
  }
  
  private final String inputUrl;
  private final String parameterName;
  private final Optional<String> expectedParameterValue;
  
  public UrlUtilsTestGetUrlParameterValue(String aInputUrl, String aParameterName, Optional<String> aExpectedParameterValue) {
    inputUrl = aInputUrl;
    parameterName = aParameterName;
    expectedParameterValue = aExpectedParameterValue;
  }
  
  @Test
  public void getUrlParameterValueTest() throws UrlParseException {
    Optional<String> actual = UrlUtils.getUrlParameterValue(inputUrl, parameterName);
    
    assertThat(actual, equalTo(expectedParameterValue));
  }
}
