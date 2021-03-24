package de.mediathekview.mserver.crawler.kika.json;

import com.google.gson.*;

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.tool.FileSizeDeterminer;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.M3U8Dto;
import de.mediathekview.mserver.crawler.basic.M3U8Parser;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class KikaApiVideoInfoPageDeserializer implements JsonDeserializer<KikaApiVideoInfoDto> {
  private static final String[] TAG_ERROR_CODE = new String[] {"error", "code"};
  private static final String[] TAG_ERROR_MESSAGE = new String[] {"error", "message"};
  private static final String TAG_MP4_ASSETS_ARRAY = "hbbtvAssets";
  private static final String TAG_MP4_URL_TAG = "url";
  private static final String TAG_M3U8_ASSETS_ARRAY = "assets";
  private static final String TAG_M3U8_URL_TAG = "url";
  private static final String[] TAG_HAS_SUB = {"additional", "subtitle"};
  private static final String[] TAG_SUB_XML = {"additional", "closedCaption", "EBU-TT"};
  private static final String[] TAG_SUB_VTT = {"additional", "closedCaption", "WebVTT"};
  //
  private AbstractCrawler crawler;


  
  public KikaApiVideoInfoPageDeserializer(AbstractCrawler aCrawler) {
    this.crawler = aCrawler;
  }

  @Override
  public KikaApiVideoInfoDto deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    KikaApiVideoInfoDto aKikaApiTopicDto = new KikaApiVideoInfoDto();
    // catch error
    Optional<String> errorCode = JsonUtils.getElementValueAsString(jsonElement, TAG_ERROR_MESSAGE);
    Optional<String> errorMessage = JsonUtils.getElementValueAsString(jsonElement, TAG_ERROR_CODE);
    if (errorCode.isPresent()) {
      aKikaApiTopicDto.setError(errorCode, errorMessage);
      return aKikaApiTopicDto;
    }
    // search for urls
    final ArrayList<String> urls = new ArrayList<String>();
    // we will use the mp4 but in case missing - we will take the play list m3u8
    final JsonObject root = jsonElement.getAsJsonObject();
    if (root.has(TAG_MP4_ASSETS_ARRAY)) {
      final JsonArray mp4Array = root.get(TAG_MP4_ASSETS_ARRAY).getAsJsonArray();
      //
      for (JsonElement arrayElement : mp4Array) {
        final Optional<String> url = JsonUtils.getElementValueAsString(arrayElement, TAG_MP4_URL_TAG);
        if (url.isPresent() && url.get().endsWith("mp4")) {
          urls.add(UrlUtils.addProtocolIfMissing(url.get(),UrlUtils.PROTOCOL_HTTPS));
        }
      }
    }
    
    if (urls.size() == 0 && root.has(TAG_M3U8_ASSETS_ARRAY)) {
      final JsonArray m3u8Array = root.get(TAG_M3U8_ASSETS_ARRAY).getAsJsonArray();
      //
      for (JsonElement arrayElement : m3u8Array) {
        final Optional<String> url = JsonUtils.getElementValueAsString(arrayElement, TAG_M3U8_URL_TAG);
        if (url.isPresent()) {
          urls.add(UrlUtils.addProtocolIfMissing(url.get(),UrlUtils.PROTOCOL_HTTPS));
        }
      }
    }
    
    // FIND BEST URL
    if (urls.size() > 0) {
      if (urls.size() > 2) {
        addFilmUrl(aKikaApiTopicDto, urls.get(urls.size()-1));
      }
      if (urls.size() > 1) {
        addFilmUrl(aKikaApiTopicDto, urls.get(urls.size()/2));
      }
      addFilmUrl(aKikaApiTopicDto, urls.get(0));
    }
    // SUBTITLE
    Optional<String> hasSubtitles = JsonUtils.getElementValueAsString(jsonElement, TAG_HAS_SUB);
    if (hasSubtitles.isPresent() && hasSubtitles.get().equalsIgnoreCase("true")) {
      //
      try {
        Optional<String> subXml = JsonUtils.getElementValueAsString(jsonElement, TAG_SUB_XML);          
        if (subXml.isPresent()) {
          aKikaApiTopicDto.add(new URL(UrlUtils.addProtocolIfMissing(subXml.get(), UrlUtils.PROTOCOL_HTTPS)));
        }
        //
        Optional<String> subvtt = JsonUtils.getElementValueAsString(jsonElement, TAG_SUB_VTT);
        if (subvtt.isPresent()) {
          aKikaApiTopicDto.add(new URL(UrlUtils.addProtocolIfMissing(subvtt.get(), UrlUtils.PROTOCOL_HTTPS)));
        }
        //
        if (subXml.isEmpty() && subvtt.isEmpty()) {
          System.out.println("WHAT IS GOING ON ??"+jsonElement);
        }
      } catch (Exception e) {
        System.out.println("FAILED ON " + jsonElement);
        e.printStackTrace();
      }
    }
    return aKikaApiTopicDto;
  }

  private void addFilmUrl(KikaApiVideoInfoDto aKikaApiTopicDto, String url) {
    //final FileSizeDeterminer smallFsd = new FileSizeDeterminer(url);
    try {
      //final FilmUrl filmUrl = new FilmUrl(url, smallFsd.getFileSizeInMiB());
      final FilmUrl filmUrl = new FilmUrl(url, crawler.getConnection().determineFileSize(url));
      aKikaApiTopicDto.addUrl(
          Resolution.SMALL, 
          filmUrl
          );
    } catch (Exception e) {
      System.out.println("FAILED ON " + url);
      e.printStackTrace();
    }
  }
}
