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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.Clip;
import org.apache.commons.lang3.Validate;
import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.BooleanVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.FloatVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.IDVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.IntegerVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.RecursiveAbstractVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.StringVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.VariableList;

public class BrGraphQLQueries {
    
    
    
    private static final String JSON_GRAPHQL_HEADER = "{\"query\":\"";
    
    public static String getQuery2GetFilmCount() {
        
      StringVariable lteVariable = new StringVariable("lte", "2017-11-14T05:00:00.000Z");
      StringVariable gteVariable = new StringVariable("gte", "1970-01-01T05:00:00.000Z");
      List<AbstractVariable> startList = new LinkedList<>();
      startList.add(gteVariable);
      startList.add(lteVariable);
      VariableList startListVariable = new VariableList("start", startList);
      RecursiveAbstractVariable broadcastsVariable = new RecursiveAbstractVariable("broadcasts", startListVariable);
      
      StringVariable eqOntologyVariable = new StringVariable("eq", "av:http://ard.de/ontologies/lifeCycle#published");
      RecursiveAbstractVariable idEqOntologyVariable = new RecursiveAbstractVariable("id", eqOntologyVariable);
      RecursiveAbstractVariable statusIdEqOntologyVariable = new RecursiveAbstractVariable("status", idEqOntologyVariable);
      
      List<AbstractVariable> programmeFilterList = new LinkedList<>();
      programmeFilterList.add(statusIdEqOntologyVariable);
      programmeFilterList.add(broadcastsVariable);
      VariableList programmeFilterVariable = new VariableList("programmeFilter", programmeFilterList);
      programmeFilterVariable.setAsNotNullableType();
      
      List<AbstractVariable> variablesList = new LinkedList<>();
      variablesList.add(programmeFilterVariable);
      VariableList variables = new VariableList(variablesList);
      
      StringBuilder sb = new StringBuilder();
      
      sb.append(JSON_GRAPHQL_HEADER);
      sb.append(getGraphQLHeaderWithVariable("MediathekViewCountFilms",  variables));
        
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
      sb.append(getGraphQLFooterWithVariable(variables));
        
      return sb.toString();
        
    }
    
    public static String getQuery2GetAllClipIds(int clipCount, String cursor) {
      
      BooleanVariable triggerSearchVariable = new BooleanVariable("triggerSearch", true);
      triggerSearchVariable.setAsNotNullableType();
      
      IntegerVariable clipCountVariable = new IntegerVariable("clipCount", clipCount); 
      
      BooleanVariable eqFalseVariable = new BooleanVariable("eq", false);
      RecursiveAbstractVariable emptyEqFalseVariable = new RecursiveAbstractVariable("empty", eqFalseVariable);
      RecursiveAbstractVariable essencesEmptyEqFalseVariable = new RecursiveAbstractVariable("essences", emptyEqFalseVariable);
      
      RecursiveAbstractVariable audioOnlyEqFalseVariable = new RecursiveAbstractVariable("audioOnly", eqFalseVariable);
      
      List<AbstractVariable> clipFilterList = new LinkedList<>();
      clipFilterList.add(audioOnlyEqFalseVariable);
      clipFilterList.add(essencesEmptyEqFalseVariable);
      
      VariableList clipFilter = new VariableList(clipFilterList);
      
      List<AbstractVariable> variablesList = new LinkedList<>();
      variablesList.add(triggerSearchVariable);
      variablesList.add(clipCountVariable);
      variablesList.add(clipFilter);
      
      VariableList variables = new VariableList(variablesList);
      
      
      StringBuilder sb = new StringBuilder();
      
      Map<String, String> variableMap = new HashMap();
      variableMap.put("triggerSearch", "true");
      variableMap.put("clipCount", String.valueOf(clipCount));
      variableMap.put("clipFilter", "{\"audioOnly\":{\"eq\":false},\"essences\":{\"empty\":{\"eq\":false}}}");
      
      
      
      return sb.toString();
      
    }
    
    static String getGraphQLHeaderWithVariable(String queryName, VariableList rootElement) throws IllegalArgumentException {
        
      if(!rootElement.isRootElement())
        throw new IllegalArgumentException("Header basieren auf einer echten rootElement Liste. Bitte diese übergeben!");
      
      StringBuilder sb = new StringBuilder();

      sb.append("query ");
      sb.append(queryName);
      sb.append("(");
        
      List<AbstractVariable> elements = rootElement.getValue();
      
      elements.stream().forEach( (AbstractVariable v) -> {
        sb.append(getVariableNameAsJSON(v.getName()));
        if(v instanceof BooleanVariable) {
          sb.append("Boolean");
        } else if (v instanceof FloatVariable) {
          sb.append("Float");
        } else if (v instanceof IDVariable) {
          sb.append("ID");
        } else if (v instanceof IntegerVariable) {
          sb.append("Int");
        } else if (v instanceof StringVariable) {
          sb.append("String");
        } else {
          sb.append(v.getName().substring(0, 1).toUpperCase());
          sb.append(v.getName().substring(1, v.getName().length()));
        }
        if(v.isNotNullableType()) {
          sb.append("!");
        }
      });
        
      sb.append(") {");
      
      return sb.toString();
        
    }
    
    static String getGraphQLFooterWithVariable(VariableList rootElement) {
      return "\"," + rootElement.getJSONFromVariableOrDefaulNull() + "}";
    }

    private static String getVariableNameAsJSON(String variable) {
      return "  $" + variable.substring(0,1).toLowerCase() + variable.substring(1, variable.length()) + ": ";
    }
    
}


