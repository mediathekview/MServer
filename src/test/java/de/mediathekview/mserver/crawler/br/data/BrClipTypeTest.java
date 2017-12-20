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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;

public class BrClipTypeTest {

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testGetInstanceByNameWithValidItem() {
    assertEquals(BrClipType.ITEM, BrClipType.getInstanceByName("Item"));
  }

  @Test
  public void testGetInstanceByeNameWithProgramme() {
    assertEquals(BrClipType.PROGRAMME, BrClipType.getInstanceByName("Programme"));
  }
 
  @Test
  public void testGetInstanceByeNameWithWringName() {
    assertNull(BrClipType.getInstanceByName("brzlfitz"));
  }
  
  @Test
  public void testGetNameItem() {
    assertEquals("Item", BrClipType.ITEM.getGraphQLName());
  }
  
  @Test
  public void testGetNameProgramm() {
    assertEquals("Programme", BrClipType.PROGRAMME.getGraphQLName());
  }
  
}
