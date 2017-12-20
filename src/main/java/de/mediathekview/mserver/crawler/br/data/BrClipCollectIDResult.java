/*
 * BrClipCollectResult.java
 * 
 * Projekt    : MServer
 * erstellt am: 12.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.data;

import de.mediathekview.mserver.crawler.br.json.BrIdsDTO;

public class BrClipCollectIDResult {
  
  private BrIdsDTO clipList;
  private boolean hasNextPage;
  private String cursor = null;
  private int resultSize;
  
  public synchronized BrIdsDTO getClipList() {
    return clipList;
  }
  public synchronized boolean hasNextPage() {
    return hasNextPage;
  }
  public synchronized String getCursor() {
    return cursor;
  }
  public synchronized void setClipList(BrIdsDTO clipList) {
    this.clipList = clipList;
  }
  
  public synchronized void setHasNextPage() {
    this.hasNextPage = true;
  }
  
  public synchronized void setHasNonNextPage() {
    this.hasNextPage = false;
  }
  
  public synchronized void setCursor(String cursor) {
    this.cursor = cursor;
  }
  public synchronized int getResultSize() {
    return resultSize;
  }
  public synchronized void setResultSize(int resultSize) {
    this.resultSize = resultSize;
  }
  
  
}
