package de.mediathekview.mserver.base.config;

import java.util.Map;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;

public class MServerFTPSettings {

	private Boolean ftpEnabled;
	private String ftpUrl;
	private Map<FilmlistFormats, String> ftpTargetFilePaths;
	private Integer ftpPort;
	private String ftpUsername;
	private String ftpPassword;

	public Boolean getFtpEnabled() {
		return ftpEnabled;
	}

	public void setFtpEnabled(Boolean ftpEnabled) {
		this.ftpEnabled = ftpEnabled;
	}

	public String getFtpUrl() {
		return ftpUrl;
	}

	public void setFtpUrl(String ftpUrl) {
		this.ftpUrl = ftpUrl;
	}

	public Map<FilmlistFormats, String> getFtpTargetFilePaths() {
		return ftpTargetFilePaths;
	}

	public void setFtpTargetFilePaths(Map<FilmlistFormats, String> ftpTargetFilePaths) {
		this.ftpTargetFilePaths = ftpTargetFilePaths;
	}

	public Integer getFtpPort() {
		return ftpPort;
	}

	public void setFtpPort(Integer ftpPort) {
		this.ftpPort = ftpPort;
	}

	public String getFtpUsername() {
		return ftpUsername;
	}

	public void setFtpUsername(String ftpUsername) {
		this.ftpUsername = ftpUsername;
	}

	public String getFtpPassword() {
		return ftpPassword;
	}

	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ftpEnabled == null) ? 0 : ftpEnabled.hashCode());
		result = prime * result + ((ftpPassword == null) ? 0 : ftpPassword.hashCode());
		result = prime * result + ((ftpPort == null) ? 0 : ftpPort.hashCode());
		result = prime * result + ((ftpTargetFilePaths == null) ? 0 : ftpTargetFilePaths.hashCode());
		result = prime * result + ((ftpUrl == null) ? 0 : ftpUrl.hashCode());
		result = prime * result + ((ftpUsername == null) ? 0 : ftpUsername.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MServerFTPSettings))
			return false;
		MServerFTPSettings other = (MServerFTPSettings) obj;
		if (ftpEnabled == null) {
			if (other.ftpEnabled != null)
				return false;
		} else if (!ftpEnabled.equals(other.ftpEnabled))
			return false;
		if (ftpPassword == null) {
			if (other.ftpPassword != null)
				return false;
		} else if (!ftpPassword.equals(other.ftpPassword))
			return false;
		if (ftpPort == null) {
			if (other.ftpPort != null)
				return false;
		} else if (!ftpPort.equals(other.ftpPort))
			return false;
		if (ftpTargetFilePaths == null) {
			if (other.ftpTargetFilePaths != null)
				return false;
		} else if (!ftpTargetFilePaths.equals(other.ftpTargetFilePaths))
			return false;
		if (ftpUrl == null) {
			if (other.ftpUrl != null)
				return false;
		} else if (!ftpUrl.equals(other.ftpUrl))
			return false;
		if (ftpUsername == null) {
			if (other.ftpUsername != null)
				return false;
		} else if (!ftpUsername.equals(other.ftpUsername))
			return false;
		return true;
	}
}
