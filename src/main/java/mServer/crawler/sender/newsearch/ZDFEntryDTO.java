package mServer.crawler.sender.newsearch;

import java.io.Serializable;
import java.util.Objects;

/**
 * A Data-Transfer-Object to transfer the URL with the general information and the download
 * information.
 */
public final class ZDFEntryDTO implements Serializable {
  private static final long serialVersionUID = 1L;
  private final String apiBaseUrl;
  private String tvService;
  private String entryGeneralInformationUrl; // canonical
  private String entryDownloadInformationUrl; // ptmd-template

  public ZDFEntryDTO(
      String aApiBaseUrl,
      final String entryGeneralInformationUrl,
      final String entryDownloadInformationUrl,
      String tvService) {
    apiBaseUrl = aApiBaseUrl;
    setEntryGeneralInformationUrl(entryGeneralInformationUrl);
    setEntryDownloadInformationUrl(entryDownloadInformationUrl);
    setTvService(tvService);
  }

  public String getEntryGeneralInformationUrl() {
    return entryGeneralInformationUrl;
  }

  public void setEntryGeneralInformationUrl(final String aEntryInformationsUrl) {
    if (aEntryInformationsUrl.contains(apiBaseUrl)) {
      entryGeneralInformationUrl = aEntryInformationsUrl;
    } else {
      entryGeneralInformationUrl = apiBaseUrl + aEntryInformationsUrl;
    }
  }

  public String getEntryDownloadInformationUrl() {
    return entryDownloadInformationUrl;
  }

  public void setEntryDownloadInformationUrl(final String aEntryDownloadInformationsUrl) {
    if (aEntryDownloadInformationsUrl.contains(apiBaseUrl)) {
      entryDownloadInformationUrl = aEntryDownloadInformationsUrl;
    } else {
      entryDownloadInformationUrl = apiBaseUrl + aEntryDownloadInformationsUrl;
    }
  }

    public String getTvService() {
        return tvService;
    }

    public void setTvService(String tvService) {
        this.tvService = tvService;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZDFEntryDTO that = (ZDFEntryDTO) o;
        return Objects.equals(apiBaseUrl, that.apiBaseUrl) &&
                Objects.equals(getTvService(), that.getTvService()) &&
                Objects.equals(getEntryGeneralInformationUrl(), that.getEntryGeneralInformationUrl()) &&
                Objects.equals(getEntryDownloadInformationUrl(), that.getEntryDownloadInformationUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiBaseUrl, getTvService(), getEntryGeneralInformationUrl(), getEntryDownloadInformationUrl());
    }
}
