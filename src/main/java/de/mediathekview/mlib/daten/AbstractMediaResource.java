package de.mediathekview.mlib.daten;

import de.mediathekview.mserver.base.utils.TextCleaner;
import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public abstract class AbstractMediaResource<T extends Serializable> implements Serializable {
  private static final long serialVersionUID = -6404888306701549134L;
  private Map<Resolution, T> urls;
  private UUID uuid; // Old: filmNr
  private Sender sender;
  private LocalDateTime time;
  private Collection<GeoLocations> geoLocations;
  private String titel;
  private String thema;
  private String beschreibung;
  private URL website;

  /** DON'T USE! - ONLY FOR GSON! */
  AbstractMediaResource() {
    geoLocations = new ArrayList<>();
    urls = new EnumMap<>(Resolution.class);
    uuid = null;
    sender = null;
    time = null;
    website = null;
    beschreibung = "";
  }

  protected AbstractMediaResource(
      final UUID aUuid,
      final Sender aSender,
      final String aTitel,
      final String aThema,
      final LocalDateTime aTime) {
    geoLocations = new ArrayList<>();
    urls = new EnumMap<>(Resolution.class);
    uuid = aUuid;
    if (aSender == null) {
      throw new IllegalArgumentException("The sender can't be null!");
    }
    sender = aSender;
    setTitel(aTitel);
    setThema(aThema);
    time = aTime;
    website = null;

    beschreibung = "";
  }

  protected AbstractMediaResource(final AbstractMediaResource<T> copyObj) {
    super();
    uuid = copyObj.uuid;
    geoLocations = copyObj.geoLocations;
    urls = copyObj.urls;
    sender = copyObj.sender;
    titel = copyObj.titel;
    thema = copyObj.thema;
    time = copyObj.time;
    beschreibung = copyObj.beschreibung;
    website = copyObj.website;
  }

  public AbstractMediaResource<T> merge(final AbstractMediaResource<T> objToMergeWith) {
    addAllGeoLocations(objToMergeWith.getGeoLocations());
    addAllUrls(
        objToMergeWith.getUrls().entrySet().stream()
            .filter(urlEntry -> !urls.containsKey(urlEntry.getKey()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
    return this;
  }

  public void addAllGeoLocations(final Collection<GeoLocations> aGeoLocations) {
    geoLocations.addAll(aGeoLocations);
  }

  public void addAllUrls(final Map<Resolution, T> urlMap) {
    urls.putAll(urlMap);
  }

  public void addGeolocation(final GeoLocations aGeoLocation) {
    geoLocations.add(aGeoLocation);
  }

  public void addUrl(final Resolution aQuality, final T aUrl) {
    if (aQuality != null && aUrl != null) {
      urls.put(aQuality, aUrl);
    }
  }

  public void addUrlIfAbsent(final Resolution aQuality, final T aUrl) {
    if (aQuality != null && aUrl != null) {
      urls.putIfAbsent(aQuality, aUrl);
    }
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AbstractMediaResource<T> other = (AbstractMediaResource<T>) obj;
    if (sender != other.sender) {
      return false;
    }
    if (thema == null) {
      if (other.thema != null) {
        return false;
      }
    } else if (!thema.equals(other.thema)) {
      return false;
    }
    if (titel == null) {
      return other.titel == null;
    } else {
      return titel.equals(other.titel);
    }
  }

  public String getBeschreibung() {
    return beschreibung;
  }

  public void setBeschreibung(final String aBeschreibung) {
    beschreibung = TextCleaner.shortAndCleanBeschreibung(aBeschreibung, titel, thema);
  }

  public void setBeschreibungRaw(final String aBeschreibung) {
    beschreibung = aBeschreibung;
  }
  
  public Optional<T> getDefaultUrl() {
    if (urls.containsKey(Resolution.NORMAL)) {
      return Optional.of(getUrl(Resolution.NORMAL));
    }
    final Iterator<Entry<Resolution, T>> entryIterator = urls.entrySet().iterator();
    if (entryIterator.hasNext()) {
      return Optional.of(entryIterator.next().getValue());
    }
    return Optional.empty();
  }

  public Collection<GeoLocations> getGeoLocations() {
    return new ArrayList<>(geoLocations);
  }

  public void setGeoLocations(final Collection<GeoLocations> aGeoLocations) {
    geoLocations = aGeoLocations;
  }

  public Sender getSender() {
    return sender;
  }

  public void setSender(Sender sender) {
    this.sender = sender;
  }

  public String getSenderName() {
    return sender.getName();
  }

  public String getThema() {
    return thema;
  }

  public void setThema(final String aThema) {
    thema = TextCleaner.clean(aThema);
  }
  
  public void setThemaRaw(final String aThema) {
    thema = aThema;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  public String getTitel() {
    return titel;
  }

  public void setTitel(final String aTitel) {
    titel = TextCleaner.clean(aTitel);
  }
  
  public void setTitelRaw(final String aTitel) {
    titel = aTitel;
  }

  public T getUrl(final Resolution aQuality) {
    return urls.get(aQuality);
  }

  public Map<Resolution, T> getUrls() {
    if (urls.isEmpty()) {
      return new EnumMap<>(Resolution.class);
    }
    return new EnumMap<>(urls);
  }

  public void setUrls(Map<Resolution, T> urls) {
    this.urls = urls;
  }

  protected Map<Resolution, T> getUrlsDirect() {
    return urls;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public Optional<URL> getWebsite() {
    return Optional.ofNullable(website);
  }

  public void setWebsite(final URL aWebsite) {
    website = aWebsite;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (sender == null ? 0 : sender.hashCode());
    result = prime * result + (thema == null ? 0 : thema.hashCode());
    result = prime * result + (titel == null ? 0 : titel.hashCode());
    return result;
  }

  public boolean hasHD() {
    return urls.containsKey(Resolution.HD);
  }

  @Override
  public String toString() {
    return "AbstractMediaResource{"
        + "urls="
        + urls
        + ", uuid="
        + uuid
        + ", sender="
        + sender
        + ", time="
        + time
        + ", geoLocations="
        + geoLocations
        + ", titel='"
        + titel
        + '\''
        + ", thema='"
        + thema
        + '\''
        + ", beschreibung='"
        + beschreibung
        + '\''
        + ", website="
        + website
        + '}';
  }
}
