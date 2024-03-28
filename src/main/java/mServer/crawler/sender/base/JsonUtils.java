package mServer.crawler.sender.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Optional;

/**
 * A util class to collect useful Json related methods.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 * <b>Mail:</b> nicklas@wiegandt.eu<br>
 * <b>Jabber:</b> nicklas2751@elaon.de<br>
 * <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public final class JsonUtils {

  private JsonUtils() {
    super();
  }

  /**
   * Checks if a JSON tree path outgoing from the first JsonElement is correct
   * and all given element IDs exist in the given order.<br>
   * Example: If the tree is MainObj {@literal -> } FirstChild {@literal -> }
   * SecondChild and the given JsonElement is MainObj and the element IDs are
   * "FirstChild,SecondChild" this will return true.
   *
   * @param aJsonElement The first element from which the tree will be checked.
   * @param aElementIds The tree-childs in the order in which they should be
   * checked.
   * @return true if the JSON tree path is correct and all given element IDs
   * exist in the given order.
   */
  public static boolean checkTreePath(
          final JsonElement aJsonElement,
          final String... aElementIds) {
    JsonElement elemToCheck = aJsonElement;
    for (final String elementId : aElementIds) {
      if (elemToCheck.isJsonObject() && elemToCheck.getAsJsonObject().has(elementId)) {
        elemToCheck = elemToCheck.getAsJsonObject().get(elementId);
      } else {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the value of an attribute
   *
   * @param aJsonObject the object
   * @param aAttributeName the name of the attribute
   * @return the value of the attribute, if it exists, else Optional.empty
   */
  public static Optional<String> getAttributeAsString(
          final JsonObject aJsonObject, final String aAttributeName) {
    if (aJsonObject.has(aAttributeName)) {
      final JsonElement aElement = aJsonObject.get(aAttributeName);
      if (!aElement.isJsonNull()) {
        return Optional.of(aElement.getAsString());
      }
    }

    return Optional.empty();
  }

  public static Optional<Integer> getAttributeAsInt(final JsonObject jsonObject, final String attributeName) {
    if (jsonObject.has(attributeName)) {
      final JsonElement aElement = jsonObject.get(attributeName);
      if (!aElement.isJsonNull()) {
        return Optional.of(aElement.getAsInt());
      }
    }

    return Optional.empty();
  }

  /**
   * Checks if the {@link JsonElement} is a {@link JsonObject} and if it has all
   * given elements and if no element is null.
   *
   * @param aJsonElement The element to check.
   * @param aElementIds The elements which it should has.
   * @return true when the element is a {@link JsonObject} and if it has all
   * given elements and if no element is null.
   */
  public static boolean hasElements(
          final JsonElement aJsonElement,
          final String... aElementIds) {
    return aJsonElement.isJsonObject()
            && hasElements(aJsonElement.getAsJsonObject(), aElementIds);
  }

  /**
   * Checks if the {@link JsonObject} has all given elements and if no element
   * is null.
   *
   * @param aJsonObject The object to check.
   * @param aElementIds The elements which it should has.
   * @return true when the object has all given elements and if no element is
   * null.
   */
  public static boolean hasElements(
          final JsonObject aJsonObject,
          final String... aElementIds) {
    for (final String elementId : aElementIds) {
      if (!aJsonObject.has(elementId) || aJsonObject.get(elementId).isJsonNull()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the {@link JsonObject} has all given elements and if no element
   * is null or empty.
   *
   * @param aJsonObject The object to check.
   * @param aElementIds The elements which it should has.
   * @return true when the object has all given elements and if no element is
   * null.
   */
  public static boolean hasStringElements(
          final JsonObject aJsonObject,
          final String... aElementIds) {
    return hasElements(aJsonObject, aElementIds)
            && Arrays.stream(aElementIds)
                    .map(aJsonObject::get)
                    .map(JsonElement::getAsString)
                    .noneMatch(String::isEmpty);
  }
  
  public static Optional<String> getElementValueAsString(final JsonElement aJsonElement, final String... aElementIds) {
    Optional<JsonElement> rs = JsonUtils.getElement(aJsonElement, aElementIds);
    if (rs.isPresent()) {
      return Optional.of(rs.get().getAsString());
    }
    return Optional.empty();
  }

  public static Optional<JsonElement> getElement(final JsonElement aJsonElement, final String... aElementIds) {
    Optional<JsonElement> rs = Optional.empty();
    if (aElementIds == null || aElementIds.length == 0) {
      return rs;
    }
    JsonObject aJsonObject = aJsonElement.getAsJsonObject();
    for (int i = 0; i < aElementIds.length-1; i++) {
      String elementId = aElementIds[i];
      if (aJsonObject.has(elementId) && aJsonObject.get(elementId).isJsonObject()) {
        aJsonObject = aJsonObject.getAsJsonObject(elementId);
      } else {
        aJsonObject = null;
        break;
      }
    }
    //
    String elementId = aElementIds[aElementIds.length-1];
    if (aJsonObject != null && aJsonObject.has(elementId) && !aJsonObject.get(elementId).isJsonNull()) {
      rs =  Optional.of(aJsonObject.get(elementId));
    }
    //
    return rs;
  }


}
