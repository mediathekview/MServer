package de.mediathekview.mserver.crawler.funk.json;

import com.google.gson.*;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractFunkElementDeserializer<T>
    implements JsonDeserializer<PagedElementListDTO<T>> {
  private static final Logger LOG = LogManager.getLogger(AbstractFunkElementDeserializer.class);
  private static final String TAG_LINKS = "_links";
  private static final String TAG_NEXT = "next";
  private static final String ATTRIBUTE_HREF = "href";
  private static final String TAG_EMBEDDED = "_embedded";
  private static final String WRONG_URL_PART = "/v4.";
  private static final String RIGHT_URL_PART = "/api/v4.";
  private static final String TAG_PAGE = "page";
  private static final String ATTRIBUTE_SIZE = "size";
  private static final String ATTRIBUTE_NUMBER = "number";
  private static final String ATTRIBUTE_TOTAL = "totalElements";
  private static final String FUNK_CORE_SERVICE = "funk-core-service.default.svc";
  private static final String FUNK_CORE_SERVICE_HOST = "www.funk.net/api";
  protected final Optional<AbstractCrawler> crawler;

  protected AbstractFunkElementDeserializer(
      final Optional<AbstractCrawler> aCrawler) {
    super();
    crawler = aCrawler;
  }

  @Override
  public PagedElementListDTO<T> deserialize(
      final JsonElement baseElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    final PagedElementListDTO<T> funkElementList = new PagedElementListDTO<>();
    final JsonObject baseObject = baseElement.getAsJsonObject();

    funkElementList.setNextPage(getNextPageLink(baseElement, baseObject));
    analyzeActualSize(baseElement, funkElementList.getNextPage().isPresent());

    if (JsonUtils.checkTreePath(
        baseElement, crawler.orElse(null), TAG_EMBEDDED, getElementListTag())) {
      funkElementList.addElements(
          StreamSupport.stream(
                  baseObject
                      .getAsJsonObject(TAG_EMBEDDED)
                      .getAsJsonArray(getElementListTag())
                      .spliterator(),
                  true)
              .filter(element -> JsonUtils.hasElements(element, getRequiredTags()))
              .map(JsonElement::getAsJsonObject)
              .map(this::mapToElement)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet()));
    } else {
      LOG.fatal("A Funk list is broken! {}", baseElement);
    }

    return funkElementList;
  }

  /*
   * the size element contains the size we requested - regardless the number of elements on the page.
   */
  private void analyzeActualSize(final JsonElement baseElement, final boolean hasNext) {
    if (JsonUtils.checkTreePath(baseElement, null, TAG_PAGE, ATTRIBUTE_SIZE)) {
      final int total =
          baseElement.getAsJsonObject().getAsJsonObject(TAG_PAGE).get(ATTRIBUTE_TOTAL).getAsInt();
      final int pageSize =
          baseElement.getAsJsonObject().getAsJsonObject(TAG_PAGE).get(ATTRIBUTE_SIZE).getAsInt();
      int elementCount = pageSize; // for all pages
      if (pageSize > total) {
        elementCount = total; // first page and not filled
      } else if (!hasNext) {
        elementCount = (total % pageSize); // last page
      }
      addSizeToStatistic(elementCount);
    }
  }

  protected void addSizeToStatistic(final int actualSize) {
    LOG.debug("Actual page size: {}", actualSize);
  }

  protected abstract T mapToElement(JsonObject jsonObject);

  protected abstract String[] getRequiredTags();

  protected abstract String getElementListTag();

  private Optional<String> getNextPageLink(
      final JsonElement baseElement, final JsonObject baseObject) {
    if (JsonUtils.checkTreePath(baseElement, null, TAG_PAGE, ATTRIBUTE_NUMBER)
        && JsonUtils.checkTreePath(baseElement, null, TAG_LINKS, TAG_NEXT, ATTRIBUTE_HREF)) {
      return Optional.of(
          fixNextPageUrl(
              baseObject
                  .getAsJsonObject(TAG_LINKS)
                  .getAsJsonObject(TAG_NEXT)
                  .get(ATTRIBUTE_HREF)
                  .getAsString()));
    }
    return Optional.empty();
  }

  private String fixNextPageUrl(final String url) {
    return url
        .replace(WRONG_URL_PART, RIGHT_URL_PART)
        .replace(FUNK_CORE_SERVICE, FUNK_CORE_SERVICE_HOST);
  }
}
