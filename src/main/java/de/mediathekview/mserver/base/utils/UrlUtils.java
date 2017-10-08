package de.mediathekview.mserver.base.utils;

/**
 * A util class to collect useful URL related methods.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public final class UrlUtils {
  private static final String WRONG_PARAMETER_START = "?&";
  private static final String REGEX_ESCAPOR = "\\";
  private static final String PARAMETER_PATTERN = "%s=%s";
  private static final String URL_PARAMETER_SEPPERATOR = "&";
  private static final String URL_TO_PARAMETERS_SPLITTERATOR = "?";
  private static final String URL_PARAMETER_REPLACEMENT_REGEX_PATTERN = "%s=[^&]*";

  private UrlUtils() {
    super();
  }

  /**
   * Changes or adds an URL parameter.
   *
   * @param aUrl The URL which parameter should be changed or gets the parameter added.
   * @param aParameter The parameter which should be changed or added.
   * @param aValue The parameter value.
   * @return The changed URL.
   */
  public static String changeOrAddParameter(final String aUrl, final String aParameter,
      final String aValue) {
    final StringBuilder newUrlBuilder = new StringBuilder();
    final String[] splittedUrl = aUrl.split(REGEX_ESCAPOR + URL_TO_PARAMETERS_SPLITTERATOR);
    newUrlBuilder.append(splittedUrl[0]);

    if (splittedUrl.length == 2) {
      final String cleanedParameters = splittedUrl[1]
          .replaceAll(String.format(URL_PARAMETER_REPLACEMENT_REGEX_PATTERN, aParameter), "")
          .replaceAll(REGEX_ESCAPOR + WRONG_PARAMETER_START, URL_TO_PARAMETERS_SPLITTERATOR);
      newUrlBuilder.append(cleanedParameters);
      if (!cleanedParameters.endsWith(URL_PARAMETER_SEPPERATOR)) {
        if (cleanedParameters.isEmpty()) {
          newUrlBuilder.append(URL_TO_PARAMETERS_SPLITTERATOR);
        } else {
          newUrlBuilder.append(URL_PARAMETER_SEPPERATOR);
        }
      }
    } else {
      newUrlBuilder.append(URL_TO_PARAMETERS_SPLITTERATOR);
    }

    newUrlBuilder.append(String.format(PARAMETER_PATTERN, aParameter, aValue));
    return newUrlBuilder.toString();
  }
}
