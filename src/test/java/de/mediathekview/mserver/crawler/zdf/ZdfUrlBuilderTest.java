package de.mediathekview.mserver.crawler.zdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class ZdfUrlBuilderTest {
  @Test
  void testBuildLetterPageUrl() {
    assertEquals(
            "https://api.zdf.de/graphql?operationName=specialPageByCanonical&variables=%7B%22staticGridClusterPageSize%22%3A6%2C%22staticGridClusterOffset%22%3A0%2C%22canonical%22%3A%22sendungen-100%22%2C%22endCursor%22%3Anull%2C%22tabIndex%22%3A0%2C%22itemsFilter%22%3A%7B%22teaserUsageNotIn%22%3A%5B%22TIVI_HBBTV_ONLY%22%5D%7D%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%227d33167e7700ba57779f48b28b5d485c8ada0a1d5dfbdc8a261b7bd62ca28ba7%22%7D%7D",
        ZdfUrlBuilder.buildLetterPageUrl(ZdfConstants.NO_CURSOR, 0));
  }

  @Test
  void testBuildLetterPageUrlWithCursor() {
    assertEquals(
            "https://api.zdf.de/graphql?operationName=specialPageByCanonical&variables=%7B%22staticGridClusterPageSize%22%3A6%2C%22staticGridClusterOffset%22%3A0%2C%22canonical%22%3A%22sendungen-100%22%2C%22endCursor%22%3A%22MyCursorValue%22%2C%22tabIndex%22%3A0%2C%22itemsFilter%22%3A%7B%22teaserUsageNotIn%22%3A%5B%22TIVI_HBBTV_ONLY%22%5D%7D%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%227d33167e7700ba57779f48b28b5d485c8ada0a1d5dfbdc8a261b7bd62ca28ba7%22%7D%7D",
            ZdfUrlBuilder.buildLetterPageUrl("MyCursorValue", 0));
  }

  @Test
  void testBuildTopicSeasonUrl() {
    assertEquals("https://api.zdf.de/graphql?operationName=seasonByCanonical&variables=%7B%22seasonIndex%22%3A18%2C%22episodesPageSize%22%3A24%2C%22canonical%22%3A%22soko-wismar-104%22%2C%22sortBy%22%3A%5B%7B%22field%22%3A%22EDITORIAL_DATE%22%2C%22direction%22%3A%22DESC%22%7D%5D%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%229412a0f4ac55dc37d46975d461ec64bfd14380d815df843a1492348f77b5c99a%22%7D%7D",
            ZdfUrlBuilder.buildTopicSeasonUrl(18, 24, "soko-wismar-104"));
  }

  @Test
  void testBuildTopicSeasonUrlWithCursor() {
    assertEquals("https://api.zdf.de/graphql?operationName=seasonByCanonical&variables=%7B%22seasonIndex%22%3A18%2C%22episodesPageSize%22%3A24%2C%22canonical%22%3A%22soko-wismar-104%22%2C%22sortBy%22%3A%5B%7B%22field%22%3A%22EDITORIAL_DATE%22%2C%22direction%22%3A%22DESC%22%7D%5D%2C%22episodesAfter%22%3A%22WyJmZTlmZTc5ZDU1IiwiMjAyMy0xMi0wOFQxOTo0NjowNi4wOTIwMDArMDA6MDAiLCJwYWdlLXZpZGVvLWFyZF92aWRlb19hcmRfZFhKdU9tRnlaRHB3ZFdKc2FXTmhkR2x2YmpvMVlqSXhZV1E1TmpFeFpqVTRNalZqIl0%3D%22%7D&extensions=%7B%22persistedQuery%22%3A%7B%22version%22%3A1%2C%22sha256Hash%22%3A%229412a0f4ac55dc37d46975d461ec64bfd14380d815df843a1492348f77b5c99a%22%7D%7D",
            ZdfUrlBuilder.buildTopicSeasonUrl(18, 24, "soko-wismar-104", "WyJmZTlmZTc5ZDU1IiwiMjAyMy0xMi0wOFQxOTo0NjowNi4wOTIwMDArMDA6MDAiLCJwYWdlLXZpZGVvLWFyZF92aWRlb19hcmRfZFhKdU9tRnlaRHB3ZFdKc2FXTmhkR2x2YmpvMVlqSXhZV1E1TmpFeFpqVTRNalZqIl0="));
  }

}