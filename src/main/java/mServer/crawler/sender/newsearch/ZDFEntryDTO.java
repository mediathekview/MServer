package mServer.crawler.sender.newsearch;

import java.io.Serializable;

/**
 * A Data-Transfer-Object to transfer the URL with the general informations and the download informations.
 */
public class ZDFEntryDTO implements Serializable
{
    private static final String BASE_URL = "https://api.zdf.de";
    private String entryGeneralInformationsUrl; //canonical
    private String entryDownloadInformationsUrl; //ptmd-template

    public ZDFEntryDTO(final String entryGeneralInformationsUrl, final String entryDownloadInformationsUrl)
    {
        setEntryGeneralInformationsUrl(entryGeneralInformationsUrl);
        setEntryDownloadInformationsUrl(entryDownloadInformationsUrl);
    }

    public String getEntryGeneralInformationsUrl()
    {
        return entryGeneralInformationsUrl;
    }

    public void setEntryGeneralInformationsUrl(final String aEntryInformationsUrl)
    {
        if (aEntryInformationsUrl.contains(BASE_URL))
        {
            entryGeneralInformationsUrl = aEntryInformationsUrl;
        } else
        {
            entryGeneralInformationsUrl = BASE_URL + aEntryInformationsUrl;
        }
    }

    public String getEntryDownloadInformationsUrl()
    {
        return entryDownloadInformationsUrl;
    }

    public void setEntryDownloadInformationsUrl(final String aEntryDownloadInformationsUrl)
    {
        if (aEntryDownloadInformationsUrl.contains(BASE_URL))
        {
            entryDownloadInformationsUrl = aEntryDownloadInformationsUrl;
        } else
        {
            entryDownloadInformationsUrl = BASE_URL + aEntryDownloadInformationsUrl;
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ZDFEntryDTO that = (ZDFEntryDTO) o;

        if (getEntryGeneralInformationsUrl() != null ? !getEntryGeneralInformationsUrl().equals(that.getEntryGeneralInformationsUrl()) : that.getEntryGeneralInformationsUrl() != null)
            return false;
        return getEntryDownloadInformationsUrl() != null ? getEntryDownloadInformationsUrl().equals(that.getEntryDownloadInformationsUrl()) : that.getEntryDownloadInformationsUrl() == null;

    }

    @Override
    public int hashCode()
    {
        int result = getEntryGeneralInformationsUrl() != null ? getEntryGeneralInformationsUrl().hashCode() : 0;
        result = 31 * result + (getEntryDownloadInformationsUrl() != null ? getEntryDownloadInformationsUrl().hashCode() : 0);
        return result;
    }
}
