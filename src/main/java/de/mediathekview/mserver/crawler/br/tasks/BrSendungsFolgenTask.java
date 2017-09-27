package de.mediathekview.mserver.crawler.br.tasks;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.json.BrIdsDTO;
import de.mediathekview.mserver.crawler.br.json.BrSendungsFolgenDeserializer;

public class BrSendungsFolgenTask implements Callable<Set<String>> {

  private static final Logger LOG = LogManager.getLogger(BrSendungsFolgenTask.class);
  private static final int VIRTUAL_PAGE_COUNT = 25;
  private static final String QUERY_TEMPLATE =
      "{\"query\":\"query SeriesPageRendererQuery(  $id: ID!  $itemCount: Int  $clipCount: Int  $previousEpisodesFilter: ProgrammeFilter  $clipsOnlyFilter: ProgrammeFilter) {  viewer {    ...SeriesPage_viewer_2PDDaq    id  }}fragment SeriesPage_viewer_2PDDaq on Viewer {  series(id: $id) {    __typename    ...TeaserImage_creativeWorkInterface    ...SeriesBrandBanner_series    clipsOnly: episodes(orderBy: VERSIONFROM_DESC, first: $clipCount, filter: $clipsOnlyFilter) {      ...ProgrammeSlider_programmes    }    previousEpisodes: episodes(first: $itemCount, orderBy: BROADCASTS_START_DESC, filter: $previousEpisodesFilter) {      ...ProgrammeSlider_programmes      edges {        node {          __typename          ...SmallTeaserBox_node          id        }      }    }    id  }}fragment TeaserImage_creativeWorkInterface on CreativeWorkInterface {  id  kicker  title }fragment SeriesBrandBanner_series on SeriesInterface {  ...SubscribeAction_series  title  shortDescription  externalURLS(first: 1) {    edges {      node {        __typename        id        url        label      }    }  }  }fragment ProgrammeSlider_programmes on ProgrammeConnection {  edges {    node {      __typename      ...SmallTeaserBox_node      id    }  }}fragment SmallTeaserBox_node on Node {  id  ... on CreativeWorkInterface {    ...TeaserImage_creativeWorkInterface  }  ... on ClipInterface {    id    title    kicker    ...Bookmark_clip    ...Duration_clip    ...Progress_clip  }  ... on ProgrammeInterface {    broadcasts(first: 1, orderBy: START_DESC) {      edges {        node {          __typename          start          id        }      }    }  }}fragment Bookmark_clip on ClipInterface {  id  bookmarked  title}fragment Duration_clip on ClipInterface {  duration}fragment Progress_clip on ClipInterface {  myInteractions {    __typename    progress    completed    id  }}fragment SubscribeAction_series on SeriesInterface {  id  subscribed}\",\"variables\":{\"id\":\"%s\",\"itemCount\":%d,\"clipCount\":%d,\"previousEpisodesFilter\":{\"essences\":{\"empty\":{\"eq\":false}},\"broadcasts\":{\"empty\":{\"eq\":false},\"start\":{\"lte\":\"%sT24:00:00Z\"}}},\"clipsOnlyFilter\":{\"broadcasts\":{\"empty\":{\"eq\":true}},\"essences\":{\"empty\":{\"eq\":false}}}}}";
  private final MServerBasicConfigDTO config;
  private final String sendungsReihenId;
  private final AbstractCrawler crawler;

  public BrSendungsFolgenTask(final AbstractCrawler aCrawler, final String aSendungsReihenId) {
    crawler = aCrawler;
    config = MServerConfigManager.getInstance().getConfig(aCrawler.getSender());
    sendungsReihenId = aSendungsReihenId;
  }

  @Override
  public Set<String> call() {
    BrIdsDTO sendungsFolgen;
    try {
      final Client client = ClientBuilder.newClient();
      final WebTarget target = client.target(Consts.BR_API_URL);

      final Gson gson = new GsonBuilder()
          .registerTypeAdapter(BrIdsDTO.class, new BrSendungsFolgenDeserializer(crawler)).create();

      // 2017-09-19T16:52:25.559Z
      final String todayDateString = LocalDateTime.now().format(Consts.BR_FORMATTER);
      final int folgenCount = config.getMaximumSubpages() + 1 * VIRTUAL_PAGE_COUNT;
      final String response =
          target.request(MediaType.APPLICATION_JSON_TYPE)
              .post(
                  Entity.entity(String.format(QUERY_TEMPLATE, sendungsReihenId, folgenCount,
                      folgenCount, todayDateString), MediaType.APPLICATION_JSON_TYPE),
                  String.class);
      sendungsFolgen = gson.fromJson(response, BrIdsDTO.class);

    } catch (final JsonSyntaxException jsonSyntaxException) {
      LOG.error(String.format(Consts.JSON_SYNTAX_ERROR,
          Consts.BR_API_URL + " with " + sendungsReihenId, crawler.getSender().getName()),
          jsonSyntaxException);
      crawler.incrementAndGetErrorCount();
      crawler.printErrorMessage();
      sendungsFolgen = new BrIdsDTO();
    }
    return sendungsFolgen.getIds();
  }


}
