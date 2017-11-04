package mServer.crawler.sender.br;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URL;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;

public class BrAllSendungenTask extends RecursiveTask<Set<String>> {

  private static final Logger LOG = LogManager.getLogger(BrAllSendungenTask.class);
  private static final long serialVersionUID = 8178190311832356223L;

  private static final String QUERY =
      "{\"query\":\"query SeriesIndexRefetchQuery(\\n  $seriesFilter: SeriesFilter\\n) {\\n  viewer {\\n    ...SeriesIndex_viewer_19SNIy\\n    id\\n  }\\n}\\n\\nfragment SeriesIndex_viewer_19SNIy on Viewer {\\n  seriesIndexAllSeries: allSeries(first: 1000, orderBy: TITLE_ASC, filter: $seriesFilter) {\\n    edges {\\n      node {\\n        __typename\\n        id\\n        title\\n        ...SeriesTeaserBox_node\\n        ...TeaserListItem_node\\n      }\\n    }\\n  }\\n}\\n\\nfragment SeriesTeaserBox_node on Node {\\n  __typename\\n  id\\n  ... on CreativeWorkInterface {\\n    ...TeaserImage_creativeWorkInterface\\n  }\\n  ... on SeriesInterface {\\n    ...SubscribeAction_series\\n    subscribed\\n    title\\n  }\\n}\\n\\nfragment TeaserListItem_node on Node {\\n  __typename\\n  id\\n  ... on CreativeWorkInterface {\\n    ...TeaserImage_creativeWorkInterface\\n  }\\n  ... on ClipInterface {\\n    title\\n  }\\n}\\n\\nfragment TeaserImage_creativeWorkInterface on CreativeWorkInterface {\\n  id\\n  kicker\\n  title\\n   }\\n\\nfragment SubscribeAction_series on SeriesInterface {\\n  id\\n  subscribed\\n}\\n\",\"variables\":{\"seriesFilter\":{\"title\":{\"startsWith\":\"*\"},\"audioOnly\":{\"eq\":false},\"status\":{\"id\":{\"eq\":\"Status:http://ard.de/ontologies/lifeCycle#published\"}}}}}";

  private final transient ForkJoinPool forkJoinPool;
  private final transient MediathekReader crawler;

  private BrIdsDTO allSendungen;
  
  public BrAllSendungenTask(final MediathekReader aCrawler, final ForkJoinPool aForkJoinPool) {
    crawler = aCrawler;
    forkJoinPool = aForkJoinPool;
  }

  private Set<String> getAllSendungenIds() {
    allSendungen = new BrIdsDTO();
    
    BrWebAccessHelper.handleWebAccessExecution(LOG, crawler, () -> {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(BrIdsDTO.class, new BrSendungenIdsDeserializer()).create();
        final String response = WebAccessHelper.getJsonResultFromPostAccess(new URL(Consts.BR_API_URL), QUERY);
        
        allSendungen = gson.fromJson(response, BrIdsDTO.class);  
    });

    LOG.debug("BR Anzahl Sendungen: " + allSendungen.getIds().size());
    return allSendungen.getIds();
  }

  @Override
  protected Set<String> compute() {
    final Set<String> results = ConcurrentHashMap.newKeySet();

    try {
      final Set<String> sendungenIds = getAllSendungenIds();

      final List<ForkJoinTask<Set<String>>> futureSendungsfolgenTasks = new ArrayList<>();
      for (final String sendungsId : sendungenIds) {
        futureSendungsfolgenTasks
            .add(forkJoinPool.submit(new BrSendungsFolgenTask(crawler, sendungsId)));
      }

      for (final ForkJoinTask<Set<String>> featureSendungsfolgenTask : futureSendungsfolgenTasks) {
        results.addAll(featureSendungsfolgenTask.get());
      }
    } catch (InterruptedException | ExecutionException exception) {
      LOG.error("Something wen't terrible wrong while getting the Folgen for a BR Sendung.",
          exception);
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLER);
    }

    return results;
  }

}
