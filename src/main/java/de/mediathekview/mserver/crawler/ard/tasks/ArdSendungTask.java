package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.AbstractCrawler;
import de.mediathekview.mserver.crawler.AbstractUrlTask;
import de.mediathekview.mserver.crawler.ard.json.ArdBasicInfoDTO;
import de.mediathekview.mserver.crawler.ard.json.ArdBasicInfoJsonDeserializer;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoDTO;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoJsonDeserializer;
import mServer.crawler.CrawlerTool;
import mServer.tool.MserverDatumZeit;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recursively crawls the ARD Sendungsfolge page.
 */
public class ArdSendungTask extends AbstractUrlTask
{
    private static final Logger LOG = LogManager.getLogger(ArdSendungTask.class);
    private static final String REGEX_PATTERN_DOCUMENT_ID = "(?<=&documentId=)\\d+";
    private static final String LOAD_DOCUMENT_ERRORTEXTPATTERN = "Something terrible happened while convert \"%s\" to a film.";
    private static final String URL_BASE_PATTERN_BASIC_INFO = "http://www.ardmediathek.de/play/sola/%s";
    private static final String URL_BASE_PATTERN_VIDEO_INFO = "http://www.ardmediathek.de/play/media/%s?devicetype=pc&features=flash";
    private static final Type QUALITY_MAP_TYPE = new TypeToken<Map<Qualities, String>>()
    {
    }.getType();
    private static final String SELECTOR_CLIP_INFO = "div.modClipinfo p.subtitle";
    private static final String SPLITTERATOR_CLIP_INFO = "\\s\\|\\s";
    private static final String REGEX_PATTERN_NUMBERS = "\\d+";

    private final ConcurrentHashMap<String, String> urlsSendezeitenMap;

    public ArdSendungTask(final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<String> aUrlsToCrawl, ConcurrentHashMap<String, String> aUrlsSendezeitenMap)
    {
        super(aCrawler, aUrlsToCrawl);
        urlsSendezeitenMap = aUrlsSendezeitenMap;
    }

    @Override
    protected AbstractUrlTask createNewOwnInstance()
    {
        return new ArdSendungTask(crawler, urlsToCrawl, urlsSendezeitenMap);
    }

    @Override
    protected void processDocument(final String aUrl, final Document aDocument)
    {
        try
        {
            final String[] clipInfo = getClipInfo(aDocument);
            String datumAsText = gatherDatum(clipInfo);
            int dauerInMinutes = gatherDauerInMinutes(clipInfo);

            String sendezeitAsText = urlsSendezeitenMap.get(aUrl);
            String documentId = getDocumentIdFromUrl(aUrl);

            Gson gson = createGson();
            ArdBasicInfoDTO basicInfo = gson.fromJson(
                    new InputStreamReader(
                            new URL(buildBasicInfoUrl(documentId)
                            ).openStream())
                    , ArdBasicInfoDTO.class);

            ArdVideoInfoDTO videoInfo = gson.fromJson(
                    new InputStreamReader(
                            new URL(buildVideoInfoUrl(documentId)
                            ).openStream())
                    , ArdVideoInfoDTO.class);

            Sender sender = getSenderFromName(basicInfo.getSenderName());

            Film newFilm = new Film(UUID.randomUUID(),
                    CrawlerTool.getGeoLocations(sender, videoInfo.getDefaultVideoUrl()),
                    sender,
                    basicInfo.getTitle(),
                    basicInfo.getThema(),
                    MserverDatumZeit.parseDateTime(datumAsText, sendezeitAsText),
                    Duration.of(dauerInMinutes, ChronoUnit.MINUTES),
                    new URI(aUrl));
            if(StringUtils.isNotBlank(videoInfo.getSubtitleUrl()))
            {
                newFilm.addSubtitle(new URI(videoInfo.getSubtitleUrl()));
            }
            addUrls(newFilm,videoInfo.getVideoUrls());
            filmTasks.add(newFilm);
            crawler.incrementAndGetActualCount();
            crawler.updateProgress();
        } catch (Exception exception)
        {
            LOG.error(String.format(LOAD_DOCUMENT_ERRORTEXTPATTERN, crawler.getSender().getName(), aUrl), exception);
            crawler.printErrorMessage();
            crawler.incrementAndGetErrorCount();
        }
    }

    private void addUrls(final Film aFilm, final Map<Qualities, String> aVideoUrls) throws URISyntaxException
    {
        for(Qualities qualitiy : aVideoUrls.keySet())
        {
            aFilm.addUrl(qualitiy, CrawlerTool.stringToFilmUrl(aVideoUrls.get(qualitiy)));
        }
    }

    private String[] getClipInfo(final Document aDocument)
    {
        return aDocument.select(SELECTOR_CLIP_INFO).text().split(SPLITTERATOR_CLIP_INFO);
    }

    private String gatherDatum(final String[] aClipInfo)
    {
        return aClipInfo[0];
    }

    private int gatherDauerInMinutes(final String[] aClipInfo)
    {
        Matcher numberMatcher = Pattern.compile(REGEX_PATTERN_NUMBERS).matcher(aClipInfo[1]);
        numberMatcher.find();
        return Integer.parseInt(numberMatcher.group());
    }

    private Sender getSenderFromName(final String aSenderName)
    {
        Sender foundSender = Sender.getSenderByName(aSenderName);
        return foundSender == null ? Sender.ARD : foundSender;
    }

    private String buildBasicInfoUrl(final String aDocumentId)
    {
        return String.format(URL_BASE_PATTERN_BASIC_INFO, aDocumentId);
    }

    private String buildVideoInfoUrl(final String aDocumentId)
    {
        return String.format(URL_BASE_PATTERN_VIDEO_INFO, aDocumentId);
    }

    private Gson createGson()
    {
        return new GsonBuilder()
                .registerTypeAdapter(ArdBasicInfoDTO.class, new ArdBasicInfoJsonDeserializer())
                .registerTypeAdapter(ArdVideoInfoDTO.class, new ArdVideoInfoJsonDeserializer())
                .create();
    }

    private String getDocumentIdFromUrl(final String aUrl)
    {
        Matcher documentIdRegexMatcher = Pattern.compile(REGEX_PATTERN_DOCUMENT_ID).matcher(aUrl);
        documentIdRegexMatcher.find();
        return documentIdRegexMatcher.group();
    }

}
