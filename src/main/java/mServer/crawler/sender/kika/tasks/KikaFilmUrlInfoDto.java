package mServer.crawler.sender.kika.tasks;

import mServer.crawler.sender.base.FilmUrlInfoDto;

import java.util.Objects;

public class KikaFilmUrlInfoDto extends FilmUrlInfoDto {

    private final String profileName;
    private long size = 0;

    public KikaFilmUrlInfoDto(String aUrl, String aProfileName) {
        super(aUrl);
        profileName = aProfileName;
    }

    public KikaFilmUrlInfoDto(String aUrl, int aWidth, int aHeight, String profileName) {
        super(aUrl, aWidth, aHeight);
        this.profileName = profileName;
    }

    public long getSize() {
      return size;
    }
    
    public void setSize(long size) {
      this.size = size;
    }
    
    public String getProfileName() {
        return profileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        KikaFilmUrlInfoDto that = (KikaFilmUrlInfoDto) o;
        return Objects.equals(profileName, that.profileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), profileName);
    }
}
