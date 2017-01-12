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
    private static final String JSON_ELEMENT_LEADPARAGRAPH = "leadParagraph";
    private static final String JSON_ELEMENT_MAINVIDEO  = "mainVideoContent";
    private static final String JSON_ELEMENT_PROGRAMMITEM = "programmeItem";
    private static final String JSON_ELEMENT_SHARING_URL = "http://zdf.de/rels/sharing-url";
    private static final String JSON_ELEMENT_SUBTITLE = "subtitle";
    private static final String JSON_ELEMENT_TARGET = "http://zdf.de/rels/target";
    private static final String JSON_ELEMENT_TITLE = "title";
    private static final String JSON_ELEMENT_TEASERTEXT = "teasertext";

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");//2016-10-29T16:15:00+02:00
    private final SimpleDateFormat sdfOutTime = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat sdfOutDay = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public VideoDTO deserialize(final JsonElement aJsonElement, final Type aTypeOfT, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
        VideoDTO dto = new VideoDTO();

        JsonObject rootNode = aJsonElement.getAsJsonObject();
        JsonArray programmItem = rootNode.getAsJsonArray(JSON_ELEMENT_PROGRAMMITEM);
        JsonObject programmItemTarget = null;

        if(programmItem != null) {
            if(programmItem.size() > 1) {
                throw new RuntimeException("Element does not contain programmitem" + rootNode.getAsString());
            }

            if(programmItem.size() == 1) {
                programmItemTarget = programmItem.get(0).getAsJsonObject().get(JSON_ELEMENT_TARGET).getAsJsonObject();
            } 
        }

        parseTitle(dto, rootNode, programmItemTarget);     
        parseTopic(dto, rootNode);
        parseDescription(dto, rootNode);
        
        parseWebsiteUrl(dto, rootNode);
        parseAirtime(dto, programmItemTarget);
        parseDuration(dto, rootNode);
        
        return dto;    
    }
    
    private void parseAirtime(VideoDTO dto, JsonObject programmItemTarget) {
        if(programmItemTarget == null) {
            Log.sysLog("no programmItem entry found: " + dto.g)
            System.out.println("no programmItem entry");
            return;
        }
        
        JsonArray broadcastArray = programmItemTarget.getAsJsonArray(JSON_ELEMENT_BROADCAST);
        
        if(broadcastArray == null || broadcastArray.size() < 1) {
            System.out.println("no broadcast entry");
            return;
        }

        // array is ordered ascending though the newest broadcast is the last entry
        String begin = broadcastArray.get(broadcastArray.size() - 1).getAsJsonObject().get(JSON_ELEMENT_BEGIN).getAsString();
        if(!begin.isEmpty()) {
            dto.setDate(convertDate(begin));
            dto.setTime(convertTime(begin));
        }
    }

    private void parseWebsiteUrl(VideoDTO dto, JsonObject rootNode) {
        String websiteUrl = rootNode.get(JSON_ELEMENT_SHARING_URL).getAsString();
        dto.setWebsiteUrl(websiteUrl);
    }
    
    private void parseDuration(VideoDTO dto, JsonObject rootNode) {
        JsonObject mainVideo = rootNode.get(JSON_ELEMENT_MAINVIDEO).getAsJsonObject();
        JsonObject targetMainVideo = mainVideo.get(JSON_ELEMENT_TARGET).getAsJsonObject();
        JsonElement duration = targetMainVideo.get(JSON_ELEMENT_DURATION);
        if(duration != null) {
            dto.setDuration(duration.getAsInt());        
        }
    }
    
    private void parseDescription(VideoDTO dto, JsonObject rootNode) {
        JsonElement leadParagraph = rootNode.get(JSON_ELEMENT_LEADPARAGRAPH);
        if(leadParagraph != null) {
            dto.setDescription(leadParagraph.getAsString());
        } else {
            JsonElement teaserText = rootNode.get(JSON_ELEMENT_TEASERTEXT);
            if(teaserText != null) {
                dto.setDescription(teaserText.getAsString());
            }       
        }
    }
    
    private void parseTitle(VideoDTO dto, JsonObject rootNode, JsonObject target) {

        // use property "title" if found
        JsonElement titleElement = rootNode.get(JSON_ELEMENT_TITLE);
        if(titleElement != null) {
            JsonElement subTitleElement = rootNode.get(JSON_ELEMENT_SUBTITLE);
            if(subTitleElement != null) {
                dto.setTitle(titleElement.getAsString() + " - " + subTitleElement.getAsString());
            } else {
                dto.setTitle(titleElement.getAsString());
            }
        } else {
            // programmItem target required to determine title
            String title = target.get(JSON_ELEMENT_TITLE).getAsString();
            String subTitle = target.get(JSON_ELEMENT_SUBTITLE).getAsString();

            if(subTitle.isEmpty()) {
                dto.setTitle(title);
            } else {
                dto.setTitle(title + " - " + subTitle);
            }       
        }
    }
    
    private void parseTopic(VideoDTO dto, JsonObject rootNode) {
        JsonObject brand = rootNode.getAsJsonObject(JSON_ELEMENT_BRAND);
        if(brand != null) {
            JsonElement topic = brand.get(JSON_ELEMENT_TITLE);
            if(topic != null) {
                dto.setTopic(topic.getAsString());
            }
        }          
        
        // if no topic found, set topic to title
        if(dto.getTopic() == null) {
            dto.setTopic(dto.getTitle());
        }
    }
     
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
