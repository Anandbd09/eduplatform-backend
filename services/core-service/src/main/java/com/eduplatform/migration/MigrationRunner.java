package com.eduplatform.migration;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationRunner.class);

    private final DataSource dataSource;

    public MigrationRunner(ObjectProvider<DataSource> dataSourceProvider) {
        this.dataSource = dataSourceProvider.getIfAvailable();
    }

    @Override
    public void run(String... args) {
        try {
            log.info("Starting application migrations...");

            runSqlMigrationsIfConfigured();
            MigrationValidator.validateCollections();

            log.info("All migrations completed successfully");
        } catch (Exception e) {
            log.error("Migration failed", e);
            throw new RuntimeException("Database migration failed", e);
        }
    }

    private void runSqlMigrationsIfConfigured() {
        if (dataSource == null) {
            log.info("No SQL DataSource configured. Skipping SQL migrations.");
            return;
        }

        try {
            Class<?> flywayClass = Class.forName("org.flywaydb.core.Flyway");
            Object fluentConfiguration = flywayClass.getMethod("configure").invoke(null);
            Class<?> configurationClass = fluentConfiguration.getClass();

            Object configured = configurationClass
                    .getMethod("dataSource", DataSource.class)
                    .invoke(fluentConfiguration, dataSource);

            configured = configurationClass
                    .getMethod("locations", String[].class)
                    .invoke(configured, (Object) new String[]{"classpath:db/flyway"});

            Object flyway = configurationClass.getMethod("load").invoke(configured);
            Object migrationResult = flyway.getClass().getMethod("migrate").invoke(flyway);

            log.info("SQL migrations completed: {}", migrationResult);
        } catch (ClassNotFoundException e) {
            log.info("Flyway dependency is not present. Skipping SQL migrations.");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to execute Flyway migrations", e);
        }
    }
}
