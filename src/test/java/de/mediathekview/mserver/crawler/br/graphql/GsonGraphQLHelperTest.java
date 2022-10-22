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

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GsonGraphQLHelperTest {

  @Test
  void testChildIsRegularJsonArray() {

    JsonObject jo = new JsonObject();

    JsonArray ja = new JsonArray();

    ja.add("one");
    ja.add("two");

    jo.add("childArray", ja);

    assertThat(GsonGraphQLHelper.getChildArrayIfExists(jo, "childArray").get().getClass())
        .hasSameClassAs(JsonArray.class);
  }

  @Test
  void testChildWithWrongArrayName() {

    JsonObject jo = new JsonObject();

    JsonArray ja = new JsonArray();

    ja.add("one");
    ja.add("two");

    jo.add("wrongName", ja);

    assertThat(GsonGraphQLHelper.getChildArrayIfExists(jo, "childArray")).isEmpty();
  }

  @Test
  void testChildIsJsonNull() {

    JsonObject jo = new JsonObject();

    JsonNull jn = JsonNull.INSTANCE;

    jo.add("childArray", jn);

    assertThat(GsonGraphQLHelper.getChildArrayIfExists(jo, "childArray")).isEmpty();
  }
}
