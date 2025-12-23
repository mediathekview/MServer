package de.mediathekview.mserver.ui.config;

import java.util.HashMap;
import java.util.Map;

public class MServerCommandLine {
  public enum CMDARG {
    config,
    gconf,
    flow,
    topicsSearchEnabled,
    invalid;
    
    public static CMDARG from(String key) {
      try {
          return CMDARG.valueOf(key);
      } catch (IllegalArgumentException e) {
          return CMDARG.invalid;
      }
    }
  }
  
  public static void print() {
    System.err.println("Call --config abc.yaml --gconf --flow abc,def,ghi ");
  }
  static boolean validateArgs(String[] args) {
    if(!args[0].startsWith("--")) {
      System.err.println("must start with --");
      return false;
    }
    for (int index = 0; index < args.length; index++) {
      if(!args[index].startsWith("--") && args.length > index+1 && !args[index+1].startsWith("--")) {
        return false;
      }
    }
    Map<CMDARG, String> enumArgs = parseArgs(args);
    if (enumArgs.containsKey(CMDARG.invalid)) {
      return false;
    }
    return true;
 }

  
  // TODO: replace me with Apache Commons CLI
  // Usage aba.jar --input data.json --limit 50 --verbose
  static Map<CMDARG, String> parseArgs(String[] args) {
    Map<CMDARG, String> map = new HashMap<>();
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("--")) {
        String key = args[i].substring(2);
        CMDARG enumKey = CMDARG.from(key);
        if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
          map.put(enumKey, args[++i]);
        } else {
          map.put(enumKey, "true");
        }
      }
    }
    return map;
  }
  
}
