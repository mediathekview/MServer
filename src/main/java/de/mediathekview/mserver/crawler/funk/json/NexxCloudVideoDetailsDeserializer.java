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

      if (JsonUtils.hasElements(
              streamdata,
              Optional.of(crawler),
              ATTRIBUTE_CDN_SHIELD_PROG_HTTPS,
              ATTRIBUTE_AZURE_LOCATOR,
              ATTRIBUTE_AZURE_FILE_DISTRIBUTION)
          && JsonUtils.hasStringElements(general, Optional.of(crawler), ATTRIBUTE_ID)) {
        final String cdnShieldProgHTTP =
            streamdata.get(ATTRIBUTE_CDN_SHIELD_PROG_HTTPS).getAsString();
        final String azureLocator = streamdata.get(ATTRIBUTE_AZURE_LOCATOR).getAsString();
        final String id = general.get(ATTRIBUTE_ID).getAsString();

        final Set<NexxResolutionDTO> resolutions = gatherResolutions(streamdata);
        videoDetails.addAll(
            resolutions.parallelStream()
                .map(
                    resolution ->
                        buildFilmUrlInfoDto(cdnShieldProgHTTP, azureLocator, id, resolution))
                .collect(Collectors.toSet()));
      }
    }
    return videoDetails;
  }

  private FilmUrlInfoDto buildFilmUrlInfoDto(
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

  private Set<NexxResolutionDTO> gatherResolutions(final JsonObject streamdata) {
    final String[] azureFileDistributionSplitted =
        streamdata.get(ATTRIBUTE_AZURE_FILE_DISTRIBUTION).getAsString().split(SPLITERATOR_COMMA);
    return Arrays.stream(azureFileDistributionSplitted)
        .map(this::toResolution)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }

  private Optional<NexxResolutionDTO> toResolution(final String resolutionText) {
    final String[] resolutionTextSplitted = resolutionText.split(SPLITERATOR_COLON);
    if (resolutionTextSplitted.length == 2) {
      final int size = Integer.parseInt(resolutionTextSplitted[0]);
      final String[] reolutions = resolutionTextSplitted[1].split(SPLITERATOR_X);
      if (reolutions.length == 2) {
        final int width = Integer.parseInt(clearNumber(reolutions[0]));
        final int height = Integer.parseInt(clearNumber(reolutions[1]));
        return Optional.of(new NexxResolutionDTO(width, height, size));
      }
    }
    return Optional.empty();
  }

  private String clearNumber(final String number) {
    // Removes things like _AACAudio
    return number.replaceAll("_.*", "");
  }
}
