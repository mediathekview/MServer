package mServer.crawler.sender.newsearch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A Data-Transfer-Object to transfer the ZDF configuration information.
 */
public class ZDFConfigurationDTO implements Serializable {
    private static final long serialVersionUID = -445386435734116784L;
    private final Map<ZDFClient.ZDFClientMode, String> apiToken = new HashMap<>();

    public String getApiToken(ZDFClient.ZDFClientMode aClientMode) {
        return apiToken.getOrDefault(aClientMode, "");
    }

    public void setApiToken(ZDFClient.ZDFClientMode aClientMode, final String aApiToken) {
        apiToken.put(aClientMode, aApiToken);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ZDFConfigurationDTO that = (ZDFConfigurationDTO) o;

        return getApiToken(ZDFClient.ZDFClientMode.SEARCH) != null ? getApiToken(ZDFClient.ZDFClientMode.SEARCH).equals(that.getApiToken(ZDFClient.ZDFClientMode.SEARCH)) : that.getApiToken(ZDFClient.ZDFClientMode.SEARCH) == null;
    }

    @Override
    public int hashCode() {
        return getApiToken(ZDFClient.ZDFClientMode.SEARCH) != null ? getApiToken(ZDFClient.ZDFClientMode.SEARCH).hashCode() : 0;
    }

}
