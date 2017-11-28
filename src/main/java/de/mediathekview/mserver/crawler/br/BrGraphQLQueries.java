/*
 * BrGraphQLQueries.java
 * 
 * Projekt    : MServer
 * erstellt am: 17.11.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br;

import java.util.ArrayList;
import java.util.List;

public class BrGraphQLQueries {
    
    private static final String JSON_GRAPHQL_HEADER = "{\"query\":\"";
    
    public static String getQuery2GetFilmCount() {
        
        StringBuilder sb = new StringBuilder();
        
        List<String> keys = new ArrayList<>();
        keys.add("programmeFilter");
        
        sb.append(JSON_GRAPHQL_HEADER);
        sb.append(getGraphQLHeaderWithVariable("MediathekViewCountFilms", keys));
        
        sb.append("  viewer {");
        sb.append("    ...on Viewer {");
        sb.append("      broadcastService(id: \\\"av:http://ard.de/ontologies/ard#BR_Fernsehen\\\") {");
        sb.append("        __typename");
        sb.append("        ...on BroadcastServiceInterface {");
        sb.append("          id");
        sb.append("          programmes(first: 1, orderBy: BROADCASTS_START_ASC, filter: $programmeFilter) {");
        sb.append("            count");
        sb.append("          }");
        sb.append("        }");
        sb.append("      }");
        sb.append("    }");
        sb.append("  }");
        sb.append("}");
        sb.append("\",\"variables\":{\"programmeFilter\":{\"status\":{\"id\":{\"eq\":\"av:http://ard.de/ontologies/lifeCycle#published\"}},\"broadcasts\":{\"start\":{\"gte\":\"1970-01-01T05:00:00.000Z\",\"lte\":\"2017-11-14T05:00:00.000Z\"}}}}}");
        
        return sb.toString();
        
        
    }
    
    static String getGraphQLHeaderWithVariable(String queryName, List<String> variableKeys) {
        
        StringBuilder sb = new StringBuilder();

        sb.append("query ");
        sb.append(queryName);
        sb.append("(");
        
        
        for (String varialeKey : variableKeys) {
            sb.append("  $");
            sb.append(varialeKey.substring(0, 1).toLowerCase());
            sb.append(varialeKey.substring(1, varialeKey.length()));
            sb.append(": ");
            sb.append(varialeKey.substring(0, 1).toUpperCase());
            sb.append(varialeKey.substring(1, varialeKey.length()));
            sb.append("!");
        }
        
        sb.append(") {");
        
        return sb.toString();
        
    }
    
}
