/*
 * BrIDTest.java
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

class BrIDTest {

  @Test
  void testConstructorAndGetters() {
    final BrID id = new BrID(BrClipType.ITEM, "av:584f7f303b4679001197f6b2");

    assertThat(id.getType()).isEqualTo(BrClipType.ITEM);
    assertThat(id.getId()).isEqualTo("av:584f7f303b4679001197f6b2");
  }

  @Test
  void testComparableTwoGreaterOne() {
    final BrID id1 = new BrID(BrClipType.ITEM, "av:584f7f303b4679001197f6b2");
    final BrID id2 = new BrID(BrClipType.ITEM, "av:584f7f313b4679001197f7da");

    assertThat(id1.compareTo(id2)).isEqualTo(-1);
  }

  @Test
  void testComparableOneGreaterTwo() {
    final BrID id1 = new BrID(BrClipType.ITEM, "av:584f7f313b4679001197f7da");
    final BrID id2 = new BrID(BrClipType.ITEM, "av:584f7f303b4679001197f6b2");

    assertThat(id1.compareTo(id2)).isEqualTo(1);
  }

  @Test
  void testComparableEquals() {
    final BrID id1 = new BrID(BrClipType.ITEM, "av:584f7f313b4679001197f7da");
    final BrID id2 = new BrID(BrClipType.ITEM, "av:584f7f313b4679001197f7da");

    assertThat(id1).isEqualTo(id2);
    assertThat(id1).isEqualByComparingTo(id2);
  }

  @Test
  void testEqulsIgnoreType() {
    final BrID id1 = new BrID(BrClipType.ITEM, "av:584f7f313b4679001197f7da");
    final BrID id2 = new BrID(BrClipType.PROGRAMME, "av:584f7f313b4679001197f7da");

    assertThat(id1).isEqualTo(id2);
  }
}
