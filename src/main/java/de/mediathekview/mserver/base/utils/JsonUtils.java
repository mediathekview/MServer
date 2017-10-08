package de.mediathekview.mserver.base.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A util class to collect useful Json related methods.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public final class JsonUtils {
  private JsonUtils() {
    super();
  }

  /**
   * Checks if the {@link JsonElement} is a {@link JsonObject} and if it has all given elements and
   * if no element is null.
   *
   * @param aJsonElement The element to check.
   * @param aElementIds The elements which it should has.
   * @return true when the element is a {@link JsonObject} and if it has all given elements and if
   *         no element is null.
   */
  public static boolean hasElements(final JsonElement aJsonElement, final String... aElementIds) {
    return aJsonElement.isJsonObject() && hasElements(aJsonElement.getAsJsonObject(), aElementIds);
  }

  /**
   * Checks if the {@link JsonObject} has all given elements and if no element is null.
   *
   * @param aJsonObject The object to check.
   * @param aElementIds The elements which it should has.
   * @return true when the object has all given elements and if no element is null.
   */
  public static boolean hasElements(final JsonObject aJsonObject, final String... aElementIds) {
    for (final String elementId : aElementIds) {
      if (!aJsonObject.has(elementId) || aJsonObject.get(elementId).isJsonNull()) {
        return false;
      }
    }
    return true;
  }
}
