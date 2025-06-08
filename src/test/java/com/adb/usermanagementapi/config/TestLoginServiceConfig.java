package com.adb.usermanagementapi.config;


import com.adb.usermanagementapi.mapper.UserMapper;
import com.adb.usermanagementapi.repository.LoginAttemptsRepository;
import com.adb.usermanagementapi.repository.UserRepository;
import com.adb.usermanagementapi.service.security.LoginService;
import com.adb.usermanagementapi.service.security.LoginServiceImpl;
import com.adb.usermanagementapi.service.security.PasswordValidator;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class TestLoginServiceConfig {
    @Bean
    public LoginAttemptsRepository loginAttemptsRepository(){
        return mock(LoginAttemptsRepository.class);
    }

    @Bean
    public UserRepository userRepository(){
        return mock(UserRepository.class);
    }

    @Bean
    public UserMapper userMapper(){
        return mock(UserMapper.class);
    }

    @Bean
    public PasswordValidator passwordValidator(){
        return mock(PasswordValidator.class);
    }

    @Bean
    public LoginService loginService(
            LoginAttemptsRepository loginAttemptsRepository,
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordValidator passwordValidator
            ){
        return new LoginServiceImpl(loginAttemptsRepository, userRepository, userMapper, passwordValidator);
    }
}
