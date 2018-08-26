package de.mediathekview.mserver.crawler.arte.tasks;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.arte.ArteCrawlerUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.json.ArteSubcategoryListDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class ArteSubcategorysTask implements Callable<Set<ArteCrawlerUrlDto>> {
  private static final Logger LOG = LogManager.getLogger(ArteSubcategorysTask.class);
  private static final String HEADER_AUTHORIZATION = "Authorization";
  private static final String ENCODING_GZIP = "gzip";
  private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  /**
   * The parameter needs the language code.
   */
  private static final String SUBCATEGORIES_URL_PATTERN =
      "https://api.arte.tv/api/opa/v3/subcategories?language=%s&fields=code,label";

  private final AbstractCrawler crawler;
  private final ArteLanguage language;
  private final String authKey;
  private final Client client;
  private final GsonBuilder gsonBuilder;

  public ArteSubcategorysTask(final AbstractCrawler aCrawler, final ArteLanguage aLanguage,
      final String aAuthKey) {
    crawler = aCrawler;
    language = aLanguage;
    authKey = aAuthKey;

    client = ClientBuilder.newClient();
    client.register(EncodingFilter.class);
    client.register(GZipEncoder.class);
    client.register(DeflateEncoder.class);
    gsonBuilder = new GsonBuilder();

  }

  @Override
  public Set<ArteCrawlerUrlDto> call() throws Exception {
    gsonBuilder.registerTypeAdapter(getType(),
        new ArteSubcategoryListDeserializer(crawler, language));
    final Gson gson = gsonBuilder.create();
    final String subcategoriesUrl =
        String.format(SUBCATEGORIES_URL_PATTERN, language.getLanguageCode().toLowerCase());
    Builder request = client.target(subcategoriesUrl).request();
    request = request.header(HEADER_AUTHORIZATION, authKey);

    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();

    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, getType());
    } else {
      handleHttpError(subcategoriesUrl, response);
      return new HashSet<>();
    }
  }

  private Type getType() {
    return new TypeToken<Set<ArteCrawlerUrlDto>>() {}.getType();
  }

  protected void handleHttpError(final String aUrl, final Response aResponse) {
    crawler.printErrorMessage();
    LOG.fatal(String.format("A HTTP error %d occured when getting REST informations from: \"%s\".",
        aResponse.getStatus(), aUrl.toString()));
  }

}
