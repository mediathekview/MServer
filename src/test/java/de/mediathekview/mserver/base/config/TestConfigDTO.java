package de.mediathekview.mserver.base.config;


public class TestConfigDTO implements ConfigDTO {

	private String valueWithDefault = "DefaultValue";
	private String valueWithoutDefault;

	public String getValueWithDefault() {
		return valueWithDefault;
	}

	public String getValueWithoutDefault() {
		return valueWithoutDefault;
	}
}