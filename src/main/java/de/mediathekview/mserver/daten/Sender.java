package de.mediathekview.mserver.daten;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/** A enum of the possible sender. */
public enum Sender {
  ARD("ARD"),
  ARD_ALPHA("ARD-alpha"),
  ARTE_DE("ARTE.DE", "ARTEDE", "ARTE_DE"),
  ARTE_EN("ARTE.EN", "ARTEEN", "ARTE_EN"),
  ARTE_ES("ARTE.ES", "ARTEES", "ARTE_ES"),
  ARTE_FR("ARTE.FR", "ARTEFR", "ARTE_FR"),
  ARTE_PL("ARTE.PL", "ARTEPL", "ARTE_PL"),
  ARTE_IT("ARTE.IT", "ARTEIT", "ARTE_IT"),
  BR("BR"),
  DREISAT("3Sat"),
  DW("DW"),
  FUNK("Funk.net"),
  HR("HR"),
  KIKA("KiKA", "KIKA"),
  MDR("MDR"),
  NDR("NDR"),
  ONE("ONE"),
  ORF("ORF"),
  PHOENIX("PHOENIX"),
  RBB("RBB"),
  RBTV("rbtv", "Radio Bremen TV", "radiobremen", "radio_bremen"),
  SF("SF"),
  SR("SR"),
  SRF("SRF"),
  SRF_PODCAST("SRF.Podcast"),
  SWR("SWR"),
  TAGESSCHAU24("tagesschau24"),
  WDR("WDR"),
  WDR_COSMO("WDR COSMO"),
  WDR_KIRAKA("WDR KIRAKA"),
  WDR1_LIVE("WDR 1LIVE"),
  WDR2("WDR 2"),
  WDR3("WDR 3"),
  WDR4("WDR 4"),
  WDR5("WDR 5"),
  ZDF("ZDF"),
  ZDF_INFO("ZDFinfo"),
  ZDF_NEO("ZDFneo"),
  ZDF_TIVI("ZDF-tivi", "ZDFtivi", "ZDF Tivi");

  private final String name;
  private final String[] nameAlternatives;

  Sender(final String aName, final String... aNameAlternatives) {
    name = aName;
    nameAlternatives = aNameAlternatives;
  }

  public static Optional<Sender> getSenderByName(final String searchedSenderName) {
    for (final Sender sender : Sender.values()) {
      if (sender.getName().equalsIgnoreCase(searchedSenderName)
          || sender.name().equalsIgnoreCase(searchedSenderName)
          || Arrays.stream(sender.nameAlternatives)
              .anyMatch(n -> n.equalsIgnoreCase(searchedSenderName))) {
        return Optional.of(sender);
      }
    }

    return Optional.empty();
  }

  public static Collection<String> getSenderNamen() {
    final Collection<String> senderNamen = new ArrayList<>();

    for (final Sender sender : Sender.values()) {
      senderNamen.add(sender.getName());
    }

    return senderNamen;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
