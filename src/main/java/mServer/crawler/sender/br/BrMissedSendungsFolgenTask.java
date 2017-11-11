package mServer.crawler.sender.br;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URL;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.br.Consts;

public class BrMissedSendungsFolgenTask implements Callable<Set<String>> {

  private static final Logger LOG = LogManager.getLogger(BrMissedSendungsFolgenTask.class);

  private static final String QUERY_TEMPLATE =
      "{\"query\":\"query ProgrammeCalendarPageQuery(\\n  $broadcasterId: ID!\\n  $livestreamFilter: LivestreamFilter!\\n  $programmeFilter: ProgrammeFilter!\\n  $programmeStageFilter: ProgrammeFilter!\\n) {\\n  viewer {\\n    ...ProgrammeCalendarPage_viewer_5uC0z\\n    id\\n  }\\n}\\n\\nfragment ProgrammeCalendarPage_viewer_5uC0z on Viewer {\\n  broadcastService(id: $broadcasterId) {\\n    __typename\\n    ...ProgrammeStage_broadcastService_4juArI\\n    ...ProgrammeContainer_broadcastService_3zH8HL\\n    id\\n  }\\n  allLivestreams(filter: $livestreamFilter) {\\n    edges {\\n      node {\\n        __typename\\n        id\\n      }\\n    }\\n  }\\n}\\n\\nfragment ProgrammeStage_broadcastService_4juArI on BroadcastServiceInterface {\\n  today: programmes(last: 1, orderBy: BROADCASTS_START_ASC, filter: $programmeStageFilter) {\\n    edges {\\n      node {\\n        __typename\\n        ...ProgrammeInfo_programme\\n        id\\n      }\\n    }\\n  }\\n}\\n\\nfragment ProgrammeContainer_broadcastService_3zH8HL on BroadcastServiceInterface {\\n  id\\n  containerToday: programmes(first: 96, orderBy: BROADCASTS_START_ASC, filter: $programmeFilter) {\\n    ...ProgrammeTable_programmes\\n  }\\n}\\n\\nfragment ProgrammeTable_programmes on ProgrammeConnection {\\n  edges {\\n    node {\\n      __typename\\n      id\\n      ...ProgrammeTableRow_programme\\n    }\\n  }\\n}\\n\\nfragment ProgrammeTableRow_programme on ProgrammeInterface {\\n  ...ProgrammeTeaserBox_programme\\n  title\\n  kicker\\n  broadcasts(first: 1) {\\n    edges {\\n      node {\\n        __typename\\n        start\\n        end\\n        id\\n      }\\n    }\\n  }\\n  id\\n}\\n\\nfragment ProgrammeTeaserBox_programme on ProgrammeInterface {\\n  title\\n  broadcasts(first: 1) {\\n    edges {\\n      node {\\n        __typename\\n        start\\n        end\\n        id\\n      }\\n    }\\n  }\\n  ... on CreativeWorkInterface {\\n    ...TeaserImage_creativeWorkInterface\\n  }\\n  ... on ClipInterface {\\n    title\\n    kicker\\n    essences(first: 1) {\\n      count\\n    }\\n    ...Bookmark_clip\\n    ...Duration_clip\\n  }\\n}\\n\\nfragment TeaserImage_creativeWorkInterface on CreativeWorkInterface {\\n  id\\n  kicker\\n  title\\n  teaserImages(first: 1) {\\n    edges {\\n      node {\\n        __typename\\n        shortDescription\\n        id\\n      }\\n    }\\n  }\\n  defaultTeaserImage {\\n    __typename\\n    imageFiles(first: 1) {\\n      edges {\\n        node {\\n          __typename\\n          id\\n          publicLocation\\n          crops(first: 10) {\\n            count\\n            edges {\\n              node {\\n                __typename\\n                publicLocation\\n                width\\n                height\\n                id\\n              }\\n            }\\n          }\\n        }\\n      }\\n    }\\n    id\\n  }\\n}\\n\\nfragment Bookmark_clip on ClipInterface {\\n  id\\n  bookmarked\\n  title\\n}\\n\\nfragment Duration_clip on ClipInterface {\\n  duration\\n}\\n\\nfragment ProgrammeInfo_programme on ProgrammeInterface {\\n  id\\n  title\\n  kicker\\n  description\\n  broadcasts(first: 1) {\\n    edges {\\n      node {\\n        __typename\\n        start\\n        end\\n        id\\n      }\\n    }\\n  }\\n  ... on ClipInterface {\\n    ...Duration_clip\\n  }\\n}\\n\",\"variables\":{\"broadcasterId\":\"BroadcastService:http://ard.de/ontologies/ard#BR_Fernsehen\",\"livestreamFilter\":{\"broadcastedBy\":{\"id\":{\"eq\":\"BroadcastService:http://ard.de/ontologies/ard#BR_Fernsehen\"}}},\"programmeFilter\":{\"status\":{\"id\":{\"eq\":\"Status:http://ard.de/ontologies/lifeCycle#published\"}},\"broadcasts\":{\"start\":{\"gte\":\"2017-09-22T04:00:00.000Z\",\"lte\":\"2017-09-27T04:00:00.000Z\"}}},\"programmeStageFilter\":{\"status\":{\"id\":{\"eq\":\"Status:http://ard.de/ontologies/lifeCycle#published\"}},\"broadcasts\":{\"start\":{\"gte\":\"%sT00:00:00.000Z\",\"lte\":\"%sT00:00:00.000Z\"}}}}}";

  private final MediathekReader crawler;

  private final int maximumDays;
  
  private BrIdsDTO missedSendungsFolgen;
  
  public BrMissedSendungsFolgenTask(final MediathekReader aCrawler, int aMaximumDays) {
    crawler = aCrawler;
    maximumDays = aMaximumDays;
  }


  @Override
  public Set<String> call() {
    missedSendungsFolgen = new BrIdsDTO();
    
    BrWebAccessHelper.handleWebAccessExecution(LOG, crawler, () -> {

      // 2017-09-19T16:52:25.559Z
      final String fromDateString =
          LocalDateTime.now().plus(3, ChronoUnit.DAYS).format(Consts.BR_FORMATTER);
      // 2017-09-22T04:00:00.000Z
      final String toDateString = LocalDateTime.now()
          .minus(maximumDays, ChronoUnit.DAYS)
          .format(Consts.BR_FORMATTER);

      final Gson gson = new GsonBuilder()
          .registerTypeAdapter(BrIdsDTO.class, new BrMissedSendungsFolgenDeserializer(crawler))
          .create();

      final String response = WebAccessHelper.getJsonResultFromPostAccess(new URL(Consts.BR_API_URL), String.format(QUERY_TEMPLATE, toDateString, fromDateString));
      missedSendungsFolgen = gson.fromJson(response, BrIdsDTO.class);
    });
    
    return missedSendungsFolgen.getIds();
  }
}
