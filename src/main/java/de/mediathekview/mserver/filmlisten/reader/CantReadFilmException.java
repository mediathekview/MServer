package de.mediathekview.mserver.filmlisten.reader;

import java.io.Serial;

public class CantReadFilmException extends Exception {
  @Serial private static final long serialVersionUID = -1839526789842123501L;

  public CantReadFilmException(final String aExceptionText) {
    super(aExceptionText);
  }
}
