package de.mediathekview.mserver.crawler.br;

import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.BooleanVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.StringVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.VariableList;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BrGraphQLQueriesTest {

  @Test
  void getQueryGetIdsFirstPage() {
    final LocalDate start = LocalDate.of(2021, 3, 20);
    final LocalDate end = LocalDate.of(2021, 3, 25);
    final String query =
        BrGraphQLQueries.getQueryGetIds(
            BrConstants.BROADCAST_SERVICE_BR, start, end, 10, Optional.empty());

    assertThat(query)
        .isEqualTo(
            "{\"query\":\"query MediathekViewCountFilms(  $programmeFilter: ProgrammeFilter!) {  viewer {    ...on Viewer {      broadcastService(id: \\\"av:http://ard.de/ontologies/ard#BR_Fernsehen\\\") {        __typename        ...on BroadcastServiceInterface {          id           programmes(first: 10, orderBy: INITIALSCREENING_START_DESC, filter: $programmeFilter) {   count pageInfo {hasNextPage} edges { node { videoFiles { count } } }         edges { cursor  node { id __typename description broadcasts { edges {node {start}}} initialScreening { start  }}}  }        }      }    }  }}\",\"variables\":{\"programmeFilter\":{\"status\":{\"id\":{\"eq\":\"av:http://ard.de/ontologies/lifeCycle#published\"}},\"broadcasts\":{\"start\":{\"gte\":\"2021-03-20T00:00:00.000Z\",\"lte\":\"2021-03-25T23:59:59.000Z\"}}}}}");
  }

  @Test
  void getQueryGetIdsNextPage() {
    final LocalDate start = LocalDate.of(2021, 3, 20);
    final LocalDate end = LocalDate.of(2021, 3, 25);
    final String cursor = "eyJfaWQiOiI2MDU2MzhhZjczYjViNzAwMTMwM2U4OGMiLCJfa2V5IjpudWxsfQ==";

    final String query =
        BrGraphQLQueries.getQueryGetIds(
            BrConstants.BROADCAST_SERVICE_BR, start, end, 10, Optional.of(cursor));

    assertThat(query)
        .isEqualTo(
            "{\"query\":\"query MediathekViewCountFilms(  $programmeFilter: ProgrammeFilter!) {  viewer {    ...on Viewer {      broadcastService(id: \\\"av:http://ard.de/ontologies/ard#BR_Fernsehen\\\") {        __typename        ...on BroadcastServiceInterface {          id           programmes(first: 10, orderBy: INITIALSCREENING_START_DESC, filter: $programmeFilter, after: \\\"eyJfaWQiOiI2MDU2MzhhZjczYjViNzAwMTMwM2U4OGMiLCJfa2V5IjpudWxsfQ==\\\") {   count pageInfo {hasNextPage} edges { node { videoFiles { count } } }         edges { cursor  node { id __typename description broadcasts { edges {node {start}}} initialScreening { start  }}}  }        }      }    }  }}\",\"variables\":{\"programmeFilter\":{\"status\":{\"id\":{\"eq\":\"av:http://ard.de/ontologies/lifeCycle#published\"}},\"broadcasts\":{\"start\":{\"gte\":\"2021-03-20T00:00:00.000Z\",\"lte\":\"2021-03-25T23:59:59.000Z\"}}}}}");
  }

  @Test
  void testCreateHeaderWithOneStringVariable() {
    String queryTitle = "MediathekViewCountFilms";

    StringVariable sv = new StringVariable("programmeFilter", "text");
    List<AbstractVariable> rootList = new LinkedList<>();
    rootList.add(sv);
    VariableList vl = new VariableList(rootList);

    assertThat(BrGraphQLQueries.getGraphQLHeaderWithVariable(queryTitle, vl))
        .isEqualTo("query MediathekViewCountFilms(  $programmeFilter: String) {");
  }

  @Test
  void testQuery2GetClipDetails() {
    BrID id = new BrID(BrClipType.PROGRAMME, "av:5a0603ce8c16b90012f4bc49");
    assertThat(BrGraphQLQueries.getQuery2GetClipDetails(id))
        .isEqualTo(
            "{\"query\":\"query MediathekViewGetClipDetails(  $clipID: ID!) { viewer {  clipDetails: clip(id: $clipID) { __typename id title kicker duration ageRestriction description shortDescription slug availableUntil  authors { count  edges {  node { id name  }   }   }   subjects { count  edges {  node { id  }   }   }   tags { count  edges {  node { id label  }   }   }   executiveProducers { count  edges {  node { id name  }   }   }   credits { count  edges {  node { id name  }   }   }   categorizations { count  edges {  node { id  }   }   }   genres { count  edges {  node { id label  }   }   }   videoFiles(first: 50, orderBy: FILESIZE_DESC) { count  edges {  node { id fileSize publicLocation  accessibleIn(first: 50) { count  edges {  node { id baseIdPrefix  }   }   }   videoProfile { id height width  }   }   }   }   captionFiles(first: 50, orderBy: FILESIZE_DESC) { count  edges {  node { id publicLocation  }   }   }   ... on ItemInterface { availableUntil  itemOf { count  edges {  node { id title  }   }   }   }   ... on ProgrammeInterface { episodeNumber  episodeOf { id title kicker scheduleInfo shortDescription  }   initialScreening { start id  }   }   }  id  } }\",\"variables\":{\"clipID\":\"av:5a0603ce8c16b90012f4bc49\"}}");
  }

  @Test
  void testFooterGenerator() {

    BooleanVariable bv = new BooleanVariable("isClip", true);
    List<AbstractVariable> rootList = new LinkedList<>();
    rootList.add(bv);
    VariableList rootElement = new VariableList(rootList);

    assertThat(BrGraphQLQueries.getGraphQLFooterWithVariable(rootElement))
        .isEqualTo("\",\"variables\":{\"isClip\":true}}");
  }
}
