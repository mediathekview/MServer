package de.mediathekview.mserver.crawler.arte;

import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.daten.Sender;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArteRestVideoTypeMapper {

  private static final Logger LOG = LogManager.getLogger(ArteRestVideoTypeMapper.class);

  private ArteRestVideoTypeMapper() {}

  public static Optional<Resolution> mapQuality(String quality) {
    return switch (quality) {
      case "EQ", "XQ" -> Optional.of(Resolution.NORMAL);
      case "HQ" -> Optional.of(Resolution.SMALL);
      case "SQ" -> Optional.of(Resolution.HD);
      case "MQ" -> Optional.of(Resolution.VERY_SMALL);
      default -> {
        LOG.debug("unknown quality: {}", quality);
        yield Optional.empty();
      }
    };
  }

  public static Optional<ArteVideoType> map(Sender sender, String code) {
    return switch (sender) {
      case ARTE_DE -> mapGerman(code);
      case ARTE_EN -> mapEnglish(code);
      case ARTE_ES -> mapSpanish(code);
      case ARTE_FR -> mapFrench(code);
      case ARTE_IT -> mapItalian(code);
      case ARTE_PL -> mapPolish(code);
      default -> {
        LOG.debug("unknown sender: {}", sender);
        yield Optional.empty();
      }
    };
  }

  private static Optional<ArteVideoType> mapSpanish(String code) {
    return switch (code) {
      case "VE[ESP]" -> Optional.of(ArteVideoType.DEFAULT);
      case "VE[ESP]-STE[ESP]", "VO-STE[ESP]", "VOA-STE[ESP]", "VOEU-STE[ESP]", "VOF-STE[ESP]" ->
              Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default -> Optional.empty();
    };
  }

  private static Optional<ArteVideoType> mapEnglish(String code) {
    return switch (code) {
      case "VE[ANG]" -> Optional.of(ArteVideoType.DEFAULT);
      case "VE[ANG]-STE[ANG]", "VO-STE[ANG]", "VOA-STE[ANG]", "VOEU-STE[ANG]", "VOF-STE[ANG]" ->
              Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default -> Optional.empty();
    };
  }

  private static Optional<ArteVideoType> mapFrench(String code) {
    return switch (code) {
      case "VF", "VOF", "VOF-STF", "VF-STF" -> Optional.of(ArteVideoType.DEFAULT);
      case "VF-STMF", "VOF-STMF" -> Optional.of(ArteVideoType.SUBTITLE_INCLUDED);
      case "VFAUD" -> Optional.of(ArteVideoType.AUDIO_DESCRIPTION);
      case "VO-STF" -> Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default -> Optional.empty();
    };
  }

  private static Optional<ArteVideoType> mapGerman(String code) {
    return switch (code) {
      case "VA", "VA-STA", "VOA", "VOA-STA" -> Optional.of(ArteVideoType.DEFAULT);
      case "VA-STMA", "VOA-STMA" -> Optional.of(ArteVideoType.SUBTITLE_INCLUDED);
      case "VAAUD" -> Optional.of(ArteVideoType.AUDIO_DESCRIPTION);
      case "VO" -> Optional.of(ArteVideoType.ORIGINAL);
      case "VO-STA", "VOA-STF", "VOF-STA" -> Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default -> Optional.empty();
    };
  }

  private static Optional<ArteVideoType> mapItalian(String code) {
    return switch (code) {
      case "VE[ITA]-STE[ITA]", "VO-STE[ITA]", "VOA-STE[ITA]", "VOEU-STE[ITA]", "VOF-STE[ITA]" ->
              Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default -> Optional.empty();
    };
  }

  private static Optional<ArteVideoType> mapPolish(String code) {
    return switch (code) {
      case "VE[POL]-STE[POL]", "VO-STE[POL]", "VOA-STE[POL]", "VOEU-STE[POL]", "VOF-STE[POL]" ->
              Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default -> Optional.empty();
    };
  }
}
