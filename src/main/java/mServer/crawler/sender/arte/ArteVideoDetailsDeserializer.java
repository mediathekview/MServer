package mServer.crawler.sender.arte;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Calendar;
import mServer.crawler.sender.newsearch.GeoLocations;
import mServer.tool.DateWithoutTimeComparer;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArteVideoDetailsDeserializer implements JsonDeserializer<ArteVideoDetailsDTO> {

    private static final String JSON_ELEMENT_KEY_CATEGORY = "category";
    private static final String JSON_ELEMENT_KEY_SUBCATEGORY = "subcategory";
    private static final String JSON_ELEMENT_KEY_NAME = "name";
    private static final String JSON_ELEMENT_KEY_TITLE = "title";
    private static final String JSON_ELEMENT_KEY_SUBTITLE = "subtitle";
    private static final String JSON_ELEMENT_KEY_URL = "url";
    private static final String JSON_ELEMENT_KEY_PROGRAM_ID = "programId";
    private static final String JSON_ELEMENT_KEY_SHORT_DESCRIPTION = "shortDescription";
    
    private static final String JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_1 = "programs";
    private static final String JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_2 = "broadcastProgrammings";
    private static final String JSON_ELEMENT_BROADCAST = "broadcastBeginRounded";
    private static final String JSON_ELEMENT_BROADCASTTYPE = "broadcastType";
    private static final String JSON_ELEMENT_BROADCAST_VIDEORIGHTS_BEGIN = "videoRightsBegin";
    private static final String JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_BEGIN = "catchupRightsBegin";
    private static final String JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_END = "catchupRightsEnd";
    private static final String BROADCASTTTYPE_FIRST = "FIRST_BROADCAST";
    private static final String BROADCASTTTYPE_MINOR_RE = "MINOR_REBROADCAST";
    private static final String BROADCASTTTYPE_MAJOR_RE = "MAJOR_REBROADCAST";
    
    private static final Logger LOG = LogManager.getLogger(ArteVideoDeserializer.class);

    private final FastDateFormat broadcastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssX");//2016-10-29T16:15:00Z

    private final Calendar today;

    public ArteVideoDetailsDeserializer(Calendar aToday) {
        today = aToday;
    }
    
    @Override
    public ArteVideoDetailsDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
        ArteVideoDetailsDTO detailsDTO = new ArteVideoDetailsDTO();
        
        if(aJsonElement.isJsonObject() && 
            aJsonElement.getAsJsonObject().get(JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_1).getAsJsonArray().size() > 0) {
                
            JsonObject programElement = aJsonElement.getAsJsonObject()
                .get(JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_1).getAsJsonArray().get(0).getAsJsonObject();

            String titel = getTitle(programElement);
            String thema = getSubject(programElement);

            String beschreibung = getElementValue(programElement, JSON_ELEMENT_KEY_SHORT_DESCRIPTION);

            String urlWeb = getElementValue(programElement, JSON_ELEMENT_KEY_URL);
               detailsDTO.setDescription(beschreibung);
               detailsDTO.setTheme(thema);
               detailsDTO.setTitle(titel);
               detailsDTO.setWebsite(urlWeb);

            JsonArray broadcastArray = programElement.get(JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_2).getAsJsonArray();

            if(broadcastArray.size() > 0) {
                detailsDTO.setBroadcastBegin(getBroadcastDate(broadcastArray));
            } else {
                // keine Ausstrahlungen verfügbar => catchupRightsBegin verwenden
                // wenn es die auch nicht gibt => videoRightsBegin verwenden
                String begin = getElementValue(programElement, JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_BEGIN);
                if(begin.isEmpty()) {
                    begin = getElementValue(programElement, JSON_ELEMENT_BROADCAST_VIDEORIGHTS_BEGIN);
                }
                detailsDTO.setBroadcastBegin(begin);
            }
            
            detailsDTO.setGeoLocation(getGeoLocation(programElement));
        }
            
        return detailsDTO;
    }

    private static String getSubject(JsonObject programObject) {
        String category = "";
        String subcategory = "";
        String subject;
        
        JsonElement catElement = programObject.get(JSON_ELEMENT_KEY_CATEGORY);
        if(!catElement.isJsonNull()) {
            JsonObject catObject = catElement.getAsJsonObject();
            category = catObject != null ? getElementValue(catObject, JSON_ELEMENT_KEY_NAME) : "";
        }
        
        JsonElement subcatElement = programObject.get(JSON_ELEMENT_KEY_SUBCATEGORY);
        if(!subcatElement.isJsonNull()) {
            JsonObject subcatObject = subcatElement.getAsJsonObject();
            subcategory = subcatObject != null ? getElementValue(subcatObject, JSON_ELEMENT_KEY_NAME) : "";
        }
       
        if(!category.equals(subcategory) && !subcategory.isEmpty()) {
            subject = category + " - " + subcategory;
        } else {
            subject = category;
        }

        return subject;
    }

    private static String getTitle(JsonObject programObject) {
        String title = getElementValue(programObject, JSON_ELEMENT_KEY_TITLE);
        String subtitle = getElementValue(programObject, JSON_ELEMENT_KEY_SUBTITLE);
                
        if (!title.equals(subtitle) && !subtitle.isEmpty()) {
            title = title + " - " + subtitle;
        }        
        
        return title;
    }

    private static boolean isValidProgramObject(JsonObject programObject) {
        return programObject.has(JSON_ELEMENT_KEY_TITLE) && 
            programObject.has(JSON_ELEMENT_KEY_PROGRAM_ID) && 
            programObject.has(JSON_ELEMENT_KEY_URL) &&
            !programObject.get(JSON_ELEMENT_KEY_TITLE).isJsonNull() &&
            !programObject.get(JSON_ELEMENT_KEY_PROGRAM_ID).isJsonNull() &&
            !programObject.get(JSON_ELEMENT_KEY_URL).isJsonNull();
    }
    
    private static String getElementValue(JsonObject jsonObject, String elementName) {
        return !jsonObject.get(elementName).isJsonNull() ? jsonObject.get(elementName).getAsString() : "";        
    }
    
    private GeoLocations getGeoLocation(JsonObject programElement) {
        GeoLocations geo = GeoLocations.GEO_NONE;
        
        if(programElement.has("geoblocking")) {
            JsonElement geoElement = programElement.get("geoblocking");
            if(!geoElement.isJsonNull()) {
                JsonObject geoObject = geoElement.getAsJsonObject();
                if(!geoObject.isJsonNull() && geoObject.has("code")) {
                    String code = geoObject.get("code").getAsString();
                    switch(code) {
                        case "DE_FR":
                        case "EUR_DE_FR":
                            geo = GeoLocations.GEO_DE_FR;
                            break;
                        case "SAT":
                            geo = GeoLocations.GEO_DE_AT_CH_EU;
                            break;
                        case "ALL":
                            geo = GeoLocations.GEO_NONE;
                            break;
                        default:
                            LOG.debug("New ARTE GeoLocation: " + code);
                    }
                }
            }
        }
        
        return geo;
    }
    
    /**
     * ermittelt Ausstrahlungsdatum aus der Liste der Ausstrahlungen
     * @param broadcastArray
     * @return 
     */
    private String getBroadcastDate(JsonArray broadcastArray) {
        String broadcastDate = "";
        String broadcastBeginFirst = "";
        String broadcastBeginMajor = "";
        String broadcastBeginMinor = "";

        // nach Priorität der BroadcastTypen den relevanten Eintrag suchen
        // FIRST_BROADCAST => MAJOR_REBROADCAST => MINOR_REBROADCAST
        // dabei die "aktuellste" Ausstrahlung verwenden
        for(int i = 0; i < broadcastArray.size(); i++) {
            JsonObject broadcastObject = broadcastArray.get(i).getAsJsonObject();

            if(broadcastObject.has(JSON_ELEMENT_BROADCASTTYPE) && 
                    broadcastObject.has(JSON_ELEMENT_BROADCAST)) {
                String value = this.getBroadcastDateConsideringCatchupRights(broadcastObject);

                if(!value.isEmpty()) {
                    String type = broadcastObject.get(JSON_ELEMENT_BROADCASTTYPE).getAsString();
                    switch(type) {
                        case BROADCASTTTYPE_FIRST:
                            broadcastBeginFirst = value;
                            break;
                        case BROADCASTTTYPE_MAJOR_RE:
                            broadcastBeginMajor = value;
                            break;
                        case BROADCASTTTYPE_MINOR_RE:
                            broadcastBeginMinor = value;
                            break;
                        default:
                            LOG.debug("New broadcasttype: " + type);
                    }
                }
            }
        }

        if(!broadcastBeginFirst.isEmpty()) {
            broadcastDate = broadcastBeginFirst;
        } else if(!broadcastBeginMajor.isEmpty()) {
            broadcastDate = broadcastBeginMajor;
        } else if(!broadcastBeginMinor.isEmpty()) {
            broadcastDate = broadcastBeginMinor;
        }

        // wenn kein Ausstrahlungsdatum vorhanden, dann die erste Ausstrahlung nehmen
        // egal, wann die CatchupRights liegen, damit ein "sinnvolles" Datum vorhanden ist
        if(broadcastDate.isEmpty()) {
            broadcastDate = getBroadcastDateIgnoringCatchupRights(broadcastArray, BROADCASTTTYPE_FIRST);
        }        
        // wenn immer noch leer, dann die Major-Ausstrahlung verwenden
        if(broadcastDate.isEmpty()) {
            broadcastDate = getBroadcastDateIgnoringCatchupRights(broadcastArray, BROADCASTTTYPE_MAJOR_RE);
        }        
        
        return broadcastDate;
    }
    
    /**
     * Liefert den Beginn der Ausstrahlung, 
     * wenn 
     *  - heute im Zeitraum von CatchUpRights liegt 
     *  - oder heute vor dem Zeitraum liegt
     *  - oder CatchUpRights nicht gesetzt ist
     * @param broadcastObject 
     * @return der Beginn der Ausstrahlung oder ""
     */
    private String getBroadcastDateConsideringCatchupRights(JsonObject broadcastObject) {
        String broadcastDate = "";
        
        JsonElement elementBegin = broadcastObject.get(JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_BEGIN);
        JsonElement elementEnd = broadcastObject.get(JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_END);
        
        if (!elementBegin.isJsonNull() && !elementEnd.isJsonNull()) {
            String begin = elementBegin.getAsString();
            String end = elementEnd.getAsString();

            try {
                Calendar beginDate = Calendar.getInstance();
                beginDate.setTime(broadcastDateFormat.parse(begin));
                Calendar endDate = Calendar.getInstance();
                endDate.setTime(broadcastDateFormat.parse(end));

                if((DateWithoutTimeComparer.compare(today, beginDate) >= 0 && DateWithoutTimeComparer.compare(today, endDate) <= 0)
                    || (DateWithoutTimeComparer.compare(today, beginDate) < 0)) {
                    // wenn das heutige Datum zwischen begin und end liegt,
                    // dann ist es die aktuelle Ausstrahlung
                    broadcastDate = broadcastObject.get(JSON_ELEMENT_BROADCAST).getAsString();
                }
                
            } catch (ParseException ex) {
                LOG.debug(ex);
            }           
        } else {
            String broadcast = broadcastObject.get(JSON_ELEMENT_BROADCAST).getAsString();
            
            try {
                Calendar broadcastCal = Calendar.getInstance();
                broadcastCal.setTime(broadcastDateFormat.parse(broadcast));
                broadcastDate = broadcast;
                
            } catch (ParseException ex) {
                LOG.debug(ex);
            }            
        }
        return broadcastDate;
    }    
    
    /***
     * liefert die erste Ausstrahlung des Typs ohne Berücksichtigung der CatchupRights
     */
    private static String getBroadcastDateIgnoringCatchupRights(JsonArray broadcastArray, String broadcastType) {
        String broadcastDate = "";
        
        for(int i = 0; i < broadcastArray.size(); i++) {
            JsonObject broadcastObject = broadcastArray.get(i).getAsJsonObject();
            
            if(broadcastObject.has(JSON_ELEMENT_BROADCASTTYPE) && 
                broadcastObject.has(JSON_ELEMENT_BROADCAST)) {
                String type = broadcastObject.get(JSON_ELEMENT_BROADCASTTYPE).getAsString();
                
                if(type.equals(broadcastType)) {
                    if (!broadcastObject.get(JSON_ELEMENT_BROADCAST).isJsonNull()) {
                        broadcastDate = (broadcastObject.get(JSON_ELEMENT_BROADCAST).getAsString());
                    }
                }
            }
        }
        
        return broadcastDate;
    }
}
