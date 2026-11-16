package de.mediathekview.mserver.crawler.basic;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

/** Parses M3U8 files. */
public class M3U8Parser {

  private static void parseMeta(String aLine, Map<String, String> meta) {
    int index = aLine.indexOf(':');
    if (index > 0) {
      String line = aLine.substring(index + 1);

      while ((index = line.indexOf(',')) > 0) {
        // Sicherstellen, dass Index für , nicht innerhalb eines mit " begrenzten Wertes liegt
        int indexQuote = line.indexOf('\"');
        while (indexQuote + 1 < index && indexQuote != -1) {
          indexQuote = line.indexOf('\"', indexQuote + 1);
          index = line.indexOf(',', indexQuote + 1);
        }

        if (index > 0) {

          parseMetaParameter(line.substring(0, index), meta);

          line = line.substring(index + 1);
        } else {
          // if no , found after the last quote
          break;
        }
      }

      parseMetaParameter(line, meta);
    }
  }

  private static void parseMetaParameter(String aParameter, Map<String, String> meta) {
    String[] parameterParts = aParameter.split("=", 2);
    if (parameterParts.length == 2) {
      meta.put(parameterParts[0], parameterParts[1]);
    }
  }

  private static String unquote(String value) {
    if (value != null && value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  /**
   * Bereitet URL für MV auf, so dass Downloads über FFMPEG möglich it
   *
   * @param aUrl die URL aus der m3u8-Datei
   * @return die URL für den Download
   */
  private static String prepareUrl(String aUrl) {
    String url = aUrl;

    int indexSuffix = aUrl.lastIndexOf(".m3u8");
    if (indexSuffix > 0) {
      url = aUrl.substring(0, indexSuffix + 5);
    }

    return url;
  }

  /**
   * Ermittelt die Paare aus den zusammengehörenden Meta- und URL-Zeilen
   *
   * @param aM3U8Data M3U8-Inhalt
   * @return liste der Zeilenpaare. Im Array ist Index 0 die Metazeile, Index 1 die URL-Zeile
   */
  private static List<String[]> getLinePairs(String aM3U8Data) {
    List<String[]> pairs = new ArrayList<>();

    String currentMeta = null;
    String currentUrl = null;

    String[] lines = StringUtils.split(aM3U8Data, '\n');

    for (String line : lines) {
      if (line.startsWith("#EXT-X-STREAM-INF")) {
        currentMeta = line;
      } else if (line.startsWith("#")) {
        currentMeta = null;
        currentUrl = null;
      } else if (!line.isEmpty()) {
        currentUrl = line;
      }

      if (currentMeta != null && currentUrl != null) {
        pairs.add(new String[] {currentMeta, currentUrl});
        currentMeta = null;
        currentUrl = null;
      }
    }

    return pairs;
  }

  public List<M3U8Dto> parse(String aM3U8Data) {
    List<M3U8Dto> result = new ArrayList<>();

    List<String[]> pairs = getLinePairs(aM3U8Data);
    for (String[] pair : pairs) {
      M3U8Dto dto = new M3U8Dto(prepareUrl(pair[1]));
      parseMeta(pair[0], dto.getMeta());

      result.add(dto);
    }

    pairs.clear();
    result.sort(
        Comparator.comparing(
            (M3U8Dto dto) -> dto.getNormalizedMeta(M3U8Constants.M3U8_RESOLUTION).orElse("")));

    parseSingleLines(aM3U8Data, result);

    return result;
  }

  private void parseSingleLines(String aM3U8Data, List<M3U8Dto> result) {
    String[] lines = StringUtils.split(aM3U8Data, '\n');

    for (String line : lines) {
      if (line.startsWith("#EXT-X-MEDIA")) {
        Map<String, String> meta = new HashMap<>();
        parseMeta(line, meta);

        String type = meta.get(M3U8Constants.M3U8_TYPE);
        if (type == null) {
          continue;
        }
        switch(type) {
          case M3U8Constants.M3U8_TYPE_AUDIO:
            M3U8Dto audioDto = new M3U8Dto(unquote(meta.get(M3U8Constants.M3U8_URI)), M3U8Dto.M3U8Type.MEDIA_AUDIO);
            audioDto.getMeta().putAll(meta);
            result.add(audioDto);
            break;
          case M3U8Constants.M3U8_TYPE_SUBTITLES:
            M3U8Dto subtitleDto = new M3U8Dto(unquote(meta.get(M3U8Constants.M3U8_URI)), M3U8Dto.M3U8Type.MEDIA_SUBTITLE);
            subtitleDto.getMeta().putAll(meta);
            result.add(subtitleDto);
            break;
          default:
            break;
        }
      }
    }
  }
}
