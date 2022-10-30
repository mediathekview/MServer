package de.mediathekview.mserver.base.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

/** An util class to collect useful Json related methods. */
public final class JsonUtils {
  private JsonUtils() {
    super();
  }

  /**
   * Checks if a JSON tree path outgoing from the first JsonElement is correct and all given element
   * IDs exist in the given order.<br>
   * Example: If the tree is MainObj {@literal -> } FirstChild {@literal -> } SecondChild and the
   * given JsonElement is MainObj and the element IDs are "FirstChild,SecondChild" this will return
   * true.
   *
   * @param jsonElement The first element from which the tree will be checked.
   * @param crawler The crawler which using this. If it's given and an element is missing the {@link
   *     AbstractCrawler#printMissingElementErrorMessage(String)} will be called for it.
   * @param elementIds The tree-children in the order in which they should be checked.
   * @return true if the JSON tree path is correct and all given element IDs exist in the given
   *     order.
   */
  public static boolean checkTreePath(
      final JsonElement jsonElement,
      @Nullable final AbstractCrawler crawler,
      final String... elementIds) {
    JsonElement elemToCheck = jsonElement;
    for (final String elementId : elementIds) {
      if (elemToCheck.isJsonObject() && elemToCheck.getAsJsonObject().has(elementId)) {
        elemToCheck = elemToCheck.getAsJsonObject().get(elementId);
      } else {
        Optional.ofNullable(crawler)
            .ifPresent(
                abstractCrawler -> abstractCrawler.printMissingElementErrorMessage(elementId));
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the value of an attribute
   *
   * @param jsonObject the object
   * @param attributeName the name of the attribute
   * @return the value of the attribute, if it exists, else Optional.empty
   */
  public static Optional<String> getAttributeAsString(
      final JsonObject jsonObject, final String attributeName) {
    if (jsonObject.has(attributeName)) {
      final JsonElement aElement = jsonObject.get(attributeName);
      if (!aElement.isJsonNull()) {
        return Optional.of(aElement.getAsString());
      }
    }

    return Optional.empty();
  }

  public static Optional<String> getElementValueAsString(final JsonElement aJsonElement, final String... aElementIds) {
    Optional<String> rs = Optional.empty();
    try {
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
        rs =  Optional.of(aJsonObject.get(elementId).getAsString());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    //
    return rs;
  }
  
  /**
   * Checks if the {@link JsonElement} is a {@link JsonObject} and if it has all given elements and
   * if no element is null.
   *
   * @param jsonElement The element to check.
   * @param crawler The crawler which runs this task. When given and a given element is missing the
   *     error counter will be increased and a debug message will be printed.
   * @param elementIds The elements which it should have.
   * @return true when the element is a {@link JsonObject} and if it has all given elements and if
   *     no element is null.
   */
  public static boolean hasElements(
      final JsonElement jsonElement,
      @Nullable final AbstractCrawler crawler,
      final String... elementIds) {
    return jsonElement.isJsonObject()
        && hasElements(jsonElement.getAsJsonObject(), crawler, elementIds);
  }

  /**
   * Checks if the {@link JsonElement} is a {@link JsonObject} and if it has all given elements and
   * if no element is null.
   *
   * @param jsonElement The element to check.
   * @param elementIds The elements which it should have.
   * @return true when the element is a {@link JsonObject} and if it has all given elements and if
   *     no element is null.
   */
  public static boolean hasElements(final JsonElement jsonElement, final String... elementIds) {
    return hasElements(jsonElement, null, elementIds);
  }

  /**
   * Checks if the {@link JsonObject} has all given elements and if no element is null.
   *
   * @param jsonObject The object to check.
   * @param crawler The crawler which runs this task. When given and a given element is missing the
   *     error counter will be increased and a debug message will be printed.
   * @param elementIds The elements which it should have.
   * @return true when the object has all given elements and if no element is null.
   */
  public static boolean hasElements(
      final JsonObject jsonObject,
      @Nullable final AbstractCrawler crawler,
      final String... elementIds) {
    for (final String elementId : elementIds) {
      if (!jsonObject.has(elementId) || jsonObject.get(elementId).isJsonNull()) {
        Optional.ofNullable(crawler).ifPresent(c -> c.printMissingElementErrorMessage(elementId));
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if the {@link JsonObject} has all given elements and if no element is null or empty.
   *
   * @param jsonObject The object to check.
   * @param crawler The crawler which runs this task. When given and a given element is missing the
   *     error counter will be increased and a debug message will be printed.
   * @param elementIds The elements which it should have.
   * @return true when the object has all given elements and if no element is null.
   */
  public static boolean hasStringElements(
      final JsonObject jsonObject,
      @Nullable final AbstractCrawler crawler,
      final String... elementIds) {
    return hasElements(jsonObject, crawler, elementIds)
        && Arrays.stream(elementIds)
            .map(jsonObject::get)
            .map(JsonElement::getAsString)
            .noneMatch(String::isEmpty);
  }

  /**
   * Checks if the {@link JsonObject} has all given elements and if no element is null.
   *
   * @param jsonObject The object to check.
   * @param elementIds The elements which it should have.
   * @return true when the object has all given elements and if no element is null.
   */
  public static boolean hasElements(final JsonObject jsonObject, final String... elementIds) {
    return hasElements(jsonObject, null, elementIds);
  }
}
