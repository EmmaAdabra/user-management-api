package com.adb.usermanagementapi.config;

import com.adb.usermanagementapi.repository.LoginAttemptsRepository;
import com.adb.usermanagementapi.repository.LoginAttemptsRepositoryImpl;
import com.adb.usermanagementapi.repository.UserRepository;
import com.adb.usermanagementapi.repository.UserRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@Configuration
public class TestConfig {
    // adding h2 database bean
    @Bean
    public DataSource dataSource(){
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("schema.sql")
                .setName("userDb")
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public UserRepository userRepository(JdbcTemplate jdbcTemplate){
        return new UserRepositoryImpl(jdbcTemplate);
    }

    @Bean
    public LoginAttemptsRepository loginAttemptsRepository(JdbcTemplate jdbcTemplate){
        return new LoginAttemptsRepositoryImpl(jdbcTemplate);
    }
}
