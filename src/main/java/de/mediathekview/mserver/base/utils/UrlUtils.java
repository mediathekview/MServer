package de.mediathekview.mserver.base.utils;

import de.mediathekview.mlib.tool.MVHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A util class to collect useful URL related methods.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 *     <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public final class UrlUtils {

  public static final String PROTOCOL_HTTPS = "https:";

  private static final String WRONG_PARAMETER_START = "?&";
  private static final String REGEX_ESCAPOR = "\\\\";
  private static final String PARAMETER_PATTERN = "%s=%s";
  private static final String URL_PARAMETER_SEPPERATOR = "&";
  private static final String URL_TO_PARAMETERS_SPLITTERATOR = "?";
  private static final String URL_PARAMETER_REPLACEMENT_REGEX_PATTERN = "%s=[^&]*";

  private UrlUtils() {
    super();
  }

  /**
   * adds the domain if missing.
   *
   * @param aUrl the url to check
   * @param aDomain the domain to add
   * @return the url including the domain
   */
  public static String addDomainIfMissing(final String aUrl, final String aDomain) {
    if (aUrl != null && !aUrl.isEmpty() && aUrl.startsWith("/")) {
      return aDomain + aUrl;
    }

    return aUrl;
  }

  /**
   * adds the protocol if missing.
   *
   * @param aUrl the url to check
   * @param aProtocol the protocol to add
   * @return the url including the protocol
   */
  public static String addProtocolIfMissing(final String aUrl, final String aProtocol) {
    if (aUrl != null && aUrl.startsWith("//")) {
      return aProtocol + aUrl;
    }

    return aUrl;
  }

  /**
   * Changes or adds an URL parameter.
   *
   * @param aUrl The URL which parameter should be changed or gets the parameter added.
   * @param aParameter The parameter which should be changed or added.
   * @param aValue The parameter value.
   * @return The changed URL.
   */
  public static String changeOrAddParameter(
      final String aUrl, final String aParameter, final String aValue) {
    final StringBuilder newUrlBuilder = new StringBuilder();
    final String[] splittedUrl = aUrl.split(REGEX_ESCAPOR + URL_TO_PARAMETERS_SPLITTERATOR);
    newUrlBuilder.append(splittedUrl[0]);

    if (splittedUrl.length == 2) {
      final String cleanedParameters =
          splittedUrl[1]
              + URL_TO_PARAMETERS_SPLITTERATOR
                  .replaceAll(
                      String.format(URL_PARAMETER_REPLACEMENT_REGEX_PATTERN, aParameter), "")
                  .replaceAll(
                      REGEX_ESCAPOR + WRONG_PARAMETER_START, URL_TO_PARAMETERS_SPLITTERATOR);

      newUrlBuilder.append(URL_TO_PARAMETERS_SPLITTERATOR);
      newUrlBuilder.append(cleanedParameters);
      if (!cleanedParameters.endsWith(URL_PARAMETER_SEPPERATOR) && !cleanedParameters.isEmpty()) {
        newUrlBuilder.append(URL_PARAMETER_SEPPERATOR);
      }
    } else {
      newUrlBuilder.append(URL_TO_PARAMETERS_SPLITTERATOR);
    }

    newUrlBuilder.append(String.format(PARAMETER_PATTERN, aParameter, aValue));
    return newUrlBuilder.toString();
  }

  /**
   * checks whether an url exists. uses head request to check.
   *
   * @param aUrl the url to check
   * @return true if url exists else false.
   */
  public static boolean existsUrl(final String aUrl) {
    boolean result = false;

    try {
      final Request request = new Request.Builder().head().url(aUrl).build();
      try (final Response response =
          MVHttpClient.getInstance().getReducedTimeOutClient().newCall(request).execute()) {
        if (response.isSuccessful()) {
          result = true;
        }
      } catch (final IOException ignored) {
      }
    } catch (final Exception ignored) {
    }

    return result;
  }

  /**
   * returns the base of the url example: {@literal https://www.myurl.de:778/some/resource }
   * {@literal -> } {@literal https://www.myurl.de:778 }
   *
   * @param aUrl the url
   * @return the base of the url
   */
  public static String getBaseUrl(final String aUrl) {
    if (aUrl != null) {
      int index = aUrl.indexOf("//");
      if (index > 0) {
        index = aUrl.indexOf('/', index + 2);
      } else {
        index = aUrl.indexOf('/');
      }

      if (index > 0) {
        return aUrl.substring(0, index);
      }
    }

    return aUrl;
  }

  /**
   * returns the file name of the url.
   *
   * @param aUrl the url
   * @return the name of the file
   */
  public static Optional<String> getFileName(final String aUrl) {
    if (aUrl != null) {
      final int index = aUrl.lastIndexOf('/');
      if (index > 0) {
        final String file = aUrl.substring(index + 1);
        if (file.contains(".")) {
          return Optional.of(file);
        }
      }
    }

    return Optional.empty();
  }

  /**
   * returns the file type of the url.
   *
   * @param aUrl the url
   * @return the type of the file
   */
  public static Optional<String> getFileType(final String aUrl) {
    if (aUrl != null) {
      final int index = aUrl.lastIndexOf('.');
      if (index > 0) {
        int indexQuestionMark = aUrl.indexOf('?', index);
        if (indexQuestionMark < 0) {
          indexQuestionMark = aUrl.length();
        }
        return Optional.of(aUrl.substring(index + 1, indexQuestionMark));
      }
    }

    return Optional.empty();
  }

  /**
   * returns the protocol of the url.
   *
   * @param aUrl the url
   * @return the protocol of the url (e.g. "http:")
   */
  public static Optional<String> getProtocol(final String aUrl) {
    if (aUrl != null) {
      final int index = aUrl.indexOf("//");
      if (index > 0) {
        final String protocol = aUrl.substring(0, index);
        return Optional.of(protocol);
      }
    }

    return Optional.empty();
  }

  /**
   * returns the value of an url parameter.
   *
   * @param aUrl the url
   * @param aParameterName the name of the url parameter
   * @return the parameter value
   * @throws UrlParseException Will be thrown if the given URL isn't valid.
   */
  public static Optional<String> getUrlParameterValue(
      final String aUrl, final String aParameterName) throws UrlParseException {
    if (aUrl != null) {
      final Map<String, String> parameters = getUrlParameters(aUrl);
      if (parameters.containsKey(aParameterName)) {
        return Optional.of(parameters.get(aParameterName));
      }
    }

    return Optional.empty();
  }

  private static Map<String, String> getUrlParameters(final String aUrl) throws UrlParseException {
    final Map<String, String> parameters = new HashMap<>();

    final int indexParameterStart = aUrl.indexOf('?');
    if (indexParameterStart > 0) {
      final String parameterPart = aUrl.substring(indexParameterStart + 1);
      final String[] parameterArray = parameterPart.split("&");

      for (final String parameter : parameterArray) {
        final String[] parts = parameter.split("=");
        if (parts.length == 2) {
          parameters.put(parts[0], parts[1]);
        } else {
          throw new UrlParseException("Invalid url paramters: " + aUrl);
        }
      }
    }

    return parameters;
  }

  /**
   * removes the query parameters of the url
   * @param aUrl the url
   * @return the url without query parameters
   */
  public static String removeParameters(String aUrl) {
    if (aUrl == null) {
      return null;
    }

    final int indexParameterStart = aUrl.indexOf('?');
    if (indexParameterStart > 0) {
      return aUrl.substring(0, indexParameterStart);
    }
    return aUrl;
  }
}
