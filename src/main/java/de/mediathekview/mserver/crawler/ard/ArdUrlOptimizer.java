package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArdUrlOptimizer {

  private static final String BR_URL_1280 = "_X.mp4";
  private static final String BR_URL_1920 = "_HD.mp4";
  private static final String HR_URL_1280 = "1280x720-50p-3200kbit.mp4";
  private static final String HR_URL_1920 = "1920x1080-50p-5000kbit.mp4";
  private static final String NDR_URL_1280 = ".hd.mp4";
  private static final String NDR_URL_1920 = ".1080.mp4";
  private static final String RBB_URL_1280 = "hd1080-avc720.mp4";
  private static final String RBB_URL_1920 = "hd1080-avc1080.mp4";
  private static final String SR_URL_1280 = "_P.mp4";
  private static final String SR_URL_1920 = "_H.mp4";
  private static final String SWR_URL_1280 = ".xl.mp4";
  private static final String SWR_URL_1920 = ".xxl.mp4";

  private static final Map<String, String[]> HD_OPTIMIZE = new HashMap<>();

  static {
    HD_OPTIMIZE.put(BR_URL_1280, new String[] {BR_URL_1920});
    HD_OPTIMIZE.put(HR_URL_1280, new String[] {HR_URL_1920});
    HD_OPTIMIZE.put(NDR_URL_1280, new String[] {NDR_URL_1920});
    HD_OPTIMIZE.put(RBB_URL_1280, new String[] {RBB_URL_1920});
    HD_OPTIMIZE.put(SR_URL_1280, new String[] {SR_URL_1920});
    HD_OPTIMIZE.put(SWR_URL_1280, new String[] {SWR_URL_1920});
  }

  protected AbstractCrawler crawler;

  public ArdUrlOptimizer(AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  public String optimizeHdUrl(final String url) {
    String fullHdUrl = "";
    if (url.contains("wdrmedien")) {
      fullHdUrl = determineWdrFullHdUrl(url);
    } else {
      for (Map.Entry<String, String[]> entry : HD_OPTIMIZE.entrySet()) {
        if (url.contains(entry.getKey())) {
          for (String optimizeFragment : entry.getValue()) {
            fullHdUrl = url.replace(entry.getKey(), optimizeFragment);
          }
        }
      }
    }

    if (!fullHdUrl.isEmpty() && crawler.requestUrlExists(fullHdUrl)) {
      return fullHdUrl;
    }

    return url;
  }

  /**
   * wdr urls uses the following pattern: the last part of the filename determines the quality this
   * is the actual order: 1920, 480, 640, 960, 1280 to determine the 1920-url by the 1280-url
   * substract 4 example: 1280: https://wdrmedien-a.akamaihd.net/.../2625725_54085881.mp4 1920:
   * https://wdrmedien-a.akamaihd.net/.../2625725_54085877.mp4
   */
  private String determineWdrFullHdUrl(String url) {
    final Optional<String> fileName = UrlUtils.getFileName(url);
    if (fileName.isPresent()) {
      final String s = fileName.get();
      final String substring = s.substring(s.indexOf("_") + 1).replace(".mp4", "");
      final int hdInt = Integer.parseInt(substring) - 4;
      return url.replace(substring, Integer.toString(hdInt));
    }
    return url;
  }
}
