package mServer.crawler.sender.livestream.tasks;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.orf.TopicUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LivestreamToFilmTask extends AbstractRecursivConverterTask<DatenFilm, TopicUrlDTO> {
  private static final long serialVersionUID = -2729059830661477520L;
  private static final Logger LOG = LogManager.getLogger(LivestreamToFilmTask.class);

  // REFERENCES
  // lookup name, a SenderInfo Object ( Sender, Title, useThisStream)
  private static final Hashtable<String, SenderInfo> senderLookup = new Hashtable<>();

  static {
    senderLookup.put("3sat im Livestream", new SenderInfo(Const.DREISAT, "3Sat im Livestream", false));
    senderLookup.put("3sat Livestream", new SenderInfo(Const.DREISAT, "3Sat Livestream", true));
    senderLookup.put("ARD-alpha Livestream", new SenderInfo("ARDalpha", "ARD Alpha Livestream", true));
    senderLookup.put("arte im Livestream", new SenderInfo(Const.ARTE_DE, "ARTE.DE Livestream", false));
    senderLookup.put("ARTE Livestream französisch", new SenderInfo(Const.ARTE_FR, "ARTE.FR Livestream", true));
    senderLookup.put("ARTE Livestream", new SenderInfo(Const.ARTE_DE, "ARTE.DE Livestream", true));
    senderLookup.put("BR Fernsehen Nord", new SenderInfo(Const.BR, "BR Nord Livestreamn", true));
    senderLookup.put("BR Fernsehen Süd", new SenderInfo(Const.BR, "BR Süd Livestreamn", true));
    senderLookup.put("Das Erste", new SenderInfo(Const.ARD, "ARD Livestreamn", true));
    senderLookup.put("Das ZDF im Livestream", new SenderInfo(Const.ZDF, "ZDF Livestream", true));
    senderLookup.put("Deutsche Welle (DW) - Die mediale Stimme Deutschlands", new SenderInfo(Const.DW, "DW Livestreamn", true));
    senderLookup.put("hr-fernsehen", new SenderInfo(Const.HR, "HR Livestreamn", true));
    senderLookup.put("KiKA im Livestream", new SenderInfo(Const.KIKA, "KiKA im Livestream", false));
    senderLookup.put("KiKA Livestream", new SenderInfo(Const.KIKA, "KiKA Livestream", true));
    senderLookup.put("MDR Fernsehen Sachsen", new SenderInfo(Const.MDR, "MDR Fernsehen Sachsen Livestream", true));
    senderLookup.put("MDR Fernsehen Sachsen-Anhalt", new SenderInfo(Const.MDR, "MDR Sachsen-Anhalt Livestream", true));
    senderLookup.put("MDR Fernsehen Thüringen", new SenderInfo(Const.MDR, "MDR Thüringen Livestream", true));
    senderLookup.put("NDR Fernsehen Hamburg", new SenderInfo(Const.NDR, "NDR Hamburg Livestream", true));
    senderLookup.put("NDR Fernsehen Mecklenburg-Vorpommern", new SenderInfo(Const.NDR, "NDR Mecklenburg-Vorpommern Livestream", true));
    senderLookup.put("NDR Fernsehen Schleswig-Holstein", new SenderInfo(Const.NDR, "NDR Schleswig-Holstein Livestream", true));
    senderLookup.put("NDR Fernsehen", new SenderInfo(Const.NDR, "NDR Livestream", true));
    senderLookup.put("ONE im Livestream", new SenderInfo("ONE", "ONE Livestream", true));
    senderLookup.put("ORF 1", new SenderInfo(Const.ORF, "ORF 1 Livestream", true));
    senderLookup.put("ORF 2", new SenderInfo(Const.ORF, "ORF 2 Livestream", true));
    senderLookup.put("ORF 3", new SenderInfo(Const.ORF, "ORF 3 Livestream", true));
    senderLookup.put("ORF Sport", new SenderInfo(Const.ORF, "ORF Sport Livestream", true));
    senderLookup.put("Phoenix im Livestream", new SenderInfo(Const.PHOENIX, "Phoenix Livestream", false));
    senderLookup.put("phoenix live", new SenderInfo(Const.PHOENIX, "Phoenix Livestream", true));
    senderLookup.put("rbb Fernsehen Berlin", new SenderInfo(Const.RBB, "RBB Berlin Livestream", true));
    senderLookup.put("rbb Fernsehen Brandenburg", new SenderInfo(Const.RBB, "RBB Brandenburg Livestream", true));
    senderLookup.put("SR Livestream", new SenderInfo(Const.SR, "SR Livestream", true));
    senderLookup.put("SRF 1", new SenderInfo(Const.SRF, "SRF 1 Livestream", true));
    senderLookup.put("SRF info", new SenderInfo(Const.SRF, "SRF info Livestream", true));
    senderLookup.put("SRF zwei", new SenderInfo(Const.SRF, "SRF zwei Livestream", true));
    senderLookup.put("SWR Baden-Württemberg", new SenderInfo(Const.SWR, "SWR BW Livestream", true));
    senderLookup.put("SWR Fernsehen Rheinland-Pfalz", new SenderInfo(Const.SWR, "SWR RP Livestream", true));
    senderLookup.put("tagesschau24-Livestream", new SenderInfo("tagesschau24", "tagesschau24 Livestream", true));
    senderLookup.put("WDR Fernsehen im Livestream", new SenderInfo(Const.WDR, "WDR Livestream", true));
    senderLookup.put("ZDFinfo im Livestream", new SenderInfo(Const.ZDF, "ZDF.info Livestream", true));
    senderLookup.put("ZDFneo im Livestream", new SenderInfo(Const.ZDF, "ZDF.öneo Livestream", true));
  }

  public LivestreamToFilmTask(MediathekReader crawler, ConcurrentLinkedQueue<TopicUrlDTO> topicUrlDTOS) {
    super(crawler, topicUrlDTOS);
  }

  @Override
  protected void processElement(TopicUrlDTO aElement) {
    SenderInfo info = senderLookup.get(aElement.getTopic());
    if (info != null && info.isPreferable()) {

      final DatenFilm film = new DatenFilm(info.getSender(),
              "Livestream",
              "",
              info.getName(),
              aElement.getUrl(),
              "",
              "",
              "",
              0L,
              "");

      taskResults.add(film);
    }
    if (info == null) {
      LOG.error("Unknown LIVESTREAM \"{}\"", aElement.getTopic());
    }
  }

  @Override
  protected AbstractRecursivConverterTask<DatenFilm, TopicUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<TopicUrlDTO> aElementsToProcess) {
    return new LivestreamToFilmTask(crawler, aElementsToProcess);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return Integer.MAX_VALUE;
  }

  static class SenderInfo {
    private String sender;
    private String name;
    private boolean preferable;

    public SenderInfo(String sender, String name, boolean preferable) {
      super();
      this.sender = sender;
      this.name = name;
      this.preferable = preferable;
    }

    public String getSender() {
      return sender;
    }

    public String getName() {
      return name;
    }

    public boolean isPreferable() {
      return preferable;
    }
  }
}
