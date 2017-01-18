package mServer.crawler.sender.newsearch;

import java.io.Serializable;

/**
 * A Data-Transfer-Object to transfer the URL with the general information and the download information.
 */
public final class ZDFEntryDTO implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static final String BASE_URL = "https://api.zdf.de";
    
    private String entryGeneralInformationUrl; //canonical
    private String entryDownloadInformationUrl; //ptmd-template

    public ZDFEntryDTO(final String entryGeneralInformationUrl, final String entryDownloadInformationUrl)
    {
        setEntryGeneralInformationUrl(entryGeneralInformationUrl);
        setEntryDownloadInformationUrl(entryDownloadInformationUrl);
    }

    public String getEntryGeneralInformationUrl()
    {
        return entryGeneralInformationUrl;
    }

    public void setEntryGeneralInformationUrl(final String aEntryInformationsUrl)
    {
        if (aEntryInformationsUrl.contains(BASE_URL))
        {
            entryGeneralInformationUrl = aEntryInformationsUrl;
        } else
        {
            entryGeneralInformationUrl = BASE_URL + aEntryInformationsUrl;
        }
    }

    public String getEntryDownloadInformationUrl()
    {
        return entryDownloadInformationUrl;
    }

    public void setEntryDownloadInformationUrl(final String aEntryDownloadInformationsUrl)
    {
        if (aEntryDownloadInformationsUrl.contains(BASE_URL))
        {
            entryDownloadInformationUrl = aEntryDownloadInformationsUrl;
        } else
        {
            entryDownloadInformationUrl = BASE_URL + aEntryDownloadInformationsUrl;
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ZDFEntryDTO that = (ZDFEntryDTO) o;

        if (getEntryGeneralInformationUrl() != null ? !getEntryGeneralInformationUrl().equals(that.getEntryGeneralInformationUrl()) : that.getEntryGeneralInformationUrl() != null)
            return false;
        return getEntryDownloadInformationUrl() != null ? getEntryDownloadInformationUrl().equals(that.getEntryDownloadInformationUrl()) : that.getEntryDownloadInformationUrl() == null;

    }

    @Override
    public int hashCode()
    {
        int result = getEntryGeneralInformationUrl() != null ? getEntryGeneralInformationUrl().hashCode() : 0;
        result = 31 * result + (getEntryDownloadInformationUrl() != null ? getEntryDownloadInformationUrl().hashCode() : 0);
        return result;
    }
}
