package de.mediathekview.mserver.base.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public final class GPDataSourceProvider {

    private static final HikariDataSource DATA_SOURCE;

    static {
        HikariConfig cfg = new HikariConfig();

        // === JDBC ===
        cfg.setJdbcUrl("jdbc:postgresql://OscarDS:55432/crawler");
        cfg.setUsername("crawler");
        cfg.setPassword("secret");

        // === Pool Sizing (wichtig!) ===
        cfg.setMaximumPoolSize(16);     // Sweet Spot für 10k+/min
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

    private GPDataSourceProvider() {
        // no instances
    }

    public static DataSource get() {
        return DATA_SOURCE;
    }

    public static void shutdown() {
        DATA_SOURCE.close();
    }
}
