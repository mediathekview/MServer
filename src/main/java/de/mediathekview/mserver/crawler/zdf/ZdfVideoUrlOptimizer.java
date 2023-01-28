package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * tries to find better video qualities than the used ones. checks whether video files with better
 * qualities exists.
 */
public class ZdfVideoUrlOptimizer {

  private static final String NORMAL_1456_13_11 = "1456k_p13v11.mp4";
  private static final String NORMAL_1456_13_12 = "1456k_p13v12.mp4";
  private static final String NORMAL_1496_13_13 = "1496k_p13v13.mp4";
  private static final String NORMAL_1496_13_14 = "1496k_p13v14.mp4";
  private static final String NORMAL_1628_13_15 = "1628k_p13v15.mp4";
  private static final String NORMAL_1628_13_17 = "1628k_p13v17.mp4";
  private static final String NORMAL_2256_14_11 = "2256k_p14v11.mp4";
  private static final String NORMAL_2256_14_12 = "2256k_p14v12.mp4";
  private static final String NORMAL_2296_14_13 = "2296k_p14v13.mp4";
  private static final String NORMAL_2296_14_14 = "2296k_p14v14.mp4";
  private static final String NORMAL_2328_35_11 = "2328k_p35v11.mp4";
  private static final String NORMAL_2328_35_12 = "2328k_p35v12.mp4";
  private static final String NORMAL_2328_35_13 = "2328k_p35v13.mp4";
  private static final String NORMAL_2328_35_14 = "2328k_p35v14.mp4";
  private static final String NORMAL_2360_35_15 = "2360k_p35v15.mp4";
  private static final String NORMAL_2360_35_17 = "2360k_p35v17.mp4";

  private static final String HD_3256 = "3256k_p15v12.mp4";
  private static final String HD_3296 = "3296k_p15v13.mp4";
  private static final String HD_3328_12 = "3328k_p36v12.mp4";
  private static final String HD_3328_13 = "3328k_p36v13.mp4";
  private static final String HD_3328_14 = "3328k_p36v14.mp4";
  private static final String HD_3328_35_14 = "3328k_p35v14.mp4";
  private static final String HD_3360_36_15 = "3360k_p36v15.mp4";
  private static final String HD_3360_36_17 = "3360k_p36v17.mp4";
  private static final String HD_6628_61_17 = "6628k_p61v17.mp4";

  private static final Map<String, String[]> NORMAL_OPTIMIZE = new HashMap<>();
  private static final Map<String, String[]> HD_OPTIMIZE = new HashMap<>();

  static {
    NORMAL_OPTIMIZE.put(NORMAL_1628_13_17, new String[] {NORMAL_2360_35_17});
    NORMAL_OPTIMIZE.put(NORMAL_2256_14_11, new String[] {NORMAL_2328_35_11});
    NORMAL_OPTIMIZE.put(NORMAL_2256_14_12, new String[] {NORMAL_2328_35_12});
    NORMAL_OPTIMIZE.put(NORMAL_2296_14_13, new String[] {NORMAL_2328_35_13});
    NORMAL_OPTIMIZE.put(NORMAL_2296_14_14, new String[] {NORMAL_2328_35_14});
    NORMAL_OPTIMIZE.put(NORMAL_1456_13_11, new String[] {NORMAL_2328_35_11, NORMAL_2256_14_11});
    NORMAL_OPTIMIZE.put(NORMAL_1456_13_12, new String[] {NORMAL_2328_35_12, NORMAL_2256_14_12});
    NORMAL_OPTIMIZE.put(NORMAL_1496_13_13, new String[] {NORMAL_2328_35_13, NORMAL_2296_14_13});
    NORMAL_OPTIMIZE.put(NORMAL_1496_13_14, new String[] {NORMAL_2328_35_14, NORMAL_2296_14_14});
    NORMAL_OPTIMIZE.put(NORMAL_1628_13_15, new String[] {NORMAL_2360_35_15});


    HD_OPTIMIZE.put(NORMAL_2360_35_17, new String[] {HD_6628_61_17, HD_3360_36_17});
    HD_OPTIMIZE.put(NORMAL_1628_13_17, new String[] {HD_6628_61_17, HD_3360_36_17});
    HD_OPTIMIZE.put(NORMAL_1456_13_12, new String[] {HD_3328_12, HD_3256});
    HD_OPTIMIZE.put(NORMAL_2256_14_12, new String[] {HD_3328_12, HD_3256});
    HD_OPTIMIZE.put(NORMAL_2328_35_12, new String[] {HD_3328_12, HD_3256});
    HD_OPTIMIZE.put(NORMAL_1496_13_13, new String[] {HD_3328_13, HD_3296});
    HD_OPTIMIZE.put(NORMAL_2296_14_13, new String[] {HD_3328_13, HD_3296});
    HD_OPTIMIZE.put(NORMAL_2328_35_13, new String[] {HD_3328_13, HD_3296});
    HD_OPTIMIZE.put(NORMAL_1496_13_14, new String[] {HD_3328_14, HD_3328_35_14});
    HD_OPTIMIZE.put(NORMAL_2296_14_14, new String[] {HD_3328_14, HD_3328_35_14});
    HD_OPTIMIZE.put(NORMAL_2328_35_14, new String[] {HD_3328_14, HD_3328_35_14});
    HD_OPTIMIZE.put(NORMAL_1628_13_15, new String[] {HD_3360_36_15});
    HD_OPTIMIZE.put(NORMAL_2360_35_15, new String[] {HD_3360_36_15});
  }

  protected AbstractCrawler crawler;

  public ZdfVideoUrlOptimizer(AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  /**
   * optimizes the normal url.
   *
   * @param aUrl the normal url.
   * @return the optimized url.
   */
  public String getOptimizedUrlNormal(final String aUrl) {
    return optimize(aUrl, NORMAL_OPTIMIZE);
  }

  /**
   * tries to find the hd url depending on the normal url.
   *
   * @param aNormalUrl the normal url.
   * @return the hd url or Optional.empty.
   */
  public Optional<String> determineUrlHd(final String aNormalUrl) {
    if (aNormalUrl == null) {
      return Optional.empty();
    }

    final String url = optimize(aNormalUrl, HD_OPTIMIZE);
    if (url.equalsIgnoreCase(aNormalUrl)) {
      return Optional.empty();
    }
    return Optional.of(url);
  }

  public Optional<String> determineUrlHd(final FilmUrl normalUrl) {
    if (null == normalUrl) {
      return Optional.empty();
    }
    return determineUrlHd(normalUrl.toString());
  }

  private String optimize(final String aUrl, final Map<String, String[]> aOptimizerMap) {
    final Optional<String> fileNameOptional = UrlUtils.getFileName(aUrl);
    if (fileNameOptional.isEmpty()) {
      return aUrl;
    }

    final String fileName = fileNameOptional.get();

    for (final Map.Entry<String, String[]> entry : aOptimizerMap.entrySet()) {

      if (fileName.endsWith(entry.getKey())) {
        final String baseUrl = aUrl.substring(0, aUrl.indexOf(fileName));

        for (final String optimizedFileName : entry.getValue()) {
          final String optimizedUrl = baseUrl + fileName.replace(entry.getKey(), optimizedFileName);

          if (crawler.requestUrlExists(optimizedUrl)) {
            return optimizedUrl;
          }
        }
      }
    }

    return aUrl;
  }
}
