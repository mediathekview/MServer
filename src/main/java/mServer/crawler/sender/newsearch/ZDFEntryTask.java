package mServer.crawler.sender.newsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.tool.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.RecursiveTask;

/** Searches all information required for a film */
public class ZDFEntryTask extends RecursiveTask<VideoDTO> {

  private static final long serialVersionUID = 1L;
  public static final String TAG_PROFILE = "profile";
  public static final String PROFILE_REDIRECT = "http://zdf.de/rels/moved-permanently";
  public static final String TAG_LOCATION = "location";

  private final ZDFClient client;
  private final ZDFEntryDTO zdfEntryDTO;
  private final Gson gson;

  public ZDFEntryTask(
      ZDFEntryDTO aEntryDto,
      String aBaseUrl,
      String aApiBaseUrl,
      String aApiHost,
      ZDFConfigurationDTO aConfig) {
    this(aEntryDto, new ZDFClient(aBaseUrl, aApiBaseUrl, aApiHost, aConfig));
  }

  public ZDFEntryTask(ZDFEntryDTO aEntryDto, ZDFClient zdfClient) {
    client = zdfClient;
    zdfEntryDTO = aEntryDto;
    gson =
        new GsonBuilder()
            .registerTypeAdapter(VideoDTO.class, new ZDFVideoDTODeserializer())
            .registerTypeAdapter(DownloadDTO.class, new ZDFDownloadDTODeserializer())
            .create();
  }

  @Override
  protected VideoDTO compute() {

    if (zdfEntryDTO == null) {
      return null;
    }

    VideoDTO dto = null;

    if (!Config.getStop()) {
      try {
        // read film details
        String infoUrl = zdfEntryDTO.getEntryGeneralInformationUrl();
        JsonObject baseObjectInfo = client.execute(infoUrl);
        if (baseObjectInfo != null) {
          if (isRedirect(baseObjectInfo)) {
            baseObjectInfo = loadRedirect(baseObjectInfo, infoUrl);
          }
          dto = gson.fromJson(baseObjectInfo, VideoDTO.class);
          if (dto != null) {
            // read download details
            String downloadUrl = zdfEntryDTO.getEntryDownloadInformationUrl();
            JsonObject baseObjectDownload = client.execute(downloadUrl);
            if (baseObjectDownload != null) {
              DownloadDTO downloadDto = gson.fromJson(baseObjectDownload, DownloadDTO.class);
              dto.setDownloadDto(downloadDto);
            } else {
              // entry without download infos is not relevant
              dto = null;
            }
          }
        }
      } catch (Exception ex) {
        Log.errorLog(
            496583202,
            ex,
            "Exception parsing "
                + (zdfEntryDTO != null ? zdfEntryDTO.getEntryGeneralInformationUrl() : ""));
        dto = null;
      }
    }

    return dto;
  }

  private JsonObject loadRedirect(JsonObject baseObjectInfo, String oldUrl) {
    String newLocation = baseObjectInfo.get(TAG_LOCATION).getAsString();
    try {
      baseObjectInfo = client.execute(new URL(new URL(oldUrl), newLocation).toString());
    } catch (MalformedURLException malformedUrlException) {
      Log.errorLog(445648914, "Ein Weiterleitungslink ist fehlerhaft!");
    }

    return baseObjectInfo;
  }

  private boolean isRedirect(JsonObject baseObjectInfo) {
    return baseObjectInfo.has(TAG_PROFILE)
        && !baseObjectInfo.get(TAG_PROFILE).isJsonNull()
        && baseObjectInfo.get(TAG_PROFILE).getAsString().equals(PROFILE_REDIRECT)
        && baseObjectInfo.has(TAG_LOCATION)
        && !baseObjectInfo.get(TAG_LOCATION).isJsonNull();
  }
}
