package mServer.tool;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import mServer.crawler.sender.newsearch.Qualities;

/**
 * A class with some Utils to work with M3U8 urls.
 */
public class M3U8Utils {
	public static final String M3U8_WDR_URL_BEGIN = "adaptiv.wdr.de/i/medp/";
	public static final String M3U8_WDR_URL_ALTERNATIV_BEGIN = "wdradaptiv-vh.akamaihd.net/i/medp/";
	private static final String REGEX_FIRST_USELESS_COMMA = "^,";
	private static final String M3U8_WDR_QUALITIES_USELESS_END = ",.mp4.csmil";
	private static final String REGION_WELTWEIT = "weltweit";
	private static final String REGION_WELTWEIT_DOMAIN = "ww";
	private static final String REGEX_ALL_BEFORE_PATTERN = ".*";
	public static final String WDR_MP4_URL_PATTERN = "http://ondemand-%s.wdr.de/medp/%s/%s/%s/%s.mp4";

	private M3U8Utils() {
		super();
	}

	/**
	 * If the URL follows the following structure it will be used to generate
	 * MP4 URLS from it.<br>
	 * This structure is needed:
	 * <code>http://adaptiv.wdr.de/i/medp/[region]/[fsk]/[unkownNumber]/[videoId]/,[Qualitie 01],[Qualitie 02],[Qualitie ...],.mp4.csmil/master.m3u8</code><br>
	 *
	 * @param aWDRM3U8Url
	 *            The M3U8 URL.
	 * @return A Map containing the URLs and Qualities which was found. An empty
	 *         Map if nothing was found.
	 */
	public static Map<Qualities, String> gatherUrlsFromWdrM3U8(String aWDRM3U8Url) {
		Map<Qualities, String> urlAndQualities = new EnumMap<>(Qualities.class);
		if (aWDRM3U8Url.contains(M3U8_WDR_URL_BEGIN) || aWDRM3U8Url.contains(M3U8_WDR_URL_ALTERNATIV_BEGIN)) {
			String m3u8Url = aWDRM3U8Url.replaceAll(REGEX_ALL_BEFORE_PATTERN+M3U8_WDR_URL_BEGIN, "").replaceAll(REGEX_ALL_BEFORE_PATTERN+M3U8_WDR_URL_ALTERNATIV_BEGIN, "");
			urlAndQualities.putAll(convertM3U8Url(m3u8Url));
		}
		return urlAndQualities;
	}

	private static Map<? extends Qualities, ? extends String> convertM3U8Url(String m3u8Url) {
		Map<Qualities, String> urlAndQualities = new EnumMap<>(Qualities.class);
		String[] splittedM3U8Url = StringUtils.split(m3u8Url, '/');
		if (splittedM3U8Url.length >= 6) {
			String region = splittedM3U8Url[0];
			if(REGION_WELTWEIT.equals(region))
			{
				region = REGION_WELTWEIT_DOMAIN;
			}
			String fsk = splittedM3U8Url[1];
			String unkownNumber = splittedM3U8Url[2];
			String videoId = splittedM3U8Url[3];

			// From lowest to best quality;
			String urlQualityPartsText = splittedM3U8Url[4];
			if (StringUtils.isNoneEmpty(region, fsk, unkownNumber, videoId, urlQualityPartsText)) {
				// Remove useless begin and end.
				urlQualityPartsText = urlQualityPartsText.replaceFirst(REGEX_FIRST_USELESS_COMMA, "")
						.replaceFirst(M3U8_WDR_QUALITIES_USELESS_END, "");

				urlAndQualities.putAll(gatherQualities(region, fsk, unkownNumber, videoId, urlQualityPartsText));
			}
			// The remaining is irrelevant.
		}
		return urlAndQualities;
	}

	private static Map<? extends Qualities, ? extends String> gatherQualities(String region, String fsk,
			String unkownNumber, String videoId, String urlQualityPartsText) {
		Map<Qualities, String> urlAndQualities = new EnumMap<>(Qualities.class);
		List<String> urlQualityParts = Arrays.asList(StringUtils.split(urlQualityPartsText, ','));
		if (urlQualityParts.size() == 1) {
			urlAndQualities.put(Qualities.SMALL,
					String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId, urlQualityParts.get(0)));
		} else if (urlQualityParts.size() == 2) {
			urlAndQualities.put(Qualities.SMALL,
					String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId, urlQualityParts.get(0)));
			urlAndQualities.put(Qualities.NORMAL,
					String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId, urlQualityParts.get(1)));
		} else if (urlQualityParts.size() >= 3) {
			List<String> bestThreeUrlQualityParts = urlQualityParts.subList(urlQualityParts.size() - 3,
					urlQualityParts.size());
			urlAndQualities.put(Qualities.SMALL, String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId,
					bestThreeUrlQualityParts.get(0)));
			urlAndQualities.put(Qualities.NORMAL, String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId,
					bestThreeUrlQualityParts.get(1)));
			urlAndQualities.put(Qualities.HD, String.format(WDR_MP4_URL_PATTERN, region, fsk, unkownNumber, videoId,
					bestThreeUrlQualityParts.get(2)));
		}
		return urlAndQualities;
	}
}
