package de.mediathekview.mserver.crawler.funk.json;

import com.google.gson.*;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.FilmUrlInfoDto;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NexxCloudVideoDetailsDeserializer implements JsonDeserializer<Set<FilmUrlInfoDto>> {
  // https://[result.streamdata.cdnShieldProgHTTP]/[result.streamdata.azureLocator]/[result.general.ID]_src_[result.streamdata.azureFileDistribution].mp4
  private static final String VIDEO_FILE_URL_PATTERN = "https://%s%s/%s_src_%dx%d_%d.mp4";
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
  
  private final AbstractCrawler crawler;

  public NexxCloudVideoDetailsDeserializer(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Set<FilmUrlInfoDto> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    final Set<FilmUrlInfoDto> videoDetails = new HashSet<>();
    if (JsonUtils.checkTreePath(jsonElement, Optional.of(crawler), TAG_RESULT, TAG_STREAMDATA)
        && JsonUtils.checkTreePath(jsonElement, Optional.of(crawler), TAG_RESULT, TAG_GENERAL)) {
      final JsonObject result = jsonElement.getAsJsonObject().getAsJsonObject(TAG_RESULT);
      final JsonObject streamdata = result.getAsJsonObject(TAG_STREAMDATA);
      final JsonObject general = result.getAsJsonObject(TAG_GENERAL);
      //
      Optional<String> streamdata_cdn_type = JsonUtils.getAttributeAsString(streamdata, STREAMDATA_CDN_TYPE);
      //
      if (streamdata_cdn_type.isPresent() && streamdata_cdn_type.get().equalsIgnoreCase("azure")) {
        final String cdnShieldProgHTTP = streamdata.get(ATTRIBUTE_CDN_SHIELD_PROG_HTTPS).getAsString();
        if (JsonUtils.hasElements(
                streamdata,
                Optional.of(crawler),
                ATTRIBUTE_CDN_SHIELD_PROG_HTTPS,
                ATTRIBUTE_AZURE_LOCATOR,
                ATTRIBUTE_AZURE_FILE_DISTRIBUTION)
            && JsonUtils.hasStringElements(general, Optional.of(crawler), ATTRIBUTE_ID)) {
          
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
      } else if (streamdata_cdn_type.isPresent() && streamdata_cdn_type.get().equalsIgnoreCase("3q")) {
        //
        Optional<String> streamdata_account = JsonUtils.getAttributeAsString(streamdata, STREAMDATA_ACCOUNT);
        Optional<String> streamdata_prefix = JsonUtils.getAttributeAsString(streamdata, STREAMDATA_PREFIX);
        Optional<String> streamdata_locator = JsonUtils.getAttributeAsString(streamdata, STREAMDATA_LOCATOR);
        Optional<String> streamdata_ssh_host = JsonUtils.getAttributeAsString(streamdata, STREAMDATA_SSH_HOST);
        
        if (streamdata_account.isPresent() && 
            streamdata_prefix.isPresent() &&
            streamdata_locator.isPresent() &&
            streamdata_ssh_host.isPresent()) {
          final Set<NexxResolutionDTO> resolutions = gatherResolutions(streamdata);
          resolutions.forEach(aNexxResolutionDto -> {
            if (aNexxResolutionDto.getFileId().isPresent()) {
              videoDetails.add(buildFilmUrlInfoDto3q(
                  streamdata_ssh_host.get(),
                  streamdata_account.get(),
                  streamdata_prefix.get(),
                  streamdata_locator.get(),
                  aNexxResolutionDto.getFileId().get(),
                  aNexxResolutionDto.getWidht(),
                  aNexxResolutionDto.getHeight()
                  ));
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
        String.format(
            VIDEO_FILE_URL_PATTERN,
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
      final String cdnShieldProgHTTP,
      final String account,
      final String prefix,
      final String locator,
      final String fileId,
      final int width,
      final int height) {
    return new FilmUrlInfoDto(
        String.format(
            VIDEO_FILE_URL_PATTERN_3Q,
            cdnShieldProgHTTP,
            account,
            prefix,
            locator,
            fileId),
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
      final int size = Integer.parseInt(resolutionTextSplitted[0]);
      final String[] reolutions = resolutionTextSplitted[1].split(SPLITERATOR_X);
      if (reolutions.length == 2) {
        final int width = Integer.parseInt(clearNumber(reolutions[0]));
        final int height = Integer.parseInt(clearNumber(reolutions[1]));
        if (resolutionTextSplitted.length > 2) {
          fileId = Optional.of(resolutionTextSplitted[2]);
        }
        return Optional.of(new NexxResolutionDTO(width, height, size, fileId));
      }
    }
    return Optional.empty();
  }

  private String clearNumber(final String number) {
    // Removes things like _AACAudio
    return number.replaceAll("_.*", "");
  }
}
