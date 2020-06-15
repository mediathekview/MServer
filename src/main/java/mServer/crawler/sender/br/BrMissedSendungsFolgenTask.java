package mServer.crawler.sender.br;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URL;
import mServer.crawler.sender.MediathekReader;
import org.glassfish.jersey.uri.UriComponent;

import static org.glassfish.jersey.uri.UriComponent.Type.QUERY_PARAM;

public class BrMissedSendungsFolgenTask implements Callable<Set<String>> {

  private static final Logger LOG = LogManager.getLogger(BrMissedSendungsFolgenTask.class);

  private static final String QUERY_TEMPLATE = Consts.BR_API_URL +
    "?operationName=ProgrammeCalendarPageQuery&variables=%s&extensions=%s";

  private static final String SENDER_BR ="BR_Fernsehen";
  private static final String SENDER_ALPHA ="ARD_alpha";

  private static final String JSON_VARIABLES = "{\"day\":\"%s\",\"slots\":[\"MORNING\",\"NOON\",\"EVENING\",\"NIGHT\"],\"broadcasterId\":\"av:http://ard.de/ontologies/ard#%s\"}";
  private static final String JSON_EXTENSION = "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"ec36d20a87979c32146b32bea1953fe074e62c42d868483638f147a774984ed1\"}}";

  private final MediathekReader crawler;

  private final int maximumDays;

  private final Gson gson;

  public BrMissedSendungsFolgenTask(final MediathekReader aCrawler, int aMaximumDays) {
    crawler = aCrawler;
    maximumDays = aMaximumDays;

    gson = new GsonBuilder()
      .registerTypeAdapter(BrIdsDTO.class, new BrMissedSendungsFolgenDeserializer(crawler))
      .create();
  }

  private static String buildUrl(final LocalDateTime date, final String sender) {
    final String variables = String.format(JSON_VARIABLES, date.format(DateTimeFormatter.ISO_LOCAL_DATE), sender);
    return String.format(QUERY_TEMPLATE, UriComponent.encode(variables, QUERY_PARAM), UriComponent.encode(JSON_EXTENSION, QUERY_PARAM));

  }

  @Override
  public Set<String> call() {
    Set<String> filmIds = new HashSet<>();

    getFilms(filmIds, SENDER_BR);
    getFilms(filmIds, SENDER_ALPHA);

    return filmIds;
  }

  private void getFilms(final Set<String> filmIds, final String sender) {
    for (int i = 0; i < maximumDays + 3; i++) {
      LocalDateTime day = LocalDateTime.now().plus(3, ChronoUnit.DAYS).minusDays(i);

      BrWebAccessHelper.handleWebAccessExecution(LOG, crawler, () -> {

        final String response = WebAccessHelper.getJsonResultFromGetAccess(new URL(buildUrl(day, sender)));
        final BrIdsDTO missedSendungsFolgen = gson.fromJson(response, BrIdsDTO.class);
        filmIds.addAll(missedSendungsFolgen.getIds());
      });
    }
  }
}
