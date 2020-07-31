/*
 * MediathekView
 * Copyright (C) 2020 A. Finkhaeuser
 */
package mServer.tool;


import de.mediathekview.mlib.tool.Log;

public class EnvManager {

    private final static EnvManager instance = new EnvManager();


    private static final String STRING_ENV_KEY_ENABLED = "METRIC_ENABLED";
    private static final String STRING_ENV_KEY_TELEGRAFURL = "METRIC_TELEGRAFURL";


    public boolean env_metric_enabled;

    public String env_metric_url;


    private EnvManager() {
        env_metric_enabled = isEnvSet(STRING_ENV_KEY_ENABLED);
        env_metric_url = getEnvValue(STRING_ENV_KEY_TELEGRAFURL);
    }

    public static EnvManager getInstance() {
        return instance;
    }

    private boolean isEnvSet(String envName) {
        String envvalue = System.getenv(envName);

        if(envvalue == null) return false;

        if(envvalue.equalsIgnoreCase("y")) return true;
        if(envvalue.equals("1")) return true;
        if(envvalue.equalsIgnoreCase("yes")) return true;
        if(envvalue.equalsIgnoreCase("true")) return true;

        return false;

    }

    private String getEnvValue(String envName) {
      String envvalue = System.getenv(envName);

      if(envvalue == null) return "";
      return envvalue;
    }

}
