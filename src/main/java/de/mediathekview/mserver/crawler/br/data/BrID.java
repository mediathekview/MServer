/*
 * BrID.java
 * 
 * Projekt    : MServer
 * erstellt am: 12.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.data;

public class BrID implements Comparable<BrID>{
  
    private BrClipType type;
    private String id;
    
    private BrID() {
      
    }
    
    public BrID(BrClipType type, String id) {
      this();
      this.type = type;
      this.id = id;
    }

    public synchronized BrClipType getType() {
      return type;
    }

    public synchronized String getId() {
      return id;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      BrID other = (BrID) obj;
      if (id == null) {
        if (other.id != null)
          return false;
      } else if (!id.equals(other.id))
        return false;
      //System.out.println("Duplikat + id: " + this.id);
      return true;
    }

    @Override
    public int compareTo(BrID o) {
      return this.id.compareTo(o.getId());
    }
    
}
