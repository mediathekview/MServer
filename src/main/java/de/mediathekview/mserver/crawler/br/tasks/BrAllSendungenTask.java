package de.mediathekview.mserver.crawler.br.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.json.BrIdsDTO;
import de.mediathekview.mserver.crawler.br.json.BrSendungenIdsDeserializer;

public class BrAllSendungenTask extends RecursiveTask<Set<String>> {
  private static final long serialVersionUID = 8178190311832356223L;

  private static final String QUERY =
      "{\"query\":\"query SeriesIndexRefetchQuery(\\n  $seriesFilter: SeriesFilter\\n) {\\n  viewer {\\n    ...SeriesIndex_viewer_19SNIy\\n    id\\n  }\\n}\\n\\nfragment SeriesIndex_viewer_19SNIy on Viewer {\\n  seriesIndexAllSeries: allSeries(first: 1000, orderBy: TITLE_ASC, filter: $seriesFilter) {\\n    edges {\\n      node {\\n        __typename\\n        id\\n        title\\n        ...SeriesTeaserBox_node\\n        ...TeaserListItem_node\\n      }\\n    }\\n  }\\n}\\n\\nfragment SeriesTeaserBox_node on Node {\\n  __typename\\n  id\\n  ... on CreativeWorkInterface {\\n    ...TeaserImage_creativeWorkInterface\\n  }\\n  ... on SeriesInterface {\\n    ...SubscribeAction_series\\n    subscribed\\n    title\\n  }\\n}\\n\\nfragment TeaserListItem_node on Node {\\n  __typename\\n  id\\n  ... on CreativeWorkInterface {\\n    ...TeaserImage_creativeWorkInterface\\n  }\\n  ... on ClipInterface {\\n    title\\n  }\\n}\\n\\nfragment TeaserImage_creativeWorkInterface on CreativeWorkInterface {\\n  id\\n  kicker\\n  title\\n   }\\n\\nfragment SubscribeAction_series on SeriesInterface {\\n  id\\n  subscribed\\n}\\n\",\"variables\":{\"seriesFilter\":{\"title\":{\"startsWith\":\"*\"},\"audioOnly\":{\"eq\":false},\"status\":{\"id\":{\"eq\":\"Status:http://ard.de/ontologies/lifeCycle#published\"}}}}}";

  private final ForkJoinPool forkJoinPool;
  private final AbstractCrawler crawler;

  public BrAllSendungenTask(final AbstractCrawler aCrawler, final ForkJoinPool aForkJoinPool) {
    crawler = aCrawler;
    forkJoinPool = aForkJoinPool;
  }

  private Set<String> getAllSendungenIds() {
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(Consts.BR_API_URL);
    final Gson gson = new GsonBuilder()
        .registerTypeAdapter(BrIdsDTO.class, new BrSendungenIdsDeserializer()).create();


    final String response = target.request(MediaType.APPLICATION_JSON_TYPE)
        .post(Entity.entity(QUERY, MediaType.APPLICATION_JSON_TYPE), String.class);

    final BrIdsDTO allSendungen = gson.fromJson(response, BrIdsDTO.class);
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
    } catch (InterruptedException | ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return results;
  }

}
