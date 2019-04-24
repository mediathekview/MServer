package de.mediathekview.mserver.crawler.funk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.funk.json.NexxCloudSessionInitDeserializer;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class NexxCloudSessionInitiationTask implements Callable<Long> {
  private static final String FORM_FIELD_NXP_DEVH = "nxp_devh";
  private static final String FORM_AUTH_KEY = "4\"%\"3A1500496747\"%\"3A178989";

  private final AbstractCrawler crawler;

  public NexxCloudSessionInitiationTask(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Long call() {
    final Client client =
        ClientBuilder.newBuilder()
            .readTimeout(crawler.getCrawlerConfig().getSocketTimeoutInSeconds(), TimeUnit.SECONDS)
            .build();
    client.register(EncodingFilter.class);
    client.register(GZipEncoder.class);
    client.register(DeflateEncoder.class);
    final WebTarget target = client.target(FunkUrls.NEXX_CLOUD_SESSION_INIT.getAsString());

    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Long.class, new NexxCloudSessionInitDeserializer());
    final Gson gson = gsonBuilder.create();
    final Invocation.Builder request = target.request();

    final MultivaluedHashMap<String, String> formData = new MultivaluedStringMap();
    formData.add(FORM_FIELD_NXP_DEVH, FORM_AUTH_KEY);

    final Response response = request.post(Entity.form(formData));

    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, Long.class);
    } else {
      // TODO handle http errors
    }
    return null;
  }
}
