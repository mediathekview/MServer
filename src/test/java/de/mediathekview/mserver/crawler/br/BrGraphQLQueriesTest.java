/*
 * BrGraphQLQueriesTest.java
 * 
 * Projekt    : MServer
 * erstellt am: 17.11.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

public class BrGraphQLQueriesTest {

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testQuery2GetFilmCount() {
        assertEquals("{\"query\":\"query MediathekViewCountFilms(  $programmeFilter: ProgrammeFilter!) {  viewer {    ...on Viewer {      broadcastService(id: \\\"av:http://ard.de/ontologies/ard#BR_Fernsehen\\\") {        __typename        ...on BroadcastServiceInterface {          id          programmes(first: 1, orderBy: BROADCASTS_START_ASC, filter: $programmeFilter) {            count          }        }      }    }  }}\",\"variables\":{\"programmeFilter\":{\"status\":{\"id\":{\"eq\":\"av:http://ard.de/ontologies/lifeCycle#published\"}},\"broadcasts\":{\"start\":{\"gte\":\"1970-01-01T05:00:00.000Z\",\"lte\":\"2017-11-14T05:00:00.000Z\"}}}}}", BrGraphQLQueries.getQuery2GetFilmCount());
    }

    @Test
    public void testCreateHeaderWithOneVariable() throws Exception {
        
        String          queryTitle      = "MediathekViewCountFilms";
        List<String>    keys            = new ArrayList<>();
        String          variableName    = "programmeFilter";
        
        keys.add(variableName);
        
        assertEquals("query MediathekViewCountFilms(  $programmeFilter: ProgrammeFilter!) {", BrGraphQLQueries.getGraphQLHeaderWithVariable(queryTitle, keys));
        
    }
    
}
