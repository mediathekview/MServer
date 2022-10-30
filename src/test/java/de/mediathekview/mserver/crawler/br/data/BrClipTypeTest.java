/*
 * BrClipTypeTest.java
 *
 * Projekt    : MServer
 * erstellt am: 12.12.2017
 * Autor      : Sascha
 *
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrClipTypeTest {

  @Test
  void testGetInstanceByNameWithValidItem() {
    assertThat(BrClipType.getInstanceByName("Item")).isEqualTo(BrClipType.ITEM);
  }

  @Test
  void testGetInstanceByeNameWithProgramme() {
    assertThat(BrClipType.getInstanceByName("Programme")).isEqualTo(BrClipType.PROGRAMME);
  }

  @Test
  void testGetInstanceByeNameWithWringName() {
    assertThat(BrClipType.getInstanceByName("brzlfitz")).isNull();
  }

  @Test
  void testGetNameItem() {
    assertThat(BrClipType.ITEM.getGraphQLName()).isEqualTo("Item");
  }

  @Test
  void testGetNameProgramm() {
    assertThat(BrClipType.PROGRAMME.getGraphQLName()).isEqualTo("Programme");
  }
}
