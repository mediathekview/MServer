package de.mediathekview.mserver.crawler.wdr.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.tool.MVHttpClient;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.M3U8Constants;
import de.mediathekview.mserver.crawler.basic.M3U8Dto;
import de.mediathekview.mserver.crawler.basic.M3U8Parser;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfVideoInfoDTO;
import de.mediathekview.mserver.crawler.wdr.WdrMediaDTO;
import de.mediathekview.mserver.crawler.wdr.parser.WdrVideoJsonDeserializer;
import de.mediathekview.mserver.crawler.wdr.parser.WdrVideoLinkDeserializer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.CrawlerTool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class WdrFilmDetailTask extends AbstractDocumentTask<Film, TopicUrlDTO>  {

  private static final Logger LOG = LogManager.getLogger(WdrFilmDetailTask.class);
  
  private static final String DESCRIPTION_SELECTOR = "meta[itemprop=description]";
  private static final String DURATION_SELECTOR = "meta[property=video:duration]";
  private static final String TIME_SELECTOR = "meta[name=dcterms.date]";
  private static final String TITLE_SELECTOR = "meta[itemprop=name]";
  private static final String VIDEO_LINK_SELECTOR = "div.videoLink > a";

  private static final String ATTRIBUTE_CONTENT = "content";
  private static final String ATTRIBUTE_DATA_EXTENSION = "data-extension";
  
  private static final Type OPTIONAL_CRAWLERURLDTO_TYPE_TOKEN = new TypeToken<Optional<CrawlerUrlDTO>>() {}.getType();
  private static final Type OPTIONAL_WDRMEDIADTO_TYPE_TOKEN = new TypeToken<Optional<WdrMediaDTO>>() {}.getType();
  
  private final Gson gson;
  
  public WdrFilmDetailTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    
    gson = new GsonBuilder()
      .registerTypeAdapter(OPTIONAL_CRAWLERURLDTO_TYPE_TOKEN, new WdrVideoLinkDeserializer())
      .registerTypeAdapter(OPTIONAL_WDRMEDIADTO_TYPE_TOKEN, new WdrVideoJsonDeserializer(getProtocol(aUrlToCrawlDTOs)))
      .create();
  }

  @Override
  protected void processDocument(TopicUrlDTO aUrlDTO, Document aDocument) {
    final Optional<String> title = HtmlDocumentUtils.getElementAttributeString(TITLE_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    final Optional<LocalDateTime> time = parseDate(aDocument);
    final Optional<Duration> duration = parseDuration(aDocument);
    final Optional<String> description = HtmlDocumentUtils.getElementAttributeString(DESCRIPTION_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    final Optional<OrfVideoInfoDTO> urls = parseUrls(aDocument);
    
    createFilm(aUrlDTO, urls, title, description, time, duration);    
  }

  @Override
  protected AbstractUrlTask<Film, TopicUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<TopicUrlDTO> aURLsToCrawl) {
    return new WdrFilmDetailTask(crawler, aURLsToCrawl);
  }  
  
  private String getProtocol(ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDTOs) {
    String protocol = "https:";
    
    if (!aUrlToCrawlDTOs.isEmpty()) {
      Optional<String> usedProtocol = UrlUtils.getProtocol(aUrlToCrawlDTOs.element().getUrl());
      if (usedProtocol.isPresent()) {
        protocol = usedProtocol.get();
      }
    }
    
    return protocol;
  }
  
  private void createFilm(final TopicUrlDTO aUrlDTO,
    final Optional<OrfVideoInfoDTO> aVideoInfo, 
    final Optional<String> aTitle,
    final Optional<String> aDescription,
    final Optional<LocalDateTime> aTime,
    final Optional<Duration> aDuration) {
    
    try {
      if (aVideoInfo.isPresent() && aTitle.isPresent()) {
        final Film film = new Film(UUID.randomUUID(), crawler.getSender(), aTitle.get(),
          aUrlDTO.getTheme(), aTime.orElse(LocalDateTime.now()), aDuration.orElse(Duration.ZERO));

        film.setWebsite(new URL(aUrlDTO.getUrl()));
        if (aDescription.isPresent()) {
          film.setBeschreibung(aDescription.get());
        }
     
        OrfVideoInfoDTO videoInfo = aVideoInfo.get();
        if (StringUtils.isNotBlank(videoInfo.getSubtitleUrl())) {
          film.addSubtitle(new URL(videoInfo.getSubtitleUrl()));
        }
        
        addUrls(film, videoInfo.getVideoUrls());
        film.setGeoLocations(CrawlerTool.getGeoLocations(crawler.getSender(), videoInfo.getDefaultVideoUrl()));

        taskResults.add(film);
        crawler.incrementAndGetActualCount();
        crawler.updateProgress();
      } else {
        LOG.error("OrfFilmDetailTask: no title or video found for url " + aUrlDTO.getUrl());
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    } catch (MalformedURLException ex) {
      LOG.fatal("A ORF URL can't be parsed.", ex);
      crawler.printErrorMessage();
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }      
  }
  
  private void addUrls(final Film aFilm, final Map<Resolution, String> aVideoUrls)
    throws MalformedURLException {
    
    for (final Map.Entry<Resolution, String> qualitiesEntry : aVideoUrls.entrySet()) {
      aFilm.addUrl(qualitiesEntry.getKey(), CrawlerTool.stringToFilmUrl(qualitiesEntry.getValue()));
    }
  }

  
  // TODO OrfVideoInfoDTO verallgemeinern
  private Optional<OrfVideoInfoDTO> parseUrls(final Document aDocument) {
    final Optional<CrawlerUrlDTO> videoUrlDto = parseVideoLink(aDocument);
    if (!videoUrlDto.isPresent()) {
      return Optional.empty();
    }
    Optional<String> javaScriptContent = readContent(videoUrlDto.get());
    Optional<String> embeddedJson = extractJsonFromJavaScript(javaScriptContent);
    if (!embeddedJson.isPresent()) {
      return Optional.empty();
    }
      
    Optional<WdrMediaDTO> mediaDto = gson.fromJson(embeddedJson.get(), OPTIONAL_WDRMEDIADTO_TYPE_TOKEN);
    if (!mediaDto.isPresent()) {
      return Optional.empty();
    }
    
    Optional<String> m3u8Content = readContent(mediaDto.get());
    if (!m3u8Content.isPresent()) {
      return Optional.empty();
    }
    
    OrfVideoInfoDTO dto = new OrfVideoInfoDTO();
    if (mediaDto.get().getSubtitle().isPresent()) {
      dto.setSubtitleUrl(mediaDto.get().getSubtitle().get());
    }
    
    M3U8Parser parser = new M3U8Parser();
    List<M3U8Dto> m3u8Data = parser.parse(m3u8Content.get());

    m3u8Data.forEach((entry) -> {
      Optional<Resolution> resolution = getResolution(entry);
      if (resolution.isPresent()) {
        String url = prepareUrl(entry.getUrl());
        dto.put(resolution.get(), url);
      }
    });
    
    return Optional.of(dto);
  }
  
  private static Optional<LocalDateTime> parseDate(final Document aDocument) {
    Optional<String> dateTime = HtmlDocumentUtils.getElementAttributeString(TIME_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    if (dateTime.isPresent()) {
      LocalDateTime localDateTime = LocalDateTime.parse(dateTime.get(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      return Optional.of(localDateTime);
    }
    
    return Optional.empty();
  }
  
  private static Optional<Duration> parseDuration(final Document aDocument) {
    Optional<String> duration = HtmlDocumentUtils.getElementAttributeString(DURATION_SELECTOR, ATTRIBUTE_CONTENT, aDocument);
    if (!duration.isPresent()) {
      return Optional.empty();
    }

    Long durationValue = Long.parseLong(duration.get());
    return Optional.of(Duration.ofSeconds(durationValue));    
  }

  /**
   * parses the video link
   * @param aDocument the html document
   * @return the url of the javascript file containing media infos
   */
  private Optional<CrawlerUrlDTO> parseVideoLink(final Document aDocument) {
    final Optional<String> videoLink = HtmlDocumentUtils.getElementAttributeString(VIDEO_LINK_SELECTOR, ATTRIBUTE_DATA_EXTENSION, aDocument);
    if (videoLink.isPresent()) {

      return gson.fromJson(videoLink.get(), OPTIONAL_CRAWLERURLDTO_TYPE_TOKEN);
    }
    
    return Optional.empty();
  }
  
  /**
   * parses javascript containing media infos and extract the embedded json
   * @param aJsContent the javscript content
   * @return the embedded json content
   */
  private static Optional<String> extractJsonFromJavaScript(final Optional<String> aJsContent) {
    if (aJsContent.isPresent()) {
      int indexBegin = aJsContent.get().indexOf('(');
      int indexEnd = aJsContent.get().lastIndexOf(')');
      String embeddedJson = aJsContent.get().substring(indexBegin + 1, indexEnd);
      return Optional.of(embeddedJson);
    }    
    
    return Optional.empty();
  }
  
  /**
   * reads an url
   * @param aUrl the url
   * @return the content of the url
   */
  private static Optional<String> readContent(final CrawlerUrlDTO aUrl) {
    OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();
    Request request = new Request.Builder()
            .url(aUrl.getUrl()).build();
    try (okhttp3.Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful()) {
        return Optional.of(response.body().string());
      } else {
        LOG.error(String.format("WdrFilmDetailTask: Request '%s' failed: %s", aUrl.getUrl(), response.code()));
      }
    } catch (IOException ex) {
      LOG.error("WdrFilmDetailTask: ", ex);
    }
    
    return Optional.empty();
  }
  
private static Optional<Resolution> getResolution(M3U8Dto aDto) {
    Optional<String> codecMeta = aDto.getMeta(M3U8Constants.M3U8_CODECS);
    Optional<String> resolution = aDto.getMeta(M3U8Constants.M3U8_RESOLUTION);

    // Codec muss "avcl" beinhalten, sonst ist es kein Video
    if (codecMeta.isPresent() && !codecMeta.get().contains("avc1")) {
      return Optional.empty();
    }
    
    // Auflösung verwenden, wenn vorhanden
    if (resolution.isPresent()) {
      switch(resolution.get()) {
        case "320x180":
        case "480x270":
        case "480x272":
        case "512x288":
          return Optional.of(Resolution.SMALL);
        case "640x360":
        case "960x540":
        case "960x544":
          return Optional.of(Resolution.NORMAL);
        case "1280x720":
          return Optional.of(Resolution.HD);
        default:
          LOG.debug("Unknown resolution: " + resolution.get());
      }
    }

    return Optional.empty();
  }  

  /**
   * Bereitet URL für MV auf, so dass Downloads über FFMPEG möglich it
   * @param aUrl die URL aus der m3u8-Datei
   * @return die URL für den Download
   */
  private static String prepareUrl(String aUrl) {
    String url = aUrl;
    
    int indexSuffix = aUrl.lastIndexOf("m3u8");
    if (indexSuffix > 0) {
      url = aUrl.substring(0, indexSuffix + 4);
    }
    
    return url;
  }
}
