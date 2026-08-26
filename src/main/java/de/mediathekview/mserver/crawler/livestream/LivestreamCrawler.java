package de.mediathekview.mserver.crawler.livestream;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LivestreamCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(LivestreamCrawler.class);

  public LivestreamCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.WDR3;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    KodiM3UParser m3uparser = new KodiM3UParser();
    String data = "";
    Set<Film> channels = new HashSet<>();
    try {
      data = this.getConnection().requestBodyAsString(LivestreamConstants.URL_LIVESTREAMS);
      List<HashMap<String, String>> streamInfo = m3uparser.parse(data);
      for (HashMap<String, String> stream : streamInfo) {
        if (LivestreamConstants.sender.containsKey(stream.get("tvg-name"))) {
          Film f = new Film(
              UUID.randomUUID(), 
              LivestreamConstants.sender.get(stream.get("tvg-name")),
              stream.get("name") + " Livestream",
              "Livestream", 
              null, null);
          f.addUrl(Resolution.NORMAL, new FilmUrl(new URL(stream.get("url")), 0L));
          channels.add(f);
        }
      }
      //System.out.println("################################");
      //for (HashMap<String, String> stream : streamInfo) {
      //  System.out.println(stream.get("tvg-name") + "#" + stream.get("name") + "#" + stream.get("url"));
      //}
    } catch (final IOException e) {
      LOG.fatal("Exception in {} crawler.", getSender().getName(), e);
    }
    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), channels.size());
    getAndSetMaxCount(channels.size());
    return new LivestreamToFilm(channels);
  }
  
  // TODO: move to class when stable
  class LivestreamToFilm extends RecursiveTask<Set<Film>> {
    private static final long serialVersionUID = 1L;
    Set<Film> setOfFilm;
    LivestreamToFilm (Set<Film> setOfFilm) {
      this.setOfFilm = setOfFilm;
    }
    
    @Override
    protected Set<Film> compute() {
      return setOfFilm;
    }
    
  }

  // TODO: move to class when stable
  class KodiM3UParser {
    //#EXTM3U
    //#EXTINF:-1 tvg-name="Das Erste HD" tvg-id="DasErste.de" group-title="IPTV-DE" tvg-logo="https://raw.githubusercontent.com/jnk22/kodinerds-iptv/master/logos/tv/daserstehd.png",Das Erste HD
    //https://mcdn.daserste.de/daserste/de/master.m3u8    
    List<HashMap<String, String>> parse(String aM3U8Data) {
      String[] lines = StringUtils.split(aM3U8Data, '\n');
      ArrayList<HashMap<String, String>> data = new ArrayList<>();
      HashMap<String, String> information = new HashMap<>();
      for (String line : lines) {
        if (!line.isBlank() && !line.equalsIgnoreCase("#EXTM3U") && !line.equalsIgnoreCase("#")) {
          if (line.startsWith("#EXTINF")) {
            // remove extint
            line = line.substring(8);
            // read name (last position after ,)
            information.put("name", line.substring(line.lastIndexOf(",")+1));
            // remove name but add space for parsing
            line = line.substring(0,line.lastIndexOf(",")) + " ";
            // remove length (first position)
            information.put("length", line.substring(0,line.indexOf(" ")));
            line = line.substring(line.indexOf(" ")+1);
            // bag
            String[] dictList = line.split("\" ");
            for (String valueKeyPair : dictList) {
              String key = valueKeyPair.substring(0,valueKeyPair.indexOf("="));
              String value = valueKeyPair.substring(valueKeyPair.indexOf("=")+1);
              value = value.replace("\"", "");
              information.put(key.trim(),value.trim());
            }
          } else {
            information.put("url", line);
            data.add(information);
            information = new HashMap<>();
          }
        }
      }
      return data;
    }
    
    
  }
}
