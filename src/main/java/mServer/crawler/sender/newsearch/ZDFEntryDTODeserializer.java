package mServer.crawler.sender.newsearch;

import com.google.gson.*;
import de.mediathekview.mlib.tool.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;

/** A JSON deserializer to gather the needed information for a {@link ZDFEntryDTO}. */
public class ZDFEntryDTODeserializer implements JsonDeserializer<ZDFEntryDTO> {

  private static final String PROFILE_REGEX_TEMPLATE = "\\?profile=.*";
  private static final Logger LOG = LogManager.getLogger(ZDFEntryDTODeserializer.class);
  private static final String JSON_ELEMENT_DOWNLOAD_INFORMATION_URL =
      "http://zdf.de/rels/streams/ptmd-template";
  private static final String JSON_OBJ_ELEMENT_TARGET = "http://zdf.de/rels/target";
  private static final String JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT = "mainVideoContent";
  private static final String JSON_ELEMENT_GENERAL_INFORMATION_URL = "self";
  private static final String JSON_OBJ_VIDEO_PAGE_TEASER =
      "http://zdf.de/rels/content/video-page-teaser";
  private static final String PLACEHOLDER_PLAYER_ID = "{playerId}";
  private static final String PLAYER_ID = "ngplayer_2_3";
  public static final String JSON_OBJ_ELEMENT_CONF_SECTION =
      "http://zdf.de/rels/content/conf-section";
  public static final String JSON_OBJ_ELEMENT_HOMETVSERVICE = "homeTvService";
  public static final String JSON_ELEMENT_TVSERVICEID = "tvServiceId";
  private final String apiBaseUrl;

  public ZDFEntryDTODeserializer(String aApiBaseUrl) {
    apiBaseUrl = aApiBaseUrl;
  }

  @Override
  public ZDFEntryDTO deserialize(
      final JsonElement aJsonElement,
      final Type aTypeOfT,
      final JsonDeserializationContext aJsonDeserializationContext)
      throws JsonParseException {
    ZDFEntryDTO dto = null;
    try {
      JsonObject targetObject =
          aJsonElement.getAsJsonObject().getAsJsonObject(JSON_OBJ_ELEMENT_TARGET);
      if (null == targetObject) {
        LOG.error("Can't find an JSON Target Object Element for Entry.");
        LOG.debug("Entry: " + aJsonElement.toString());
      } else {
        JsonObject mainVideoContentObject;
        if (!targetObject.has(JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT)
            && targetObject.has(JSON_OBJ_VIDEO_PAGE_TEASER)
            && targetObject.has(JSON_OBJ_ELEMENT_CONF_SECTION)
            && targetObject
                .getAsJsonObject(JSON_OBJ_VIDEO_PAGE_TEASER)
                .has(JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT)) {
          targetObject = targetObject.getAsJsonObject(JSON_OBJ_VIDEO_PAGE_TEASER);
        }

        mainVideoContentObject = targetObject.getAsJsonObject(JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT);
        JsonObject confSectionObject = targetObject.getAsJsonObject(JSON_OBJ_ELEMENT_CONF_SECTION);
        if (mainVideoContentObject != null) {
          dto = buildZDFEntryDTO(targetObject, mainVideoContentObject, confSectionObject);
        }
      }
    } catch (final Exception ex) {
      Log.errorLog(496583255, ex);
      LOG.debug("Entry: " + aJsonElement.toString());
    }

    return dto;
  }

  private ZDFEntryDTO buildZDFEntryDTO(
      final JsonObject aTargetObject,
      final JsonObject aMainVideoContentObject,
      JsonObject confSectionObject)
      throws NoDownloadInformationException {
    final JsonObject elementTargetObject =
        aMainVideoContentObject.getAsJsonObject(JSON_OBJ_ELEMENT_TARGET);
    final JsonElement entryGeneralInformationUrlElement =
        aTargetObject.get(JSON_ELEMENT_GENERAL_INFORMATION_URL);

    if (elementTargetObject.has(JSON_ELEMENT_DOWNLOAD_INFORMATION_URL)) {
      String tvService;
      if (confSectionObject != null
          && confSectionObject.has(JSON_OBJ_ELEMENT_HOMETVSERVICE)
          && confSectionObject
              .getAsJsonObject(JSON_OBJ_ELEMENT_HOMETVSERVICE)
              .has(JSON_ELEMENT_TVSERVICEID)) {
        tvService =
            confSectionObject
                .getAsJsonObject(JSON_OBJ_ELEMENT_HOMETVSERVICE)
                .get(JSON_ELEMENT_TVSERVICEID)
                .getAsString();
      } else {
        tvService = "";
      }

      final JsonElement entryDownloadInformationUrlElement =
          elementTargetObject.get(JSON_ELEMENT_DOWNLOAD_INFORMATION_URL);

      final String downloadUrl =
          entryDownloadInformationUrlElement
              .getAsString()
              .replace(PLACEHOLDER_PLAYER_ID, PLAYER_ID);

      return new ZDFEntryDTO(
          apiBaseUrl,
          entryGeneralInformationUrlElement
              .getAsString()
              .replaceAll(PROFILE_REGEX_TEMPLATE, "")
              .trim(),
          downloadUrl.trim(),
          tvService);
    } else {
      throw new NoDownloadInformationException();
    }
  }
}
