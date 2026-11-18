package de.mediathekview.mserver.crawler.livestream;

import java.util.HashMap;
import java.util.Map;

import de.mediathekview.mlib.daten.Sender;

public final class LivestreamConstants {

  /** Livestreams */
  public static final String URL_LIVESTREAMS = "http://bit.ly/kn-kodi-tv";
  
  public static final Map<String,Sender> sender = new HashMap<>();
  
  static {
    sender.put("3sat", Sender.DREISAT);
    sender.put("Das Erste HD", Sender.ARD);
    sender.put("one HD", Sender.ARD);
    sender.put("ARD-alpha", Sender.ARD);
    sender.put("tagesschau24", Sender.ARD);
    sender.put("ARTE HD", Sender.ARTE_DE);
    sender.put("ARTE HD (FR)", Sender.ARTE_FR);
    sender.put("WDR HD", Sender.WDR);
    sender.put("SWR Baden-Württemberg HD", Sender.SWR);
    sender.put("SWR Rheinland-Pfalz HD", Sender.SWR);
    sender.put("NDR Niedersachsen HD", Sender.NDR);
    sender.put("NDR Schleswig-Holstein HD", Sender.NDR);
    sender.put("NDR Mecklenburg-Vorpommern HD", Sender.NDR);
    sender.put("NDR Hamburg HD", Sender.NDR);
    sender.put("BR Fernsehen Nord HD", Sender.BR);
    sender.put("BR Fernsehen Süd HD", Sender.BR);
    sender.put("MDR Sachsen HD", Sender.MDR);
    sender.put("MDR Sachsen-Anhalt HD", Sender.MDR);
    sender.put("MDR Thüringen HD", Sender.MDR);
    sender.put("hr-fernsehen", Sender.HR);
    sender.put("rbb Berlin HD", Sender.RBB);
    sender.put("rbb Brandenburg HD", Sender.RBB);
    sender.put("SR Fernsehen HD", Sender.SR);
    sender.put("Deutsche Welle", Sender.DW);
    sender.put("Deutsche Welle+", Sender.DW);
    sender.put("Deutsche Welle (EN)", Sender.DW);
    sender.put("Deutsche Welle (ES)", Sender.DW);
    sender.put("phoenix HD", Sender.PHOENIX);
    sender.put("Radio Bremen TV", Sender.RBTV);
    sender.put("rbb Berlin", Sender.RBB);
    sender.put("rbb Brandenburg", Sender.RBB);
    sender.put("KiKA", Sender.KIKA);
    sender.put("ZDF HD", Sender.ZDF);
    sender.put("ZDFneo HD", Sender.ZDF);
    sender.put("ZDFinfo HD", Sender.ZDF);
    sender.put("ORF eins HD", Sender.ORF);
    sender.put("ORF 2 HD", Sender.ORF);
    sender.put("ORF III HD", Sender.ORF);
    sender.put("ORF SPORT +", Sender.ORF);
  };
 
  private LivestreamConstants() {}
}
