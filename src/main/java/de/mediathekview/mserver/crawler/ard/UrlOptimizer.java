package de.mediathekview.mserver.crawler.ard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.daten.Resolution;

public class UrlOptimizer {
  private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(UrlOptimizer.class);
  protected AbstractCrawler crawler;

  public UrlOptimizer(AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  static AtomicInteger good = new AtomicInteger(0);
  static AtomicInteger bad = new AtomicInteger(0);
  public void debug2(String adaptive, Map<Resolution, String> allUrls) {
    Map<Resolution, String> proposal = buildFilmUrlFromAdaptive(adaptive, allUrls.entrySet().stream().findFirst().get().getValue());
  
    if(proposal.size() != allUrls.size() && !adaptive.contains("arte.")) {
      System.out.println("asdf");
      Map<Integer, String> x = buildFromUrl(adaptive, allUrls.entrySet().stream().findFirst().get().getValue());
      StringBuffer sb = new StringBuffer();
      sb.append("#").append(adaptive).append("#").append(printMap(proposal)).append("#vs#").append(printMap(allUrls));
      LOG.debug(sb.toString());
    }
    
    boolean isEqual = proposal.equals(allUrls);
    if (!isEqual && proposal.size() < 3 && allUrls.size() < 3) {
      StringBuffer sb = new StringBuffer();
      sb.append(isEqual).append("#").append(good).append(":").append(bad).append("#").append(adaptive).append("#");
      proposal.forEach((r,url) -> {
        sb.append(r).append("|").append(url);
      });
      sb.append("#vs#");
      allUrls.forEach((r,url) -> {
        sb.append(r).append("|").append(url);
      });
      LOG.info(sb.toString());
      bad.incrementAndGet();
    } else {
      good.incrementAndGet();
    }
  }

  public static String printMap(Map<Resolution, String> urls) {
    StringBuffer sb = new StringBuffer();
    urls.forEach((r,url) -> {
      sb.append(r).append("|").append(url);
    });
    return sb.toString();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  record AdaptiveUrlStructure(String prefix, Map<Integer, String> qualities, String suffix) {
  }

  private static AdaptiveUrlStructure parseAdaptiveUrlStructure(String adaptive) {
    if (adaptive == null || !adaptive.contains(",")) {
      return null;
    }
    // kein split by "/" weil rb diese verwendet
    Pattern p = Pattern.compile("/(?=[^/]*?,)");
    Matcher m = p.matcher(adaptive);
    String metaSegment = "";
    if (m.find()) {
      int start = m.start() + 1;
      int end = adaptive.lastIndexOf('/');
      metaSegment = adaptive.substring(start, end);
    }
    //
    List<String> partsAndMeta = new ArrayList<String>(Arrays.asList(metaSegment.split(",")));
    String prefix = partsAndMeta.getFirst();
    //
    partsAndMeta.removeFirst();
    String suffix = partsAndMeta.getLast();
    suffix = suffix.replace(".csmil", "");
    partsAndMeta.removeLast();
    //
    Map<Integer, String> qualities = new HashMap<Integer, String>();
    for (int i = 0; i < partsAndMeta.size(); i++) {
      qualities.put(i, partsAndMeta.get(i));
    }
    Map<Integer, String> sortedByLength = qualities.entrySet().stream()
        .sorted(Comparator.comparingInt(e -> e.getValue().length())).collect(Collectors.toMap(Map.Entry::getKey,
            Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap<Integer, String>::new));
    //
    if (qualities.values().stream().findAny().get().endsWith(suffix)) {
      suffix = "";
    }
    //
    return new AdaptiveUrlStructure(prefix, sortedByLength, suffix);
  }

  // ---------------------------------------------------------------------------------------------------------------------
  
  public List<int[]> extractResolutionHVFromAdaptive(String adaptive) {
    List<int[]> resolutions = new ArrayList<>();
    String m3uContent;
    try {
      m3uContent = crawler.requestBodyAsString(adaptive);
    } catch (IOException e) {
      LOG.error("{}", e);
      return resolutions;
    }
    String[] lines = m3uContent.split("\n");
    for (String line : lines) {
      line = line.trim();
      if (line.startsWith("#EXT-X-STREAM-INF:")) {
        // Extract the RESOLUTION part
        String[] parts = line.substring("#EXT-X-STREAM-INF:".length()).split(",");
        for (String part : parts) {
          if (part.startsWith("RESOLUTION=")) {
            String resolutionStr = part.substring("RESOLUTION=".length());
            String[] dims = resolutionStr.split("x");
            if (dims.length == 2) {
              try {
                int horizontal = Integer.parseInt(dims[0]);
                int vertical = Integer.parseInt(dims[1]);
                resolutions.add(new int[] { horizontal, vertical });
              } catch (NumberFormatException e) {
                resolutions.add(new int[] { 0, 0 });
              }
            }
            break;
          }
        }
      }
    }
    return resolutions;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------------------  
  
  public Map<Resolution, String> buildFilmUrlFromAdaptive(String adaptive, String aUrl) {
    Map<Resolution, String> result = new HashMap<>();
    Map<Integer, String> rawStringUrlMap = buildFromUrl(adaptive, aUrl);
    rawStringUrlMap.forEach( (resolutionVertical, url) -> {
      try {
      if (crawler.requestUrlExists(url)) {
        Resolution r = ArdConstants.getResolutionFromWidth(resolutionVertical);
        result.computeIfAbsent(r, k -> url);
      } /*else {
        LOG.debug("broken url {} from {}", url, adaptive);
      }*/
      } catch(Exception e) {
        LOG.error("adaptive: {} url: {} error: {}", adaptive, aUrl, e);
      }
    });
    return result;
  }

  public Map<Integer, String> buildFromUrl(String adaptive, String aUrl) {
    if (adaptive.startsWith("https://dra-dd.akamaized.net")) {
      return buildFromUrlForDRA(adaptive, aUrl);
    } else {
      Map<Integer,String> positionToUrl = buildUrlsFromPlaylist(adaptive, aUrl);
      return addResolutionToUrls(adaptive, positionToUrl);
    }
  }
  
  public Map<Integer, String> buildUrlsFromPlaylist(String adaptive, String aUrl) {
    if (adaptive.startsWith("https://dra-dd.akamaized.net")) {
      return buildFromUrlForDRA(adaptive, aUrl);
    } else {
      return buildFromUrlForArdMediathek(adaptive, aUrl);
    }
  }
  
  private Map<Integer, String> addResolutionToUrls(String adaptive, Map<Integer,String> positionToUrl) {
    Map<Integer, String> result = new TreeMap<>(Comparator.reverseOrder());
    if(adaptive == null || adaptive.isBlank() || positionToUrl.size() == 0) {
      return result; 
    }
    List<int[]> hv = extractResolutionHVFromAdaptive(adaptive);
    for (int index = 0; index < hv.size(); index++) {
      if (positionToUrl.containsKey(index)) {
        result.put(hv.get(index)[0], positionToUrl.get(index));
      }
    }
    //
    return result;
  }

  private Map<Integer, String> buildFromUrlForArdMediathek(String adaptive, String aUrl) {
    Map<Integer,String> positionToUrl= new HashMap<>();
    if (adaptive == null || aUrl == null || adaptive.isBlank() || aUrl.isBlank() || !adaptive.contains(",")) {
      return positionToUrl;
    }
    AdaptiveUrlStructure x = parseAdaptiveUrlStructure(adaptive);
    // find the quality of sample url to determine base url
    String matchingPart = "";
    for (String qualityPart : x.qualities.values()) {
      if (aUrl.contains(x.prefix + qualityPart + x.suffix)) {
        matchingPart = x.prefix + qualityPart + x.suffix;
      }
    }
    // url to position
    String baseUrl = aUrl.substring(0, aUrl.length() - matchingPart.length());
    for (Entry<Integer, String> qualityPart : x.qualities.entrySet()) {
      String newUrl = baseUrl + x.prefix + qualityPart.getValue() + x.suffix;
      positionToUrl.put(qualityPart.getKey(), newUrl);
    }
    //
    return positionToUrl;
  }

  private static Map<Integer, String > buildFromUrlForDRA(String adaptive, String aUrl) {
    String newUrl = adaptive.replace("/HLS/", "/mp4/");
    Map<Integer, String> result = new TreeMap<>(Comparator.reverseOrder());
    result.put(360, newUrl.replace("_master.m3u8", "_vod.360.MP4"));
    result.put(540, newUrl.replace("_master.m3u8", "_vod.540.MP4"));
    result.put(720, newUrl.replace("_master.m3u8", "_vod.720.MP4"));
    result.put(1080, newUrl.replace("_master.m3u8", "_vod.1080.MP4"));
    return result;
  }

  
  
  
  
  

}
