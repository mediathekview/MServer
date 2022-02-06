package mServer.crawler.sender.arte.json;

import de.mediathekview.mlib.Const;
import mServer.crawler.sender.arte.ArteConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ArteVideoTypeMapper {

  private static final Logger LOG = LogManager.getLogger(ArteVideoTypeMapper.class);

  private ArteVideoTypeMapper() {
  }

  public static Optional<ArteVideoType> map(String sender, String code) {
    switch (sender) {
      case Const.ARTE_DE:
        return mapGerman(code);
      case ArteConstants.ARTE_EN:
        return mapEnglish(code);
      case ArteConstants.ARTE_ES:
        return mapSpanish(code);
      case Const.ARTE_FR:
        return mapFrench(code);
      case ArteConstants.ARTE_IT:
        return mapItalian(code);
      case ArteConstants.ARTE_PL:
        return mapPolish(code);
      default:
        LOG.debug("unknown sender: {}", sender);
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapSpanish(String code) {
    switch (code) {
      case "VE[ESP]-STE[ESP]":
      case "VO-STE[ESP]":
      case "VOA-STE[ESP]":
      case "VOEU-STE[ESP]":
      case "VOF-STE[ESP]":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        if (code.contains("ESP")) {
          LOG.debug("add spanish: {}", code);
        }
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapEnglish(String code) {
    switch (code) {
      case "VE[ANG]":
        return Optional.of(ArteVideoType.DEFAULT);
      case "VE[ANG]-STE[ANG]":
      case "VO-STE[ANG]":
      case "VOA-STE[ANG]":
      case "VOEU-STE[ANG]":
      case "VOF-STE[ANG]":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        if (code.contains("ANG")) {
          LOG.debug("add english: {}", code);
        }
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapFrench(String code) {
    switch (code) {
      case "VF":
      case "VF-STF":
      case "VOF":
      case "VOF-STF":
        return Optional.of(ArteVideoType.DEFAULT);
      case "VF-STMF":
      case "VOF-STMF":
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
      case "VA":
      case "VA-STA":
      case "VOA":
      case "VOA-STA":
        return Optional.of(ArteVideoType.DEFAULT);
      case "VA-STMA":
      case "VOA-STMA":
        return Optional.of(ArteVideoType.SUBTITLE_INCLUDED);
      case "VAAUD":
        return Optional.of(ArteVideoType.AUDIO_DESCRIPTION);
      case "VO":
        return Optional.of(ArteVideoType.ORIGINAL);
      case "VO-STA":
      case "VOA-STF":
      case "VOF-STA":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapItalian(String code) {
    switch (code) {
      case "VE[ITA]-STE[ITA]":
      case "VO-STE[ITA]":
      case "VOA-STE[ITA]":
      case "VOEU-STE[ITA]":
      case "VOF-STE[ITA]":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapPolish(String code) {
    switch (code) {
      case "VE[POL]-STE[POL]":
      case "VO-STE[POL]":
      case "VOA-STE[POL]":
      case "VOEU-STE[POL]":
      case "VOF-STE[POL]":
        return Optional.of(ArteVideoType.ORIGINAL_WITH_SUBTITLE);
      default:
        return Optional.empty();
    }
  }
}
