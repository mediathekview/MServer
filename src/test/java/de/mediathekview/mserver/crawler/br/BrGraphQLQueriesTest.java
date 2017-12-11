/*
 * BrGraphQLQueriesTest.java
 * 
 * Projekt : MServer erstellt am: 17.11.2017 Autor : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br;

import static org.junit.Assert.assertEquals;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.BooleanVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.StringVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.VariableList;

public class BrGraphQLQueriesTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testQuery2GetFilmCount() {
    assertEquals(
        "{\"query\":\"query MediathekViewCountFilms(  $programmeFilter: ProgrammeFilter!) {  viewer {    ...on Viewer {      broadcastService(id: \\\"av:http://ard.de/ontologies/ard#BR_Fernsehen\\\") {        __typename        ...on BroadcastServiceInterface {          id          programmes(first: 1, orderBy: BROADCASTS_START_ASC, filter: $programmeFilter) {            count          }        }      }    }  }}\",\"variables\":{\"programmeFilter\":{\"status\":{\"id\":{\"eq\":\"av:http://ard.de/ontologies/lifeCycle#published\"}},\"broadcasts\":{\"start\":{\"gte\":\"1970-01-01T05:00:00.000Z\",\"lte\":\"2017-11-14T05:00:00.000Z\"}}}}}",
        BrGraphQLQueries.getQuery2GetFilmCount());
  }

  @Test
  public void testCreateHeaderWithOneStringVariable() {
    String queryTitle = "MediathekViewCountFilms";

    StringVariable sv = new StringVariable("programmeFilter", "text");
    List<AbstractVariable> rootList = new LinkedList<>();
    rootList.add(sv);
    VariableList vl = new VariableList(rootList);
    
    assertEquals("query MediathekViewCountFilms(  $programmeFilter: String) {", BrGraphQLQueries.getGraphQLHeaderWithVariable(queryTitle, vl));
    
  }
  
  @Test
  public void testFooterGenerator() throws Exception {

    BooleanVariable bv = new BooleanVariable("isClip", true);
    List<AbstractVariable> rootList = new LinkedList<>();
    rootList.add(bv);
    VariableList rootElement = new VariableList(rootList);

    assertEquals("\",\"variables\":{\"isClip\":true}}",
        BrGraphQLQueries.getGraphQLFooterWithVariable(rootElement));

  }

}
