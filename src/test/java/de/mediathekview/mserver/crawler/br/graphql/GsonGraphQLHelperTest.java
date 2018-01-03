/*
 * GsonGraphQLHelperTest.java
 * 
 * Projekt    : MServer
 * erstellt am: 03.01.2018
 * Autor      : Sascha
 * 
 * (c) 2018 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.graphql;

import static org.junit.Assert.*;
import java.util.Optional;
import org.junit.After;
import org.junit.Test;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class GsonGraphQLHelperTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testChildIsRegularJsonArray() {
    
    JsonObject jo = new JsonObject();
    
    JsonArray ja = new JsonArray();
    
    ja.add("one");
    ja.add("two");
    
    jo.add("childArray",ja);

    assertEquals(JsonArray.class, GsonGraphQLHelper.getChildArrayIfExists(jo, "childArray").get().getClass());
    
  }

  @Test
  public void testChildWithWrongArrayName() {
    
    JsonObject jo = new JsonObject();
    
    JsonArray ja = new JsonArray();
    
    ja.add("one");
    ja.add("two");
    
    jo.add("wrongName", ja);
    
    assertEquals(Optional.empty(), GsonGraphQLHelper.getChildArrayIfExists(jo, "childArray"));
    
  }
  
  @Test
  public void testChildIsJsonNull() {
    
    JsonObject jo = new JsonObject();
    
    JsonNull jn = JsonNull.INSTANCE;
    
    jo.add("childArray", jn);
    
    assertEquals(Optional.empty(), GsonGraphQLHelper.getChildArrayIfExists(jo, "childArray"));
    
  }

}
