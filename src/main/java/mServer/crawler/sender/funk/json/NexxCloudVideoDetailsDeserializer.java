package mServer.crawler.sender.funk.json;

import com.google.gson.*;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.base.FilmUrlInfoDto;
import mServer.crawler.sender.base.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NexxCloudVideoDetailsDeserializer implements JsonDeserializer<Set<FilmUrlInfoDto>> {
  // https://[result.streamdata.cdnShieldProgHTTP]/[result.streamdata.azureLocator]/[result.general.ID]_src_[result.streamdata.azureFileDistribution].mp4
  private static final String VIDEO_FILE_URL_PATTERN = "https://%s%s/%s_src_%dx%d_%s.mp4";
  // globalstatic+details["qAccount"]+"/files/"+details["qPrefix"]+"/"+details["qLocator"]+"/"+ss+".mp4"
  private static final String VIDEO_FILE_URL_PATTERN_3Q = "https://%s%s/files/%s/%s/%s.mp4";
  private static final String TAG_RESULT = "result";
  private static final String TAG_STREAMDATA = "streamdata";
  private static final String TAG_GENERAL = "general";
  private static final String ATTRIBUTE_CDN_SHIELD_PROG_HTTPS = "cdnShieldProgHTTPS";
  private static final String ATTRIBUTE_AZURE_LOCATOR = "azureLocator";
  private static final String ATTRIBUTE_AZURE_FILE_DISTRIBUTION = "azureFileDistribution";
  private static final String ATTRIBUTE_ID = "ID";
  private static final String SPLITERATOR_X = "x";
  private static final String SPLITERATOR_COMMA = ",";
  private static final String SPLITERATOR_COLON = ":";
  //
  private static final String STREAMDATA_ACCOUNT = "qAccount";
  private static final String STREAMDATA_PREFIX = "qPrefix";
  private static final String STREAMDATA_LOCATOR = "qLocator";
  private static final String STREAMDATA_CDN_TYPE = "cdnType";
  private static final String STREAMDATA_SSH_HOST = "cdnShieldHTTPS";
  private static final String DEFAULT_CDN = "funk-02.akamaized.net/";

  private static final Logger LOGGER =
          LogManager.getLogger(NexxCloudVideoDetailsDeserializer.class);

  @Override
  public Set<FilmUrlInfoDto> deserialize(
          final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
          throws JsonParseException {
    final Set<FilmUrlInfoDto> videoDetails = new HashSet<>();
    if (JsonUtils.checkTreePath(jsonElement, TAG_RESULT, TAG_STREAMDATA)
            && JsonUtils.checkTreePath(jsonElement, TAG_RESULT, TAG_GENERAL)) {
      final JsonObject result = jsonElement.getAsJsonObject().getAsJsonObject(TAG_RESULT);
      final JsonObject streamdata = result.getAsJsonObject(TAG_STREAMDATA);
      final JsonObject general = result.getAsJsonObject(TAG_GENERAL);

      final Optional<String> streamdataCdnType =
              JsonUtils.getAttributeAsString(streamdata, STREAMDATA_CDN_TYPE);

      if (streamdataCdnType.isPresent() && streamdataCdnType.get().equalsIgnoreCase("azure")) {
        final String cdnShieldProgHTTP =
                streamdata.get(ATTRIBUTE_CDN_SHIELD_PROG_HTTPS).getAsString();
        if (JsonUtils.hasElements(
                streamdata,
                ATTRIBUTE_CDN_SHIELD_PROG_HTTPS,
                ATTRIBUTE_AZURE_LOCATOR,
                ATTRIBUTE_AZURE_FILE_DISTRIBUTION)
                && JsonUtils.hasStringElements(general, ATTRIBUTE_ID)) {

          final String azureLocator = streamdata.get(ATTRIBUTE_AZURE_LOCATOR).getAsString();
          final String id = general.get(ATTRIBUTE_ID).getAsString();

          final Set<NexxResolutionDTO> resolutions = gatherResolutions(streamdata);
          videoDetails.addAll(
                  resolutions.parallelStream()
                          .map(
                                  resolution ->
                                          buildFilmUrlInfoDtoAzure(cdnShieldProgHTTP, azureLocator, id, resolution))
                          .collect(Collectors.toSet()));
        }
      } else if (streamdataCdnType.isPresent() && streamdataCdnType.get().equalsIgnoreCase("3q")) {
        //
        final Optional<String> streamdataAccount =
                JsonUtils.getAttributeAsString(streamdata, STREAMDATA_ACCOUNT);
        final Optional<String> streamdataPrefix =
                JsonUtils.getAttributeAsString(streamdata, STREAMDATA_PREFIX);
        final Optional<String> streamdataLocator =
                JsonUtils.getAttributeAsString(streamdata, STREAMDATA_LOCATOR);
        final Optional<String> streamdataSshHost =
                JsonUtils.getAttributeAsString(streamdata, STREAMDATA_SSH_HOST);

        if (streamdataAccount.isPresent()
                && streamdataPrefix.isPresent()
                && streamdataLocator.isPresent()
                && streamdataSshHost.isPresent()) {
          final Set<NexxResolutionDTO> resolutions = gatherResolutions(streamdata);
          resolutions.forEach(
                  aNexxResolutionDto -> {
                    if (aNexxResolutionDto.getFileId().isPresent()) {
                      videoDetails.add(
                              buildFilmUrlInfoDto3q(
                                      streamdataSshHost.get(),
                                      streamdataAccount.get(),
                                      streamdataPrefix.get(),
                                      streamdataLocator.get(),
                                      aNexxResolutionDto.getFileId().get(),
                                      aNexxResolutionDto.getWidht(),
                                      aNexxResolutionDto.getHeight()));
                    }
                  });
        }
      }
    }
    return videoDetails;
  }

  /*
   * Azure Service Storage
   */
  private FilmUrlInfoDto buildFilmUrlInfoDtoAzure(
          final String cdnShieldProgHTTP,
          final String azureLocator,
          final String id,
          final NexxResolutionDTO res) {
    return new FilmUrlInfoDto(
            
                    VIDEO_FILE_URL_PATTERN.formatted(
                            cdnShieldProgHTTP,
                            azureLocator,
                            id,
                            res.getWidht(),
                            res.getHeight(),
                            res.getSize()),
            res.getWidht(),
            res.getHeight());
  }

  /*
   * 3Q Service Storage
   */
  private FilmUrlInfoDto buildFilmUrlInfoDto3q(
          String cdnShieldProgHTTP,
          final String account,
          final String prefix,
          final String locator,
          final String fileId,
          final int width,
          final int height) {
    if (cdnShieldProgHTTP.isEmpty()) {
      cdnShieldProgHTTP = DEFAULT_CDN;
    }

    return new FilmUrlInfoDto(
            
                    VIDEO_FILE_URL_PATTERN_3Q.formatted(cdnShieldProgHTTP, account, prefix, locator, fileId),
            width,
            height);
  }

  private Set<NexxResolutionDTO> gatherResolutions(final JsonObject streamdata) {
    final String[] azureFileDistributionSplitted =
            streamdata.get(ATTRIBUTE_AZURE_FILE_DISTRIBUTION).getAsString().split(SPLITERATOR_COMMA);
    return Arrays.stream(azureFileDistributionSplitted)
            .map(this::toResolution)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());
  }

  /*
   * Sample
   * 0454:426x240:5-Vv2wLFYQ8j7C94mJBWdc,0895:640x360:4-RmQ9j4TqtzM8WbZKFkdn
   * >>
   * SIZE;WIDTH;HEIGTH;FILEID
   */
  private Optional<NexxResolutionDTO> toResolution(final String resolutionText) {
    final String[] resolutionTextSplitted = resolutionText.split(SPLITERATOR_COLON);
    Optional<String> fileId = Optional.empty();
    if (resolutionTextSplitted.length >= 2) {
      try {
        final String size = resolutionTextSplitted[0].replaceAll("^0+(?!$)", "");
        final String[] reolutions = resolutionTextSplitted[1].split(SPLITERATOR_X);
        if (reolutions.length == 2) {
          final int width = Integer.parseInt(clearNumber(reolutions[0]));
          final int height = Integer.parseInt(clearNumber(reolutions[1]));
          if (resolutionTextSplitted.length > 2) {
            fileId = Optional.of(resolutionTextSplitted[2]);
          }
          return Optional.of(new NexxResolutionDTO(width, height, size, fileId));
        }
      } catch (NumberFormatException e) {
        LOGGER.error(e);
        Log.errorLog(62345326, e);
      }
    }
    return Optional.empty();
  }

  private String clearNumber(final String number) {
    // Removes things like _AACAudio
    return number.replaceAll("_.*", "");
  }
}
