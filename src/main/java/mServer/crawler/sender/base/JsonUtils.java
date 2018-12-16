package mServer.crawler.sender.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Optional;

public final class JsonUtils {

  private JsonUtils() {
    super();
  }

  /**
   * Checks if a JSON tree path outgoing from the first JsonElement is correct
   * and all given element IDs exist in the given order.<br>
   * Example: If the tree is MainObj -> FirstChild -> SecondChild and the given
   * JsonElement is MainObj and the element IDs are "FirstChild,SecondChild"
   * this will return true.
   *
   * @param aJsonElement The first element from which the tree will be checked.
   * @param aElementIds The tree-childs in the order in which they should be
   * checked.
   * @return
   */
  public static boolean checkTreePath(final JsonElement aJsonElement,
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
  public static Optional<String> getAttributeAsString(final JsonObject aJsonObject,
          final String aAttributeName) {
    if (aJsonObject.has(aAttributeName)) {
      final JsonElement aElement = aJsonObject.get(aAttributeName);
      if (!aElement.isJsonNull()) {
        return Optional.of(aElement.getAsString());
      }
    }

    return Optional.empty();
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
  public static boolean hasElements(final JsonObject aJsonObject,
          final String... aElementIds) {
    for (final String elementId : aElementIds) {
      if (!aJsonObject.has(elementId) || aJsonObject.get(elementId).isJsonNull()) {
        return false;
      }
    }

    return true;
  }
}
