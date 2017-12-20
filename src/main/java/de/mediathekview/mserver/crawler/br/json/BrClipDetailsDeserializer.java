/*
 * BrClipDetailsDeserializer.java
 * 
 * Projekt    : MServer
 * erstellt am: 19.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.json;

import java.lang.reflect.Type;
import java.util.Set;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.data.BrID;

public class BrClipDetailsDeserializer implements JsonDeserializer<Set<Film>> {

  private AbstractCrawler crawler;
  private BrID id;
  
  public BrClipDetailsDeserializer(AbstractCrawler crawler, BrID id) {
    super();
    this.crawler = crawler;
    this.id = id;
  }

  @Override
  public Set<Film> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    // TODO Auto-generated method stub
    return null;
  }

  
  
}
