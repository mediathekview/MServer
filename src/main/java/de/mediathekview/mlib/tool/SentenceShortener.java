package de.mediathekview.mlib.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class SentenceShortener {
  private static final Pattern WHITESPACE_REGEX = Pattern.compile("\\s+");
  private static final Pattern SENTENCE_BOUNDARY_REGEX = Pattern.compile("(?<=[.!?])\\s+");
  private static final String SHORTENING_SUFFIX = "...";

  private SentenceShortener() {
  }

  /**
   * Shortens text to a target length while preserving complete sentences.
   *
   * <p>Whitespace is normalized before shortening. If the text is already within the target
   * length, the normalized text is returned unchanged. If adding the next sentence would exceed
   * {@code targetLength + marginLength}, that sentence is omitted and {@code ...} is appended to
   * the returned text. If the first sentence alone is already longer than the limit, it is kept
   * whole.
   *
   * @param text the text to shorten; may be {@code null}
   * @param targetLength the preferred target length; must be positive
   * @param marginLength the allowed soft overflow beyond the target; must not be negative
   * @return the shortened text, or {@code null} if the input is {@code null} or blank
   * @throws IllegalArgumentException if {@code targetLength <= 0} or {@code marginLength < 0}
   */
  public static String shorten(String text, int targetLength, int marginLength) {
    if (targetLength <= 0) {
      throw new IllegalArgumentException("targetLength must be positive");
    }
    if (marginLength < 0) {
      throw new IllegalArgumentException("marginLength must not be negative");
    }

    String normalized = normalize(text);
    if (normalized == null || normalized.length() <= targetLength) {
      return normalized;
    }

    int softLimit = targetLength + marginLength;
    String[] rawSentences = SENTENCE_BOUNDARY_REGEX.split(normalized);
    List<String> sentences = new ArrayList<>();
    for (String rawSentence : rawSentences) {
      String sentence = rawSentence.trim();
      if (!sentence.isEmpty()) {
        sentences.add(sentence);
      }
    }
    if (sentences.isEmpty()) {
      return normalized;
    }

    List<String> selectedSentences = new ArrayList<>();
    int currentLength = 0;
    for (String sentence : sentences) {
      int candidateLength = selectedSentences.isEmpty()
              ? sentence.length()
              : currentLength + 1 + sentence.length();
      if (candidateLength > softLimit) {
        break;
      }
      selectedSentences.add(sentence);
      currentLength = candidateLength;
    }

    if (!selectedSentences.isEmpty()) {
      String shortened = String.join(" ", selectedSentences);
      return selectedSentences.size() < sentences.size()
              ? shortened + SHORTENING_SUFFIX
              : shortened;
    }
    return sentences.size() > 1
            ? sentences.get(0) + SHORTENING_SUFFIX
            : sentences.get(0);
  }

  /**
   * Shortens text to a target length without any soft overflow margin.
   *
   * @param text the text to shorten; may be {@code null}
   * @param targetLength the preferred target length; must be positive
   * @return the shortened text, or {@code null} if the input is {@code null} or blank
   * @throws IllegalArgumentException if {@code targetLength <= 0}
   */
  public static String shorten(String text, int targetLength) {
    return shorten(text, targetLength, 0);
  }

  /**
   * Normalizes whitespace and trims a text value.
   *
   * @param text the text to normalize; may be {@code null}
   * @return the normalized text, or {@code null} if the input is {@code null} or blank
   */
  public static String normalize(String text) {
    if (text == null) {
      return null;
    }
    String normalized = WHITESPACE_REGEX.matcher(text).replaceAll(" ").trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
