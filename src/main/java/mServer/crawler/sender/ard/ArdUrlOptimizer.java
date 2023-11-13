package mServer.crawler.sender.ard;

import mServer.crawler.sender.base.UrlUtils;

import java.util.HashMap;
import java.util.Map;

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

  public String optimizeHdUrl(final String url) {
    for (Map.Entry<String, String[]> entry : HD_OPTIMIZE.entrySet()) {
      if (url.contains(entry.getKey())) {
        for (String optimizeFragment : entry.getValue()) {
          final String optimizedUrl = url.replace(entry.getKey(), optimizeFragment);
          if (UrlUtils.existsUrl(optimizedUrl)) {
            return optimizedUrl;
          }
        }
      }
    }

    return url;
  }
}
