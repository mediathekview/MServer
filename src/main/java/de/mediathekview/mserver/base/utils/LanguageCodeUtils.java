package de.mediathekview.mserver.base.utils;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Bringt die Sprachcodes, die die Sender liefern, in die Form, die {@code Film.audioLanguage}
 * zusichert: dreistelliger ISO-639-2/T-Code in Kleinschreibung.
 *
 * <p>Die Sender sind darin uneinheitlich. Die ARD schreibt dreistellig ("deu", "eng", "fra"), arte
 * zweistellig ("de", "fr", "es"). Beide verwenden ausserdem Platzhalter, die gar keine Sprache
 * benennen - "ov" bei der ARD, "und" und "mul" bei arte. Diese Klasse ist die eine Stelle, an der
 * das zusammengefuehrt wird, damit im Feld nichts landet, worauf sich Clients nicht verlassen
 * koennen.
 */
public final class LanguageCodeUtils {

  /**
   * Werte, die dort stehen, wo eine Sprache erwartet wird, aber keine benennen: ARDs Marker "ov"
   * fuer die Originalfassung sowie die ISO-Platzhalter fuer unbestimmt, nicht kodiert, mehrsprachig
   * und "kein sprachlicher Inhalt".
   */
  private static final Set<String> NON_LANGUAGE_CODES = Set.of("ov", "und", "mis", "mul", "zxx");

  /**
   * ISO 639-1 auf ISO 639-2/T, beschraenkt auf die Sprachen, die in den Mediatheken vorkommen.
   *
   * <p>Bewusst als Tabelle statt ueber {@link Locale}: dessen Sprachdaten haengen unter Linux an
   * ICU und stehen nicht auf jedem Host vollstaendig zur Verfuegung. Eine Zuordnung, die je nach
   * Rechner ein anderes Ergebnis liefert, waere fuer ein Feld in der Filmliste die schlechtere
   * Wahl.
   *
   * <p>Die dreistelligen Codes sind die terminologische Reihe (ISO 639-2/T): "deu" statt "ger",
   * "fra" statt "fre", "nld" statt "dut", "ces" statt "cze", "ell" statt "gre", "ron" statt "rum",
   * "slk" statt "slo", "isl" statt "ice", "zho" statt "chi".
   */
  private static final Map<String, String> TWO_TO_THREE_LETTER =
      Map.ofEntries(
          Map.entry("de", "deu"),
          Map.entry("en", "eng"),
          Map.entry("fr", "fra"),
          Map.entry("es", "spa"),
          Map.entry("it", "ita"),
          Map.entry("pl", "pol"),
          Map.entry("nl", "nld"),
          Map.entry("pt", "por"),
          Map.entry("ru", "rus"),
          Map.entry("tr", "tur"),
          Map.entry("ar", "ara"),
          Map.entry("uk", "ukr"),
          Map.entry("el", "ell"),
          Map.entry("sv", "swe"),
          Map.entry("da", "dan"),
          Map.entry("no", "nor"),
          Map.entry("fi", "fin"),
          Map.entry("cs", "ces"),
          Map.entry("hu", "hun"),
          Map.entry("ro", "ron"),
          Map.entry("sk", "slk"),
          Map.entry("hr", "hrv"),
          Map.entry("sr", "srp"),
          Map.entry("sl", "slv"),
          Map.entry("bg", "bul"),
          Map.entry("is", "isl"),
          Map.entry("ja", "jpn"),
          Map.entry("zh", "zho"),
          Map.entry("ko", "kor"),
          Map.entry("he", "heb"),
          Map.entry("fa", "fas"),
          Map.entry("hi", "hin"));

  private LanguageCodeUtils() {}

  /**
   * Normalisiert einen Sprachcode aus den Senderdaten.
   *
   * <p>Eine etwaige Regionsangabe wird abgeschnitten ("spa-ES" wird zu "spa", "de-DE" zu "deu").
   * Zweistellige Codes werden ueber die Tabelle zugeordnet, dreistellige durchgereicht. Alles, was
   * keine Sprache benennt - die Platzhalter, ein leerer Wert, ein zweistelliger Code ausserhalb der
   * Tabelle - ergibt nichts. Lieber kein Wert als einer, auf den sich Clients nicht verlassen
   * koennen.
   *
   * @param rawCode der Code, wie der Sender ihn liefert.
   * @return der dreistellige Code, oder leer.
   */
  public static Optional<String> normalize(final String rawCode) {
    if (rawCode == null || rawCode.isBlank()) {
      return Optional.empty();
    }

    final String primary = rawCode.trim().split("-")[0].toLowerCase(Locale.ROOT);
    if (NON_LANGUAGE_CODES.contains(primary)) {
      return Optional.empty();
    }

    if (primary.length() == 2) {
      return Optional.ofNullable(TWO_TO_THREE_LETTER.get(primary));
    }

    if (primary.length() == 3 && primary.chars().allMatch(Character::isLetter)) {
      return Optional.of(primary);
    }

    return Optional.empty();
  }
}
