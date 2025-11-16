package de.mediathekview.mserver.daten;

import java.util.Comparator;

public enum MediaResourceComperators {
  SENDER_COMPERAOR(
      Comparator.comparing(
          AbstractMediaResource::getSender, Comparator.nullsFirst(Comparator.naturalOrder()))),
  TITEL_COMPERATOR(
      Comparator.comparing(
          AbstractMediaResource::getTitel, Comparator.nullsFirst(Comparator.naturalOrder()))),
  THEMA_COMPERATOR(
      Comparator.comparing(
          AbstractMediaResource::getThema, Comparator.nullsFirst(Comparator.naturalOrder()))),
  DATE_COMPERATOR(
      Comparator.comparing(
          AbstractMediaResource::getTime, Comparator.nullsFirst(Comparator.reverseOrder()))),
  DEFAULT_COMPERATOR(createDefaultComperator());

  private final Comparator<AbstractMediaResource<?>> comparator;

  MediaResourceComperators(final Comparator<AbstractMediaResource<?>> acomparator) {
    comparator = acomparator;
  }

  private static Comparator<AbstractMediaResource<?>> createDefaultComperator() {
    return SENDER_COMPERAOR
        .getComparator()
        .thenComparing(THEMA_COMPERATOR.getComparator())
        .thenComparing(DATE_COMPERATOR.getComparator());
  }

  public Comparator<AbstractMediaResource<?>> getComparator() {
    return comparator;
  }
}
