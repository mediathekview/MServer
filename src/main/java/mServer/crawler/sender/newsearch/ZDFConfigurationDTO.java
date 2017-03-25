package mServer.crawler.sender.newsearch;

import java.io.Serializable;

/**
 * A Data-Transfer-Object to transfer the ZDF configuration information.
 */
public class ZDFConfigurationDTO implements Serializable {
    private static final long serialVersionUID = -445386435734116784L;
    private String apiToken;

    /**
     * @param aApiToken The ZDF api authentication token.
     */
    public ZDFConfigurationDTO(final String aApiToken) {
        setApiToken(aApiToken);
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(final String aApiToken) {
        apiToken = aApiToken;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ZDFConfigurationDTO that = (ZDFConfigurationDTO) o;

        return getApiToken() != null ? getApiToken().equals(that.getApiToken()) : that.getApiToken() == null;
    }

    @Override
    public int hashCode() {
        return getApiToken() != null ? getApiToken().hashCode() : 0;
    }

}
