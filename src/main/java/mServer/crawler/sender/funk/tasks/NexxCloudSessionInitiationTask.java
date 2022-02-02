package mServer.crawler.sender.funk.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.funk.FunkUrls;
import mServer.crawler.sender.funk.json.NexxCloudSessionInitDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class NexxCloudSessionInitiationTask implements Callable<Long> {
  private static final Logger LOG = LogManager.getLogger(NexxCloudSessionInitiationTask.class);
  private static final String FORM_FIELD_NXP_DEVH = "nxp_devh";
  private static final String FORM_AUTH_KEY = "4\"%\"3A1500496747\"%\"3A178989";

  private final MediathekReader crawler;

  public NexxCloudSessionInitiationTask(final MediathekReader aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Long call() {
    final Gson gson = createGson();

    final Client client = createClient();
    final WebTarget target =
            client.target(FunkUrls.NEXX_CLOUD_SESSION_INIT.getAsString());

    final MultivaluedHashMap<String, String> formData = createForm();

    final Response response = target.request().post(Entity.form(formData));

    if (response.getStatus() == 201) {
      final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, Long.class);
    } else {
      LOG.fatal(
              "A HTTP error {} occurred when initialising the Nexx cloud session.",
              response.getStatus());
    }
    return null;
  }

  @NotNull
  private MultivaluedHashMap<String, String> createForm() {
    final MultivaluedHashMap<String, String> formData = new MultivaluedStringMap();
    formData.add(FORM_FIELD_NXP_DEVH, FORM_AUTH_KEY);
    return formData;
  }

  @NotNull
  private Gson createGson() {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Long.class, new NexxCloudSessionInitDeserializer());
    return gsonBuilder.create();
  }

  @NotNull
  private Client createClient() {
    final Client client =
            ClientBuilder.newBuilder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();
    client.register(EncodingFilter.class);
    client.register(GZipEncoder.class);
    client.register(DeflateEncoder.class);
    return client;
  }
}
