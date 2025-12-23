package de.mediathekview.mserver.base.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import de.mediathekview.mserver.base.config.MServerConfigManager;

import javax.sql.DataSource;

public final class PostgreSQLDataSourceProvider {
  private static HikariDataSource DATA_SOURCE;
  private static Boolean enabled = false;
  private MServerConfigManager aMServerConfigManager;

  public PostgreSQLDataSourceProvider(MServerConfigManager aMServerConfigManager) {
    this.aMServerConfigManager = aMServerConfigManager;
    init();
  }

  public static boolean isEnabled() {
    return enabled;
  }
  
  public static DataSource get() {
      return DATA_SOURCE;
    }

  public static void shutdown() {
    DATA_SOURCE.close();
  }

  private void init() {
    HikariConfig cfg = new HikariConfig();
    enabled = aMServerConfigManager.getConfig().getMServerDBConfig().getActive();
    if(!enabled) {
      return;
    }
    cfg.setJdbcUrl(aMServerConfigManager.getConfig().getMServerDBConfig().getUrl());
    cfg.setUsername(aMServerConfigManager.getConfig().getMServerDBConfig().getUsername());
    cfg.setPassword(aMServerConfigManager.getConfig().getMServerDBConfig().getPassword());

    // === Pool Sizing (wichtig!) ===
    cfg.setMaximumPoolSize(16); // Sweet Spot für 10k+/min
    cfg.setMinimumIdle(4);

    // === Performance ===
    cfg.setAutoCommit(true);
    cfg.setConnectionTimeout(3000);
    cfg.setIdleTimeout(600_000);
    cfg.setMaxLifetime(1_800_000);

    // === PostgreSQL Optimierungen ===
    cfg.addDataSourceProperty("reWriteBatchedInserts", "true");
    cfg.addDataSourceProperty("stringtype", "unspecified");

    // === Debug (optional) ===
    cfg.setPoolName("CrawlerPool");

    DATA_SOURCE = new HikariDataSource(cfg);
  }
}
