package mServer.crawler.sender.newsearch;

import java.lang.reflect.Type;
import java.text.ParseException;

import org.apache.commons.lang3.time.FastDateFormat;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.mediathekview.mlib.tool.Log;
import mServer.tool.MserverDaten;

/**
 * A JSON deserializer to gather the needed information for a {@link VideoDTO}.
 */
public class ZDFVideoDTODeserializer implements JsonDeserializer<VideoDTO>
{

    private static final String JSON_ELEMENT_BEGIN = "airtimeBegin";
    private static final String JSON_ELEMENT_BRAND = "http://zdf.de/rels/brand";
    private static final String JSON_ELEMENT_CATEGORY = "http://zdf.de/rels/category";
    private static final String JSON_ELEMENT_BROADCAST = "http://zdf.de/rels/cmdm/broadcasts";
    private static final String JSON_ELEMENT_DURATION = "duration";
    private static final String JSON_ELEMENT_EDITORIALDATE = "editorialDate";
    private static final String JSON_ELEMENT_LEADPARAGRAPH = "leadParagraph";
    private static final String JSON_ELEMENT_MAINVIDEO = "mainVideoContent";
    private static final String JSON_ELEMENT_PROGRAMMITEM = "programmeItem";
    private static final String JSON_ELEMENT_SHARING_URL = "http://zdf.de/rels/sharing-url";
    private static final String JSON_ELEMENT_SUBTITLE = "subtitle";
    private static final String JSON_ELEMENT_TARGET = "http://zdf.de/rels/target";
    private static final String JSON_ELEMENT_TITLE = "title";
    private static final String JSON_ELEMENT_TEASERTEXT = "teasertext";

    private final FastDateFormat sdfEditorialDate = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");//2016-10-29T16:15:00.000+02:00
    private final FastDateFormat sdfAirtimeBegin = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssXXX");//2016-10-29T16:15:00+02:00
    private final FastDateFormat sdfOutTime = FastDateFormat.getInstance("HH:mm:ss");
    private final FastDateFormat sdfOutDay = FastDateFormat.getInstance("dd.MM.yyyy");

    @Override
    public VideoDTO deserialize(final JsonElement aJsonElement, final Type aTypeOfT, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException
    {
        VideoDTO dto = null;
        try
        {
            dto = new VideoDTO();

            JsonObject rootNode = aJsonElement.getAsJsonObject();
            JsonObject programmItemTarget = null;

            if (rootNode.has(JSON_ELEMENT_PROGRAMMITEM) && !rootNode.get(JSON_ELEMENT_PROGRAMMITEM).isJsonNull())
            {
                JsonArray programmItem = rootNode.getAsJsonArray(JSON_ELEMENT_PROGRAMMITEM);
                programmItemTarget = programmItem.get(0).getAsJsonObject().get(JSON_ELEMENT_TARGET).getAsJsonObject();
            }

            parseTitle(dto, rootNode, programmItemTarget);
            parseTopic(dto, rootNode);
            parseDescription(dto, rootNode);

            parseWebsiteUrl(dto, rootNode);
            parseAirtime(dto, rootNode, programmItemTarget);
            parseDuration(dto, rootNode);

        } catch (UnsupportedOperationException ex)
        {
            if (MserverDaten.debug)
                Log.errorLog(496583256, ex);
        } catch (Exception ex)
        {
            dto = null;
            Log.errorLog(496583256, ex);
        }

        return dto;
    }

    private void parseAirtime(VideoDTO dto, JsonObject rootNode, JsonObject programmItemTarget)
    {
        String date;
        FastDateFormat sdf;

        // use broadcast airtime if found
        if (programmItemTarget != null)
        {
            JsonArray broadcastArray = programmItemTarget.getAsJsonArray(JSON_ELEMENT_BROADCAST);

            if (broadcastArray == null || broadcastArray.size() < 1)
            {
                date = getEditorialDate(rootNode);
                sdf = sdfEditorialDate;
            } else
            {
                // array is ordered ascending though the oldest broadcast is the first entry
                date = broadcastArray.get(0).getAsJsonObject().get(JSON_ELEMENT_BEGIN).getAsString();
                sdf = sdfAirtimeBegin;
            }
        } else
        {
            // use editorialdate
            date = getEditorialDate(rootNode);
            sdf = sdfEditorialDate;
        }
        if (!date.isEmpty())
        {
            dto.setDate(convertDate(date, sdf));
            dto.setTime(convertTime(date, sdf));
        }
    }

    private String getEditorialDate(JsonObject rootNode)
    {
        return rootNode.get(JSON_ELEMENT_EDITORIALDATE).getAsString();
    }

    private void parseWebsiteUrl(VideoDTO dto, JsonObject rootNode)
    {
        String websiteUrl = rootNode.get(JSON_ELEMENT_SHARING_URL).getAsString();
        dto.setWebsiteUrl(websiteUrl);
    }

    private void parseDuration(VideoDTO dto, JsonObject rootNode)
    {
        JsonElement mainVideoElement = rootNode.get(JSON_ELEMENT_MAINVIDEO);
        if (mainVideoElement != null)
        {
            JsonObject mainVideo = mainVideoElement.getAsJsonObject();
            JsonObject targetMainVideo = mainVideo.get(JSON_ELEMENT_TARGET).getAsJsonObject();
            JsonElement duration = targetMainVideo.get(JSON_ELEMENT_DURATION);
            if (duration != null)
            {
                dto.setDuration(duration.getAsInt());
            }
        }
    }

    private void parseDescription(VideoDTO dto, JsonObject rootNode)
    {
        JsonElement leadParagraph = rootNode.get(JSON_ELEMENT_LEADPARAGRAPH);
        if (leadParagraph != null)
        {
            dto.setDescription(leadParagraph.getAsString());
        } else
        {
            JsonElement teaserText = rootNode.get(JSON_ELEMENT_TEASERTEXT);
            if (teaserText != null)
            {
                dto.setDescription(teaserText.getAsString());
            }
        }
    }

    private void parseTitle(VideoDTO dto, JsonObject rootNode, JsonObject target)
    {

        // use property "title" if found
        JsonElement titleElement = rootNode.get(JSON_ELEMENT_TITLE);
        if (titleElement != null)
        {
            JsonElement subTitleElement = rootNode.get(JSON_ELEMENT_SUBTITLE);
            if (subTitleElement != null)
            {
                dto.setTitle(titleElement.getAsString() + " - " + subTitleElement.getAsString());
            } else
            {
                dto.setTitle(titleElement.getAsString());
            }
        } else
        {
            // programmItem target required to determine title
            String title = target.get(JSON_ELEMENT_TITLE).getAsString();
            String subTitle = target.get(JSON_ELEMENT_SUBTITLE).getAsString();

            if (subTitle.isEmpty())
            {
                dto.setTitle(title);
            } else
            {
                dto.setTitle(title + " - " + subTitle);
            }
        }
    }

    private void parseTopic(VideoDTO dto, JsonObject rootNode)
    {
        JsonObject brand = rootNode.getAsJsonObject(JSON_ELEMENT_BRAND);
        JsonObject category = rootNode.getAsJsonObject(JSON_ELEMENT_CATEGORY);

        if (brand != null)
        {
            // first use brand
            JsonElement topic = brand.get(JSON_ELEMENT_TITLE);
            if (topic != null)
            {
                dto.setTopic(topic.getAsString());
                return;
            }
        }

        if (category != null)
        {
            // second use category
            JsonElement topic = category.get(JSON_ELEMENT_TITLE);
            if (topic != null)
            {
                dto.setTopic(topic.getAsString());
                return;
            }
        }

        // if no topic found, set topic to title
        dto.setTopic(dto.getTitle());
    }

    private String convertDate(String dateValue, FastDateFormat sdf)
    {
        try
        {
            return sdfOutDay.format(sdf.parse(dateValue));
        } catch (ParseException ex)
        {
            throw new RuntimeException("Date parse exception: " + dateValue);
        }
    }

    private String convertTime(String dateValue, FastDateFormat sdf)
    {
        try
        {
            return sdfOutTime.format(sdf.parse(dateValue));
        } catch (ParseException ex)
        {
            throw new RuntimeException("Date parse exception: " + dateValue);
        }
    }
}
