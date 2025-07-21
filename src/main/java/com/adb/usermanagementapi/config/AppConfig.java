package com.adb.usermanagementapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.io.File;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.adb.usermanagementapi")
public class AppConfig {
    static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    @Bean
    public DataSource sqliteDatasource(){
        String dbDir = "data";
        String dbFile = dbDir + "/app_database.db";

        File folder = new File(dbDir);

         //Create db folder if it doesn't exist
        if(!folder.exists()){
            folder.mkdirs();
        }

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:" + dbFile);
        ds.setUsername("");
        ds.setPassword("");

         //load schema.sql
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.setContinueOnError(false);

            logger.debug("Running DB schema.sql...");
            DatabasePopulatorUtils.execute(populator, ds);
            System.out.println("Completed DB script.");
            logger.info("Completed DB script.");
        } catch (Exception e) {
            Throwable root = e;

            while (root.getCause() != null){
                root = root.getCause(); // Unwrap nested causes
            }
            logger.error("❌ Error loading schema.sql", root);
        }

        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource sqliteDatasource){
        return new JdbcTemplate(sqliteDatasource);
    }
}
