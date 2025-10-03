package de.mediathekview.mserver.filmlisten.reader;

public class CantReadFilmException extends Exception {
  private static final long serialVersionUID = -1839526789842123501L;

  public CantReadFilmException(final String aExceptionText) {
    super(aExceptionText);
  }
}
