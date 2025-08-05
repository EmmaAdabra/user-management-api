package com.adb.usermanagementapi.util;

import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

public class DatabaseUtil {
    public static DataSource initializeSchema(DataSource dataSource, String schemaPath, Logger logger) {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource(schemaPath));
            populator.setContinueOnError(false);

            logger.debug("Running DB schema.sql...");
            DatabasePopulatorUtils.execute(populator, dataSource);
            logger.info("Completed DB script.");
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause(); // Unwrap nested causes
            }
            logger.error("❌ Error loading schema.sql", root);
        }
        return dataSource;
    }
}
