package de.mediathekview.mserver.crawler.ard.json;

import de.mediathekview.mlib.daten.Sender;

/**
 *  Basic information from http://www.ardmediathek.de/play/sola/[documentId].
 */
public class ArdBasicInfoDTO
{
    private String title;
    private String thema;
    private String senderName;

    public ArdBasicInfoDTO()
    {
        title="";
        thema="";
        senderName= Sender.ARD.getName();
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(final String title)
    {
        this.title = title;
    }

    public String getThema()
    {
        return thema;
    }

    public void setThema(final String thema)
    {
        this.thema = thema;
    }

    public String getSenderName()
    {
        return senderName;
    }

    public void setSenderName(final String senderName)
    {
        this.senderName = senderName;
    }
}
