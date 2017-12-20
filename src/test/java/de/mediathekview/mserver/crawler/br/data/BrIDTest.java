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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;

public class BrIDTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testConstructorAndGetters() {
    BrID id = new BrID(BrClipType.ITEM, "av:584f7f303b4679001197f6b2");
    
    assertEquals(BrClipType.ITEM, id.getType());
    assertEquals("av:584f7f303b4679001197f6b2", id.getId());

  }
  
  @Test  
  public void testComparableTwoGreaterOne() {
    BrID id1 = new BrID(BrClipType.ITEM, "av:584f7f303b4679001197f6b2");
    BrID id2 = new BrID(BrClipType.ITEM, "av:584f7f313b4679001197f7da");
    
    assertEquals(-1, id1.compareTo(id2));
  }
  
  @Test
  public void testComparableOneGreaterTwo() {
    BrID id1 = new BrID(BrClipType.ITEM, "av:584f7f313b4679001197f7da");
    BrID id2 = new BrID(BrClipType.ITEM, "av:584f7f303b4679001197f6b2");
    
    assertEquals(1, id1.compareTo(id2));
    
  }

  @Test
  public void testComparableEquals() {
    BrID id1 = new BrID(BrClipType.ITEM, "av:584f7f313b4679001197f7da");
    BrID id2 = new BrID(BrClipType.ITEM, "av:584f7f313b4679001197f7da");
    
    assertEquals(id1, id2);
    assertEquals(0, id1.compareTo(id2));
  }

  @Test
  public void testEqulsIgnoreType() {
    BrID id1 = new BrID(BrClipType.ITEM, "av:584f7f313b4679001197f7da");
    BrID id2 = new BrID(BrClipType.PROGRAMME, "av:584f7f313b4679001197f7da");
    
    assertEquals(id1, id2);
  }
  
}
