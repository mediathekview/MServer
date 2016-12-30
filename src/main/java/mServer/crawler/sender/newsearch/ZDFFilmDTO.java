package mServer.crawler.sender.newsearch;

import java.io.Serializable;

/**
 * Created by nicklas on 30.12.16.
 */
public class ZDFFilmDTO implements Serializable
{
    public static final String BASE_URL = "https://api.zdf.de";
    private int duration;
    private String geoLocation;
    private String videoInformationUrl; //ptmd-template
    private String title;
    private String altText;
    private String caption;

    public ZDFFilmDTO()
    {
        super();
    }

    public ZDFFilmDTO(final int duration, final String geoLocation, final String videoInformationUrl, final String title, final String altText, final String caption)
    {
        this.duration = duration;
        this.geoLocation = geoLocation;
        setVideoInformationUrl(videoInformationUrl);

        this.title = title;
        this.altText = altText;
        this.caption = caption;
    }

    public int getDuration()
    {
        return duration;
    }

    public void setDuration(final int duration)
    {
        this.duration = duration;
    }

    public String getGeoLocation()
    {
        return geoLocation;
    }

    public void setGeoLocation(final String geoLocation)
    {
        this.geoLocation = geoLocation;
    }

    public String getVideoInformationUrl()
    {
        return videoInformationUrl;
    }

    public void setVideoInformationUrl(final String aVideoInformationUrl)
    {
        if (aVideoInformationUrl.contains(BASE_URL))
        {
            videoInformationUrl = aVideoInformationUrl;
        } else
        {
            videoInformationUrl = BASE_URL + aVideoInformationUrl;
        }
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(final String title)
    {
        this.title = title;
    }

    public String getAltText()
    {
        return altText;
    }

    public void setAltText(final String altText)
    {
        this.altText = altText;
    }

    public String getCaption()
    {
        return caption;
    }

    public void setCaption(final String caption)
    {
        this.caption = caption;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ZDFFilmDTO that = (ZDFFilmDTO) o;

        if (getDuration() != that.getDuration()) return false;
        if (getGeoLocation() != null ? !getGeoLocation().equals(that.getGeoLocation()) : that.getGeoLocation() != null)
            return false;
        if (getVideoInformationUrl() != null ? !getVideoInformationUrl().equals(that.getVideoInformationUrl()) : that.getVideoInformationUrl() != null)
            return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null) return false;
        if (getAltText() != null ? !getAltText().equals(that.getAltText()) : that.getAltText() != null) return false;
        return getCaption() != null ? getCaption().equals(that.getCaption()) : that.getCaption() == null;

    }

    @Override
    public int hashCode()
    {
        int result = getDuration();
        result = 31 * result + (getGeoLocation() != null ? getGeoLocation().hashCode() : 0);
        result = 31 * result + (getVideoInformationUrl() != null ? getVideoInformationUrl().hashCode() : 0);
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getAltText() != null ? getAltText().hashCode() : 0);
        result = 31 * result + (getCaption() != null ? getCaption().hashCode() : 0);
        return result;
    }
}
