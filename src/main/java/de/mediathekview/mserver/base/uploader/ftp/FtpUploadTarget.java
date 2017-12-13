package de.mediathekview.mserver.base.uploader.ftp;

import java.util.Optional;

import de.mediathekview.mserver.base.uploader.UploadTarget;

public class FtpUploadTarget implements UploadTarget
{
    private Optional<String> username;
    private Optional<String> password;
    private Optional<Integer> port;
    private final String serverUrl;
    private final String targetPath;

    public FtpUploadTarget(final String aServerUrl, final String aTargetPath)
    {
        super();
        serverUrl = aServerUrl;
        targetPath = aTargetPath;

        username = Optional.empty();
        password = Optional.empty();
        port = Optional.empty();
    }

    public Optional<String> getUsername()
    {
        return username;
    }

    public void setUsername(final Optional<String> aUsername)
    {
        username = aUsername;
    }

    public Optional<String> getPassword()
    {
        return password;
    }

    public void setPassword(final Optional<String> aPassword)
    {
        password = aPassword;
    }

    public String getServerUrl()
    {
        return serverUrl;
    }

    public String getTargetPath()
    {
        return targetPath;
    }

    public Optional<Integer> getPort()
    {
        return port;
    }

    public void setPort(final Optional<Integer> aPort)
    {
        port = aPort;
    }

}
