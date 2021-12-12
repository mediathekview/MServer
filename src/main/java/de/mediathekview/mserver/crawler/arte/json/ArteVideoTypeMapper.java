package de.mediathekview.mserver.crawler.arte.json;

import de.mediathekview.mlib.daten.Sender;
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
      case ARTE_FR:
        return mapFrench(code);
      default:
        LOG.debug("unknown sender: {}", sender);
        return Optional.empty();
    }
  }

  private static Optional<ArteVideoType> mapFrench(String code) {
    switch (code) {
      case "VF-STF":
        return Optional.of(ArteVideoType.DEFAULT);
      case "VF-STMF":
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
      case "VA", "VA-STA": // deutsch (synchronisiert)
        return Optional.of(ArteVideoType.DEFAULT);
      case "VA-STMA":
        return Optional.of(ArteVideoType.SUBTITLE_INCLUDED);
      case "VAAUD":
        return Optional.of(ArteVideoType.AUDIO_DESCRIPTION);
      case "VO":
        return Optional.of(ArteVideoType.ORIGINAL);
      default:
        return Optional.empty();
    }
  }
}
