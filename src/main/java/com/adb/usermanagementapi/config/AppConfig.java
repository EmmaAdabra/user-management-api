package com.adb.usermanagementapi.config;

import com.adb.usermanagementapi.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@PropertySource("classPath:application.properties")
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.adb.usermanagementapi")
public class AppConfig {
    @Autowired
    private Environment env;
    static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    @Bean
    public DataSource sqliteDatasource(){
        Path dbDir = Paths.get(env.getProperty("db.dir"));
        String dbFile = env.getProperty("db.path");

         //Create db folder if it doesn't exist
        try {
            Files.createDirectories(dbDir);
            logger.info("created database dir - {}", dbDir.toAbsolutePath());
        } catch (IOException ex){
            logger.warn("Fail to create database dir at - {}", dbDir.toAbsolutePath());
        }

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:" + dbFile);
        ds.setUsername("");
        ds.setPassword("");

        //load schema.sql
        return DatabaseUtil.initializeSchema(ds, env.getProperty("db.schema"), logger);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource sqliteDatasource){
        return new JdbcTemplate(sqliteDatasource);
    }
}
