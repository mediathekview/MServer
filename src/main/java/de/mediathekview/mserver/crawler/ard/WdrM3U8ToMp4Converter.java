package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mserver.daten.Resolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * * Converts an m3u8 playlist URL (in the style shown in the example) * into a list of progressive
 * mp4 URLs. * * Default progressive base host: https://wdr-progressive.ard-mcdn.de * * Example: *
 * input: *
 * https://wdrvod-rwrtr.akamaized.net/i/,/media/.../ID_AVC-,270,360,540,720,1080,.mp4.csmil/index-...m3u8
 * * * produced outputs: * https://wdr-progressive.ard-mcdn.de/media/.../ID_AVC-270.mp4 * ...
 */
public class WdrM3U8ToMp4Converter {

  // Looks for /media/...<codec>-<comma-list>.mp4.csmil
  // group(1) = /media/... (path before codec)
  // group(2) = _<CODEC>- (codec token + trailing '-')
  // group(3) = the comma separated tail that contains bitrate numbers (e.g. ",270,360,540,")
  private static final Pattern MEDIA_PATTERN =
      Pattern.compile(
          "(/media/.+?)(_[A-Za-z0-9]+-)([^/]+?)\\.mp4\\.csmil", Pattern.CASE_INSENSITIVE);
  private static final Logger LOG = LogManager.getLogger(WdrM3U8ToMp4Converter.class);

  private final String progressiveBase; // no trailing slash

  public WdrM3U8ToMp4Converter() {
    this("https://wdr-progressive.ard-mcdn.de");
  }

  public WdrM3U8ToMp4Converter(String progressiveBase) {
    if (progressiveBase == null)
      throw new IllegalArgumentException("progressiveBase must not be null");
    // ensure no trailing slash to make concatenation predictable
    this.progressiveBase = progressiveBase.replaceAll("/+$", "");
  }

  /**
   * Convert an m3u8 url into a map of mp4 URLs.
   *
   * @param m3u8Url the source m3u8 url
   * @return map of mp4 URLs (in the same order as bitrates found)
   * @throws IllegalArgumentException if the url cannot be parsed or no bitrates found
   */
  public Map<Resolution, String> convert(String m3u8Url) {
    if (m3u8Url == null) throw new IllegalArgumentException("m3u8Url must not be null");

    Map<Resolution, String> result = new EnumMap<>(Resolution.class);

    Matcher m = MEDIA_PATTERN.matcher(m3u8Url);
    if (!m.find()) {
      return result;
    }

    String pathBeforeCodec = m.group(1); // includes leading /media/...
    String codecWithDash = m.group(2); // e.g. _AVC-
    String bitrateListPart = m.group(3); // e.g. ",270,360,540,720,1080," or "270,360"

    // build the base path (starts with /media/...)
    String basePrefix = pathBeforeCodec + codecWithDash; // ends with '-'

    // extract numeric tokens (bitrate values)
    Pattern digits = Pattern.compile("\\d+");
    Matcher mDigits = digits.matcher(bitrateListPart);

    while (mDigits.find()) {
      String bitrate = mDigits.group();
      // join progressive base + basePrefix + bitrate + .mp4
      String mp4 = progressiveBase + basePrefix + bitrate + ".mp4";
      final Resolution resolution = getResolutionFromWidth(bitrate);
      result.put(resolution, mp4);
    }

    if (result.isEmpty()) {
      throw new IllegalArgumentException("No numeric bitrate tokens found in m3u8 URL: " + m3u8Url);
    }

    return result;
  }

  private Resolution getResolutionFromWidth(String bitrate) {
    try {
      switch (Integer.parseInt(bitrate)) {
        case 720:
          return Resolution.NORMAL;
        case 1080:
          return Resolution.HD;
        case 270, 360:
          return Resolution.VERY_SMALL;
        case 540:
          return Resolution.SMALL;
        default:
          LOG.warn("Unknown bitrate found in m3u8 URL: {}, defaulting to VERY_SMALL", bitrate);
          return Resolution.VERY_SMALL;
      }
    } catch (NumberFormatException e) {
      LOG.error(e);
      return Resolution.VERY_SMALL;
    }
  }
}
