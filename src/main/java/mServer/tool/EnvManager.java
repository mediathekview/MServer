/*
 * MediathekView
 * Copyright (C) 2020 A. Finkhaeuser
 */
package mServer.tool;


public class EnvManager {

    private static final EnvManager instance = new EnvManager();


    private static final String STRING_ENV_KEY_ENABLED = "METRIC_ENABLED";
    private static final String STRING_ENV_KEY_TELEGRAFURL = "METRIC_TELEGRAFURL";


    public boolean envMetricEnabled;

    public String envMetricUrl;


    private EnvManager() {
        envMetricEnabled = isEnvSet(STRING_ENV_KEY_ENABLED);
        envMetricUrl = getEnvValue(STRING_ENV_KEY_TELEGRAFURL);
    }

    public static EnvManager getInstance() {
        return instance;
    }

    private boolean isEnvSet(String envName) {
        String envvalue = System.getenv(envName);
        
        return envvalue != null && (
            envvalue.equalsIgnoreCase("y")
            || envvalue.equals("1")
            || envvalue.equalsIgnoreCase("yes")
            || envvalue.equalsIgnoreCase("true")
        );
    }

    private String getEnvValue(String envName) {
      String envvalue = System.getenv(envName);

      if(envvalue == null) return "";
      return envvalue;
    }

}
