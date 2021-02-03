package mServer.crawler.sender.kika.tasks;

import com.google.common.collect.ComparisonChain;

import java.util.Comparator;
import java.util.Objects;

public class KikaFilmUrlInfoComparator implements Comparator<KikaFilmUrlInfoDto> {
  @Override
  public int compare(KikaFilmUrlInfoDto o1, KikaFilmUrlInfoDto o2) {
    if (Objects.equals(o1, o2)) {
      return 0;
    }

    return ComparisonChain.start()
        .compare(o1.getWidth(), o2.getWidth())
        .compare(o1.getProfileName(), o2.getProfileName())
        .result();
  }
}
