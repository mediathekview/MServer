package mServer.crawler.sender.newsearch;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A JSON deserializer to gather the needed information for a {@link VideoDTO}.
 */
public class ZDFVideoDTODeserializer implements JsonDeserializer<VideoDTO> {

    private static final String JSON_ELEMENT_BEGIN = "airtimeBegin";
    private static final String JSON_ELEMENT_BRAND = "http://zdf.de/rels/brand";
    private static final String JSON_ELEMENT_BROADCAST = "http://zdf.de/rels/cmdm/broadcasts";
    private static final String JSON_ELEMENT_DURATION = "duration";
    private static final String JSON_ELEMENT_MAINVIDEO  = "mainVideoContent";
    private static final String JSON_ELEMENT_PROGRAMMITEM = "programmeItem";
    private static final String JSON_ELEMENT_SHARING_URL = "http://zdf.de/rels/sharing-url";
    private static final String JSON_ELEMENT_SUBTITLE = "subtitle";
    private static final String JSON_ELEMENT_TARGET = "http://zdf.de/rels/target";
    private static final String JSON_ELEMENT_TITLE = "title";
    private static final String JSON_ELEMENT_TEASERTEXT = "teasertext";
    
    @Override
    public VideoDTO deserialize(final JsonElement aJsonElement, final Type aTypeOfT, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
        VideoDTO dto = new VideoDTO();

        JsonObject object = aJsonElement.getAsJsonObject();
        JsonObject brand = object.getAsJsonObject(JSON_ELEMENT_BRAND);
        if(brand != null) {
            JsonElement topic = brand.get(JSON_ELEMENT_TITLE);
            dto.setTopic(topic.getAsString());
        }       
        // nicht immer gesetzt!! muss ein anderer Weg sein!!!
        JsonElement teaserText = object.get(JSON_ELEMENT_TEASERTEXT);
        if(teaserText != null) {
            dto.setDescription(teaserText.getAsString());
        }

        JsonArray programmItem = object.getAsJsonArray(JSON_ELEMENT_PROGRAMMITEM);
        JsonObject programmItemTarget = null;
        
        if(programmItem != null) {
            if(programmItem.size() > 1) {
                throw new RuntimeException("Element does not contain programmitem" + object.getAsString());
            }
        
            if(programmItem.size() == 1) {
                programmItemTarget = programmItem.get(0).getAsJsonObject().get(JSON_ELEMENT_TARGET).getAsJsonObject();
            }
        }
        setTitle(dto, object, programmItemTarget);        
        
        JsonObject mainVideo = object.get(JSON_ELEMENT_MAINVIDEO).getAsJsonObject();
        JsonObject targetMainVideo = mainVideo.get(JSON_ELEMENT_TARGET).getAsJsonObject();
        int duration = targetMainVideo.get(JSON_ELEMENT_DURATION).getAsInt();
        dto.setDuration(duration);
        
        String websiteUrl = object.get(JSON_ELEMENT_SHARING_URL).getAsString();
        dto.setWebsiteUrl(websiteUrl);
        
        return dto;
    }
    
    private void setTitle(VideoDTO dto, JsonObject object, JsonObject target) {
        JsonElement titleElement = object.get(JSON_ELEMENT_TITLE);
        if(titleElement != null) {
            dto.setTitle(titleElement.getAsString());
        } else {
            String title = target.get(JSON_ELEMENT_TITLE).getAsString();
            String subTitle = target.get(JSON_ELEMENT_SUBTITLE).getAsString();

            if(subTitle.isEmpty()) {
                dto.setTitle(title);
            } else {
                dto.setTitle(title + " - " + subTitle);
            }       
        }
    }
    
     private String getAirtimeBegin(JsonObject programmItemTarget) {

        JsonArray broadcastArray = programmItemTarget.getAsJsonArray(JSON_ELEMENT_BROADCAST);
         
        if(broadcastArray.size() != 1) {
            throw new RuntimeException("Element does not contain broadcast" + programmItemTarget.getAsString());
        }

        String begin = broadcastArray.get(0).getAsJsonObject().get(JSON_ELEMENT_BEGIN).getAsString();
        return begin;
    } 
     
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");//2016-10-29T16:15:00+02:00
    private final SimpleDateFormat sdfOutTime = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat sdfOutDay = new SimpleDateFormat("dd.MM.yyyy");

    private String convertDate(String dateValue) {
        try {
            Date filmDate = sdf.parse(dateValue);
            return sdfOutDay.format(filmDate);
        } catch (ParseException ex) {
            throw new RuntimeException("Date parse exception: " + dateValue);
        }
    }

    private String convertTime(String dateValue) {
        try {
            Date filmDate = sdf.parse(dateValue);
            return sdfOutTime.format(filmDate);
        } catch (ParseException ex) {
            throw new RuntimeException("Date parse exception: " + dateValue);
        }
    }     
}
