package de.mediathekview.mserver.crawler.br.json;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import de.mediathekview.mserver.crawler.br.data.BrID;

public class BrIdsDTO {
  private final Set<BrID> ids;

  public BrIdsDTO() {
    super();
    ids = ConcurrentHashMap.newKeySet();
  }

  public boolean add(final BrID id) {
    return ids.add(id);
  }

  public Set<BrID> getIds() {
    return new ConcurrentSkipListSet<>(ids);
  }


}
