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
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import de.mediathekview.mlib.daten.GeoLocations;

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

    private final DateTimeFormatter broadcastDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");//2016-10-29T16:15:00Z


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
                    try {
                        detailsDTO.setBroadcastBegin(LocalDateTime.parse(elementBegin.getAsString(),broadcastDateFormat));
                    }catch(DateTimeParseException dateTimeParseException)
                    {
                        LOG.error("Can't parse a broadcast relevant date.",dateTimeParseException);
                        detailsDTO.setBroadcastBegin(LocalDateTime.now());
                    }

                }
            }
            
            detailsDTO.setGeoLocation(getGeoLocation(programElement));
        }
            
        return detailsDTO;
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
    private LocalDateTime getBroadcastDate(JsonArray broadcastArray) {
        LocalDateTime broadcastDate = null;
        LocalDateTime broadcastBeginFirst = null;
        LocalDateTime broadcastBeginMajor = null;
        LocalDateTime broadcastBeginMinor = null;

        // nach Priorität der BroadcastTypen den relevanten Eintrag suchen
        // FIRST_BROADCAST => MAJOR_REBROADCAST => MINOR_REBROADCAST
        // dabei die "aktuellste" Ausstrahlung verwenden
        for(int i = 0; i < broadcastArray.size(); i++) {
            JsonObject broadcastObject = broadcastArray.get(i).getAsJsonObject();

            if(broadcastObject.has(JSON_ELEMENT_BROADCASTTYPE) && 
                    broadcastObject.has(JSON_ELEMENT_BROADCAST)) {
                LocalDateTime value = this.getBroadcastDateConsideringCatchupRights(broadcastObject);

                if(value != null) {
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

        if(broadcastBeginFirst != null) {
            broadcastDate = broadcastBeginFirst;
        } else if(broadcastBeginMajor != null) {
            broadcastDate = broadcastBeginMajor;
        } else if(broadcastBeginMinor != null) {
            broadcastDate = broadcastBeginMinor;
        }

        // wenn kein Ausstrahlungsdatum vorhanden, dann die erste Ausstrahlung nehmen
        // egal, wann die CatchupRights liegen, damit ein "sinnvolles" Datum vorhanden ist
        if(broadcastDate == null) {
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
    private LocalDateTime getBroadcastDateConsideringCatchupRights(JsonObject broadcastObject) {
        LocalDateTime broadcastDate = LocalDateTime.now();
        try {
            JsonElement elementBegin = broadcastObject.get(JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_BEGIN);
            JsonElement elementEnd = broadcastObject.get(JSON_ELEMENT_BROADCAST_CATCHUPRIGHTS_END);
            if (!elementBegin.isJsonNull() && !elementEnd.isJsonNull()) {
                String begin = elementBegin.getAsString();
                String end = elementEnd.getAsString();
    
                LocalDateTime beginDate = LocalDateTime.parse(begin, broadcastDateFormat);
                LocalDateTime endDate = LocalDateTime.parse(end, broadcastDateFormat);
    
                if((LocalDate.now().compareTo(beginDate.toLocalDate()) >= 0 && LocalDate.now().compareTo(endDate.toLocalDate()) <= 0) || (LocalDate.now().compareTo(beginDate.toLocalDate()) < 0)) {
                    // wenn das heutige Datum zwischen begin und end liegt,
                    // dann ist es die aktuelle Ausstrahlung
                    // ansonsten die zukünftige verwenden
                    broadcastDate = LocalDateTime.parse(broadcastObject.get(JSON_ELEMENT_BROADCAST).getAsString(), broadcastDateFormat);
                }
            } else {
                String broadcast = broadcastObject.get(JSON_ELEMENT_BROADCAST).getAsString();
                LocalDateTime broadcastDateTime = LocalDateTime.parse(broadcast, broadcastDateFormat);
                
                if(LocalDate.now().compareTo(broadcastDateTime.toLocalDate()) >= 0) {
                    broadcastDate = broadcastDateTime;
                }
            }
        }catch(DateTimeParseException dateTimeParseException)
        {
            LOG.error("Can't parse a broadcast relevant date.",dateTimeParseException);
        }
        return broadcastDate;
    }    
    
    /***
     * liefert das erste Ausstrahlungsdatum ohne Berücksichtigung der CatchupRights
     * @param broadcastArray
     * @return 
     */
    private LocalDateTime getFirstBroadcastDateIgnoringCatchupRights(JsonArray broadcastArray) {
        LocalDateTime broadcastDate = LocalDateTime.now();
        
        for(int i = 0; i < broadcastArray.size(); i++) {
            JsonObject broadcastObject = broadcastArray.get(i).getAsJsonObject();
            
            if(broadcastObject.has(JSON_ELEMENT_BROADCASTTYPE) && 
                broadcastObject.has(JSON_ELEMENT_BROADCAST)) {
                String type = broadcastObject.get(JSON_ELEMENT_BROADCASTTYPE).getAsString();
                
                if(type.equals(BROADCASTTTYPE_FIRST)) {
                    try {
                        broadcastDate = LocalDateTime.parse((broadcastObject.get(JSON_ELEMENT_BROADCAST).getAsString()), broadcastDateFormat);
                    }catch(DateTimeParseException dateTimeParseException)
                    {
                        LOG.error("Can't parse a broadcast relevant date.",dateTimeParseException);
                    }
                }
            }
        }
        
        return broadcastDate;
    }
}
