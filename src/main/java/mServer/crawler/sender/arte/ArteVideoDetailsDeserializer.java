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
import mServer.tool.DateWithoutTimeComparer;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArteVideoDetailsDeserializer implements JsonDeserializer<ArteVideoDetailsDTO> {
 
    private static final String JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_1 = "programs";
    private static final String JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_2 = "broadcastProgrammings";
    private static final String JSON_ELEMENT_BROADCAST = "broadcastBeginRounded";
    private static final String JSON_ELEMENT_BROADCASTTYPE = "broadcastType";
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
            JsonArray broadcastArray = programElement.get(JSON_ELEMENT_BROADCAST_ELTERNKNOTEN_2).getAsJsonArray();

            if(broadcastArray.size() > 0) {
                detailsDTO.setBroadcastBegin(getBroadcastDate(broadcastArray));
            } else {
                // keine Ausstrahlungen verfügbar => catchupRightsBegin verwenden
                JsonElement elementBegin = programElement.get(JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_BEGIN);
                
                if(!elementBegin.isJsonNull()) {
                    detailsDTO.setBroadcastBegin(elementBegin.getAsString());
                }
            }
        }
            
        return detailsDTO;
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
                String value = this.getBroadcastDate(broadcastObject);

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
            broadcastDate = getFirstBroadcastDateIgnoringCatchupRights(broadcastArray);
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
    private String getBroadcastDate(JsonObject broadcastObject) {
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

                if(DateWithoutTimeComparer.compare(today, beginDate) >= 0 && DateWithoutTimeComparer.compare(today, endDate) <= 0) {
                    // wenn das heutige Datum zwischen begin und end liegt,
                    // dann ist es die aktuelle Ausstrahlung
                    broadcastDate = broadcastObject.get(JSON_ELEMENT_BROADCAST).getAsString();
                } else if(DateWithoutTimeComparer.compare(today, beginDate) < 0) {
                    // ansonsten die zukünftige verwenden
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
     * liefert das erste Ausstrahlungsdatum ohne Berücksichtigung der CatchupRights
     * @param broadcastArray
     * @return 
     */
    private static String getFirstBroadcastDateIgnoringCatchupRights(JsonArray broadcastArray) {
        String broadcastDate = "";
        
        for(int i = 0; i < broadcastArray.size(); i++) {
            JsonObject broadcastObject = broadcastArray.get(i).getAsJsonObject();
            
            if(broadcastObject.has(JSON_ELEMENT_BROADCASTTYPE) && 
                broadcastObject.has(JSON_ELEMENT_BROADCAST)) {
                String type = broadcastObject.get(JSON_ELEMENT_BROADCASTTYPE).getAsString();
                
                if(type.equals(BROADCASTTTYPE_FIRST)) {
                    broadcastDate = (broadcastObject.get(JSON_ELEMENT_BROADCAST).getAsString());
                }
            }
        }
        
        return broadcastDate;
    }
}
