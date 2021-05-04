package de.mediathekview.mserver.base.config;

import org.apache.logging.log4j.Level;

public class MServerLogSettingsDTO {
	private Level logLevelConsole;
	private Level logLevelFile;
	private Boolean logActivateConsole;
	private Boolean logActivateFile;
	private Boolean logActivateRollingFileAppend;
	private String logPatternConsole;
	private String logPatternFile;
	private String logFileSavePath;
	private String logFileRollingPattern;
	
	public MServerLogSettingsDTO() {
		super();
		
		logLevelConsole = Level.INFO;
		logLevelFile = Level.ERROR;
		logActivateConsole = true;
		logActivateFile = true;
		logActivateRollingFileAppend = true;
        logPatternConsole = "%d{HH:mm:ss.SSS} %highlight{%d [%t] %-5level: %msg%n%throwable}";
        logPatternFile = "%d{HH:mm:ss.SSS} %d [%t] %-5level: %msg%n%throwable";
        logFileSavePath = "logs/server.log";
        logFileRollingPattern = "logs/$${date:yyyy-MM}/server-%d{MM-dd-yyyy}-%i.log";
	}
	
	public Level getLogLevelConsole() {
		return logLevelConsole;
	}
	public void setLogLevelConsole(Level logLevelConsole) {
		this.logLevelConsole = logLevelConsole;
	}
	public Level getLogLevelFile() {
		return logLevelFile;
	}
	public void setLogLevelFile(Level logLevelFile) {
		this.logLevelFile = logLevelFile;
	}
	public Boolean getLogActivateConsole() {
		return logActivateConsole;
	}
	public void setLogActivateConsole(Boolean logActivateConsole) {
		this.logActivateConsole = logActivateConsole;
	}
	public Boolean getLogActivateFile() {
		return logActivateFile;
	}
	public void setLogActivateFile(Boolean logActivateFile) {
		this.logActivateFile = logActivateFile;
	}
	public Boolean getLogActivateRollingFileAppend() {
		return logActivateRollingFileAppend;
	}
	public void setLogActivateRollingFileAppend(Boolean logActivateRollingFileAppend) {
		this.logActivateRollingFileAppend = logActivateRollingFileAppend;
	}
	public String getLogPatternConsole() {
		return logPatternConsole;
	}
	public void setLogPatternConsole(String logPatternConsole) {
		this.logPatternConsole = logPatternConsole;
	}
	public String getLogPatternFile() {
		return logPatternFile;
	}
	public void setLogPatternFile(String logPatternFile) {
		this.logPatternFile = logPatternFile;
	}
	public String getLogFileSavePath() {
		return logFileSavePath;
	}
	public void setLogFileSavePath(String logFileSavePath) {
		this.logFileSavePath = logFileSavePath;
	}
	public String getLogFileRollingPattern() {
		return logFileRollingPattern;
	}
	public void setLogFileRollingPattern(String logFileRollingPattern) {
		this.logFileRollingPattern = logFileRollingPattern;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((logActivateConsole == null) ? 0 : logActivateConsole.hashCode());
		result = prime * result + ((logActivateFile == null) ? 0 : logActivateFile.hashCode());
		result = prime * result
				+ ((logActivateRollingFileAppend == null) ? 0 : logActivateRollingFileAppend.hashCode());
		result = prime * result + ((logFileRollingPattern == null) ? 0 : logFileRollingPattern.hashCode());
		result = prime * result + ((logFileSavePath == null) ? 0 : logFileSavePath.hashCode());
		result = prime * result + ((logLevelConsole == null) ? 0 : logLevelConsole.hashCode());
		result = prime * result + ((logLevelFile == null) ? 0 : logLevelFile.hashCode());
		result = prime * result + ((logPatternConsole == null) ? 0 : logPatternConsole.hashCode());
		result = prime * result + ((logPatternFile == null) ? 0 : logPatternFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MServerLogSettingsDTO))
			return false;
		MServerLogSettingsDTO other = (MServerLogSettingsDTO) obj;
		if (logActivateConsole == other.logActivateConsole &&
		    logActivateFile == other.logActivateFile &&
		    logActivateRollingFileAppend == other.logActivateRollingFileAppend &&
		    logFileRollingPattern == other.logFileRollingPattern &&
		    logFileSavePath == other.logFileSavePath &&
		    logLevelConsole == other.logLevelConsole &&
		    logLevelFile == other.logLevelFile &&
		    logPatternConsole == other.logPatternConsole &&
		    logPatternFile == other.logPatternFile){
		  return true;
		}
		return false;
	}

}
