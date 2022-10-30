package de.mediathekview.mserver.crawler.br;

import de.mediathekview.mserver.crawler.br.data.BrGraphQLElementNames;
import de.mediathekview.mserver.crawler.br.data.BrGraphQLNodeNames;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.graphql.AbstractVariable;
import de.mediathekview.mserver.crawler.br.graphql.variables.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class BrGraphQLQueries {

  public static final String OBJECT_TITLE_EDGES = "edges";
  public static final String     OBJECT_TITLE_CATEGORIZATIONS = "categorizations";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private static final String JSON_GRAPHQL_HEADER = "{\"query\":\"";
  public static final String SUB_VARIABLE_COUNT = "count";
  public static final String OBJECT_TITLE_AUTHORS = "authors";
  public static final String OBJECT_TITLE_NODE = "node";
  public static final String SUB_VARIABLE_ID = "id";
  public static final String SUB_VARIABLE_NAME = "name";
  public static final String OBJECT_TITLE_SUBJECTS = "subjects";
  public static final String OBJECT_TITLE_TAGS = "tags";
  public static final String SUB_VARIABLE_LABEL = "label";
  public static final String OBJECT_TITLE_EXECUTIVE_PRODUCERS = "executiveProducers";
  public static final String OBJECT_TITLE_CREDITS = "credits";
  public static final String OBJECT_TITLE_GENRES = "genres";

  private BrGraphQLQueries() {}

  public static String getQuery2GetClipDetails(BrID clipId) {

    IDVariable clipID = new IDVariable("clipID", clipId.getId());
    clipID.setAsNotNullableType();
    List<AbstractVariable> variablesList = new LinkedList<>();
    variablesList.add(clipID);

    VariableList rootVariable = new VariableList(variablesList);

    String searchTitle = "MediathekViewGetClipDetails";

    StringBuilder queryBuilder = new StringBuilder();
    queryBuilder.append(JSON_GRAPHQL_HEADER);
    queryBuilder.append(getGraphQLHeaderWithVariable(searchTitle, rootVariable));
    queryBuilder.append(
        addObjectConstruct(
            BrGraphQLNodeNames.RESULT_ROOT_BR_NODE.getName(),
            addObjectConstruct(
                "clipDetails: clip(id: $clipID)",
                BrGraphQLElementNames.GRAPHQL_TYPE_ELEMENT.getName(),
                BrGraphQLElementNames.ID_ELEMENT.getName(),
                BrGraphQLElementNames.STRING_CLIP_TITLE.getName(),
                BrGraphQLElementNames.STRING_CLIP_KICKER.getName(),
                BrGraphQLElementNames.INT_CLIP_DURATION.getName(),
                "ageRestriction",
                BrGraphQLElementNames.STRING_CLIP_DESCRIPTION.getName(),
                BrGraphQLElementNames.STRING_CLIP_SHORT_DESCRIPTION.getName(),
                BrGraphQLElementNames.STRING_CLIP_SLUG.getName(),
                BrGraphQLElementNames.STRING_CLIP_AVAILABLE_UNTIL.getName(),
                addAuthors(),
                addSubjects(),
                addTags(),
                addExecutiveProducers(),
                addCredits(),
                addCategorizations(),
                addGenres(),
                addVideoFiles(),
                addCaptionFiles(),
                addOnItemInterface(),
                addOnProgrammeInterface()),
                SUB_VARIABLE_ID));

    queryBuilder.append("}");

    queryBuilder.append(getGraphQLFooterWithVariable(rootVariable));

    return queryBuilder.toString();
  }

  public static String getQueryGetIds(
      String broadcastServiceName,
      LocalDate start,
      LocalDate end,
      int pageSize,
      Optional<String> cursor) {
    String afterPart = "";
    if (cursor.isPresent()) {
      afterPart = String.format(", after: \\\"%s\\\"", cursor.get());
    }

    return String.format(
        "{\"query\":\"query MediathekViewCountFilms(  $programmeFilter: ProgrammeFilter!) {  viewer {    ...on Viewer {      broadcastService(id: \\\"av:http://ard.de/ontologies/ard#%s\\\") {        __typename        ...on BroadcastServiceInterface {          id           programmes(first: %d, orderBy: INITIALSCREENING_START_DESC, filter: $programmeFilter%s) {   count pageInfo {hasNextPage} edges { node { videoFiles { count } } }         edges { cursor  node { id __typename description broadcasts { edges {node {start}}} initialScreening { start  }}}  }        }      }    }  }}\",\"variables\":{\"programmeFilter\":{\"status\":{\"id\":{\"eq\":\"av:http://ard.de/ontologies/lifeCycle#published\"}},\"broadcasts\":{\"start\":{\"gte\":\"%sT00:00:00.000Z\",\"lte\":\"%sT23:59:59.000Z\"}}}}}",
        broadcastServiceName,
        pageSize,
        afterPart,
        start.format(DATE_FORMATTER),
        end.format(DATE_FORMATTER));
  }

  static String getGraphQLHeaderWithVariable(String queryName, VariableList rootElement)
      throws IllegalArgumentException {

    if (!rootElement.isRootElement())
      throw new IllegalArgumentException(
          "Header basieren auf einer echten rootElement Liste. Bitte diese übergeben!");

    StringBuilder sb = new StringBuilder();

    sb.append("query ");
    sb.append(queryName);
    sb.append("(");

    List<AbstractVariable> elements = rootElement.getValue();

    elements.forEach(
        (AbstractVariable v) -> {
          sb.append(getVariableNameAsJSON(v.getName()));
          if (v instanceof BooleanVariable) {
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
            sb.append(v.getName().substring(1));
          }
          if (v.isNotNullableType()) {
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
    return "  $"
        + variable.substring(0, 1).toLowerCase()
        + variable.substring(1)
        + ": ";
  }

  private static String addAuthors() {
    return addObjectConstruct(OBJECT_TITLE_AUTHORS, SUB_VARIABLE_COUNT, addObjectConstruct(OBJECT_TITLE_EDGES, addObjectConstruct(OBJECT_TITLE_NODE, SUB_VARIABLE_ID, SUB_VARIABLE_NAME)));
  }

  private static String addSubjects() {
    return addObjectConstruct(
            OBJECT_TITLE_SUBJECTS, SUB_VARIABLE_COUNT, addObjectConstruct(OBJECT_TITLE_EDGES, addObjectConstruct(OBJECT_TITLE_NODE, SUB_VARIABLE_ID)));
  }

  private static String addTags() {
    return addObjectConstruct(
            OBJECT_TITLE_TAGS, SUB_VARIABLE_COUNT, addObjectConstruct(OBJECT_TITLE_EDGES, addObjectConstruct(OBJECT_TITLE_NODE, SUB_VARIABLE_ID, SUB_VARIABLE_LABEL)));
  }

  private static String addExecutiveProducers() {
    return addObjectConstruct(
            OBJECT_TITLE_EXECUTIVE_PRODUCERS,
            SUB_VARIABLE_COUNT,
        addObjectConstruct(OBJECT_TITLE_EDGES, addObjectConstruct(OBJECT_TITLE_NODE, SUB_VARIABLE_ID, SUB_VARIABLE_NAME)));
  }

  private static String addCredits() {
    return addObjectConstruct(
            OBJECT_TITLE_CREDITS, SUB_VARIABLE_COUNT, addObjectConstruct(OBJECT_TITLE_EDGES, addObjectConstruct(OBJECT_TITLE_NODE, SUB_VARIABLE_ID, SUB_VARIABLE_NAME)));
  }

  private static String addCategorizations() {
    return addObjectConstruct(
            OBJECT_TITLE_CATEGORIZATIONS, SUB_VARIABLE_COUNT, addObjectConstruct(OBJECT_TITLE_EDGES, addObjectConstruct(OBJECT_TITLE_NODE, SUB_VARIABLE_ID)));
  }

  private static String addGenres() {
    return addObjectConstruct(
            OBJECT_TITLE_GENRES, SUB_VARIABLE_COUNT, addObjectConstruct(OBJECT_TITLE_EDGES, addObjectConstruct(OBJECT_TITLE_NODE, SUB_VARIABLE_ID, SUB_VARIABLE_LABEL)));
  }

  private static String addVideoFiles() {
    return addObjectConstruct(
        "videoFiles(first: 50, orderBy: FILESIZE_DESC)",
            SUB_VARIABLE_COUNT,
        addObjectConstruct(
                OBJECT_TITLE_EDGES,
            addObjectConstruct(
                    OBJECT_TITLE_NODE,
                    SUB_VARIABLE_ID,
                "fileSize",
                "publicLocation",
                addObjectConstruct(
                    "accessibleIn(first: 50)",
                        SUB_VARIABLE_COUNT,
                    addObjectConstruct(OBJECT_TITLE_EDGES, addObjectConstruct(OBJECT_TITLE_NODE, SUB_VARIABLE_ID, "baseIdPrefix"))),
                addObjectConstruct("videoProfile", SUB_VARIABLE_ID, "height", "width"))));
  }

  private static String addCaptionFiles() {
    return addObjectConstruct(
        "captionFiles(first: 50, orderBy: FILESIZE_DESC)",
            SUB_VARIABLE_COUNT,
        addObjectConstruct(OBJECT_TITLE_EDGES, addObjectConstruct(OBJECT_TITLE_NODE, SUB_VARIABLE_ID, "publicLocation")));
  }

  private static String addOnItemInterface() {
    return addObjectConstruct(
        "... on ItemInterface",
        "availableUntil",
        addObjectConstruct(
            "itemOf",
                SUB_VARIABLE_COUNT,
            addObjectConstruct(OBJECT_TITLE_EDGES, addObjectConstruct(OBJECT_TITLE_NODE, SUB_VARIABLE_ID, "title"))));
  }

  private static String addOnProgrammeInterface() {
    return addObjectConstruct(
        "... on ProgrammeInterface",
        "episodeNumber",
        addObjectConstruct(
            "episodeOf", SUB_VARIABLE_ID, "title", "kicker", "scheduleInfo", "shortDescription"),
        addObjectConstruct("initialScreening", "start", SUB_VARIABLE_ID));
  }

  private static String addObjectConstruct(String title, String... subVariables) {

    StringBuilder sb = new StringBuilder();
    sb.append(" ");
    sb.append(title);
    sb.append(" { ");
    Arrays.asList(subVariables).forEach((String name) -> sb.append(name).append(" "));
    sb.append(" } ");

    return sb.toString();
  }
}
