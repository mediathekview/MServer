package de.mediathekview.mserver.crawler.arte.json;

import de.mediathekview.mserver.daten.Sender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ArteVideoTypeMapper {

  private static final Logger LOG = LogManager.getLogger(ArteVideoTypeMapper.class);

  private ArteVideoTypeMapper() {}

  public static Optional<ArteVideoType> map(Sender sender, String code) {
    switch (sender) {
      case ARTE_DE:
        return mapGerman(code);
      case ARTE_EN:
        return mapEnglish(code);
      case ARTE_ES:
        return mapSpanish(code);
      case ARTE_FR:
        return mapFrench(code);
      case ARTE_IT:
        return mapItalian(code);
      case ARTE_PL:
        return mapPolish(code);
      default:
        LOG.debug("unknown sender: {}", sender);
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapSpanish(String code) {
    switch (code) {
      case "VE[ESP]-STE[ESP]", "VO-STE[ESP]", "VOA-STE[ESP]", "VOEU-STE[ESP]", "VOF-STE[ESP]":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        if(code.contains("ESP")) {
          LOG.debug("add spanish: {}", code);
        }
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapEnglish(String code) {
    switch (code) {
      case "VE[ANG]":
        return Optional.of(ArteVideoType.DEFAULT);
      case "VE[ANG]-STE[ANG]", "VO-STE[ANG]", "VOA-STE[ANG]", "VOEU-STE[ANG]", "VOF-STE[ANG]":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        if(code.contains("ANG")) {
          LOG.debug("add english: {}", code);
        }
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapFrench(String code) {
    switch (code) {
      case "VF", "VF-STF", "VOF", "VOF-STF":
        return Optional.of(ArteVideoType.DEFAULT);
      case "VF-STMF", "VOF-STMF":
        return Optional.of(ArteVideoType.SUBTITLE_INCLUDED);
      case "VFAUD":
        return Optional.of(ArteVideoType.AUDIO_DESCRIPTION);
      case "VO-STF":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapGerman(String code) {
    switch (code) {
      case "VA", "VA-STA", "VOA", "VOA-STA":
        return Optional.of(ArteVideoType.DEFAULT);
      case "VA-STMA", "VOA-STMA":
        return Optional.of(ArteVideoType.SUBTITLE_INCLUDED);
      case "VAAUD":
        return Optional.of(ArteVideoType.AUDIO_DESCRIPTION);
      case "VO":
        return Optional.of(ArteVideoType.ORIGINAL);
      case "VO-STA", "VOF-STA", "VOA-STF":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapItalian(String code) {
    switch (code) {
      case "VE[ITA]-STE[ITA]", "VO-STE[ITA]", "VOA-STE[ITA]", "VOEU-STE[ITA]", "VOF-STE[ITA]":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapPolish(String code) {
    switch (code) {
      case "VE[POL]-STE[POL]", "VO-STE[POL]", "VOA-STE[POL]", "VOEU-STE[POL]", "VOF-STE[POL]":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        return Optional.empty();
    }
  }
}
