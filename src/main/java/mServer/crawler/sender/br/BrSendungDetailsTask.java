package mServer.crawler.sender.br;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;
import mServer.crawler.sender.MediathekReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrSendungDetailsTask extends RecursiveTask<Set<DatenFilm>> {
  public static final int MAXIMUM_URLS_PER_TASK = 50;
  private static final long serialVersionUID = 5682617879894452978L;
  private static final Logger LOG = LogManager.getLogger(BrSendungDetailsTask.class);
  private static final String QUERY_TEMPLATE =
      "{\"query\":\"query DetailPageRendererQuery( $clipId: ID! $isClip: Boolean! $isLivestream: Boolean! $livestream: ID!) { viewer { ...DetailPage_viewer_22r5xP id }}fragment DetailPage_viewer_22r5xP on Viewer { ...VideoPlayer_viewer_22r5xP ...ClipActions_viewer detailClip: clip(id: $clipId) { __typename id title ...ClipActions_clip ...ClipInfo_clip ...ChildContentRedirect_creativeWork }}fragment VideoPlayer_viewer_22r5xP on Viewer { id clip(id: $clipId) @include(if: $isClip) { __typename id ageRestriction videoFiles(first: 100) { edges { node { __typename id mimetype publicLocation videoProfile { __typename id width } } } } captionFiles(first: 2) { edges { node { __typename publicLocation id } } } ...Error_clip title } livestream(id: $livestream) @include(if: $isLivestream) { __typename id streamingUrls(first: 10, filter: {accessibleIn: {contains: \\\"GeoZone:http://ard.de/ontologies/coreConcepts#GeoZone_World\\\"}, hasEmbeddedSubtitles: {eq: false}}) { edges { node { __typename id publicLocation } } } }}fragment ClipActions_viewer on Viewer { me { __typename bookmarks(first: 12) { ...BookmarkAction_bookmarks } id }}fragment ClipActions_clip on ClipInterface { id bookmarked downloadable ...BookmarkAction_clip ...Rate_clip ...Share_clip ...Download_clip}fragment ClipInfo_clip on ClipInterface { __typename id title kicker shortDescription description availableUntil ...Duration_clip ... on ProgrammeInterface { publications(first: 1) { edges { node { __typename publishedBy { __typename name id } id } } } broadcasts(first: 1) { edges { node { __typename start id } } } episodeOf { __typename id title scheduleInfo subscribed ...SubscribeAction_series ... on CreativeWorkInterface { ...TeaserImage_creativeWorkInterface } } } ... on ItemInterface { itemOf(first: 1) { edges { node { __typename publications(first: 1) { edges { node { __typename publishedBy { __typename name id } id } } } broadcasts(first: 1) { edges { node { __typename start id } } } episodeOf { __typename id title scheduleInfo subscribed ...SubscribeAction_series ... on CreativeWorkInterface { ...TeaserImage_creativeWorkInterface } } id } } } }}fragment ChildContentRedirect_creativeWork on CreativeWorkInterface { categories(first: 100) { edges { node { __typename id } } }}fragment Duration_clip on ClipInterface { duration}fragment SubscribeAction_series on SeriesInterface { id subscribed}fragment TeaserImage_creativeWorkInterface on CreativeWorkInterface { id kicker title teaserImages(first: 1) { edges { node { __typename shortDescription id } } } defaultTeaserImage { __typename imageFiles(first: 1) { edges { node { __typename id publicLocation crops(first: 10) { count edges { node { __typename publicLocation width height id } } } } } } id }}fragment BookmarkAction_clip on ClipInterface { id}fragment Rate_clip on ClipInterface { id reactions { likes dislikes } myInteractions { __typename reaction { __typename id } id }}fragment Share_clip on ClipInterface { title id}fragment Download_clip on ClipInterface { videoFiles(first: 100) { edges { node { __typename publicLocation videoProfile { __typename height id } id } } }}fragment BookmarkAction_bookmarks on ClipRemoteConnection { count ...TeaserSlider_clipRemoteConnection}fragment TeaserSlider_clipRemoteConnection on ClipRemoteConnection { edges { node { __typename ...SmallTeaserBox_node id } }}fragment SmallTeaserBox_node on Node { id ... on CreativeWorkInterface { ...TeaserImage_creativeWorkInterface } ... on ClipInterface { id title kicker ...Duration_clip } ... on ProgrammeInterface { broadcasts(first: 1, orderBy: START_DESC) { edges { node { __typename start id } } } }}fragment Error_clip on ClipInterface { ageRestriction}\",\"variables\":{\"clipId\":\"%s\",\"isClip\":true,\"isLivestream\":false,\"livestream\":\"Livestream:\"}}";

  private final transient MediathekReader crawler;
  private final ConcurrentLinkedQueue<String> filmIds;
  private final transient Set<DatenFilm> convertedFilms;

  public BrSendungDetailsTask(final MediathekReader aCrawler,
      final ConcurrentLinkedQueue<String> aBrFilmIds) {
    crawler = aCrawler;
    filmIds = aBrFilmIds;
    convertedFilms = ConcurrentHashMap.newKeySet();
  }

  
  private ConcurrentLinkedQueue<String> createSubSet(
      final ConcurrentLinkedQueue<String> aBaseQueue) {
    final int halfSize = aBaseQueue.size() / 2;
    final ConcurrentLinkedQueue<String> urlsToCrawlSubset = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < halfSize; i++) {
      urlsToCrawlSubset.offer(aBaseQueue.poll());
    }
    return urlsToCrawlSubset;
  }

  private void filmIdsToFilms(final ConcurrentLinkedQueue<String> aFilmIds) {
    for (final String filmId : aFilmIds) {
      filmIdToFilm(filmId);
    }
    crawler.meldungProgress("");
  }

  private void filmIdToFilm(final String aFilmId) {
    if (Config.getStop()) {
      return;
    }
      
    BrWebAccessHelper.handleWebAccessExecution(LOG, crawler, () -> {
        
        final Type optionalFilmType = new TypeToken<Optional<DatenFilm>>() {}.getType();
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(optionalFilmType, new BrFilmDeserializer(crawler, aFilmId)).create();
 
        final String response = WebAccessHelper.getJsonResultFromPostAccess(new URL(Consts.BR_API_URL), String.format(QUERY_TEMPLATE, aFilmId));
        
        final Optional<DatenFilm> film = gson.fromJson(response, optionalFilmType);
        if (film.isPresent()) {
            convertedFilms.add(film.get());
        }
    });

  }

  @Override
  protected Set<DatenFilm> compute() {
    if (filmIds.size() <= MAXIMUM_URLS_PER_TASK) {
      filmIdsToFilms(filmIds);
    } else {
      final BrSendungDetailsTask rightTask =
          new BrSendungDetailsTask(crawler, createSubSet(filmIds));
      final BrSendungDetailsTask leftTask = new BrSendungDetailsTask(crawler, filmIds);
      leftTask.fork();
      convertedFilms.addAll(rightTask.compute());
      convertedFilms.addAll(leftTask.join());
    }
    return convertedFilms;
  }

}
