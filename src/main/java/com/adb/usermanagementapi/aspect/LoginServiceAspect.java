package com.adb.usermanagementapi.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoginServiceAspect {
    private final Logger logger = LoggerFactory.getLogger(LoginServiceAspect.class);

    @Around("execution(* com.adb.usermanagementapi.service.security.LoginServiceImpl.*(..))")
    public Object logLoginServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String methodName = signature.getDeclaringType().getSimpleName() + "." +
                signature.getName();
        Object [] args = joinPoint.getArgs();
        String arguments = Arrays.toString(args);

        logger.info("Entering {} with args {}", methodName, arguments);

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Existing {} with {} (took {}ms)", methodName, result, duration);

            return result;
        } catch (Exception e){
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Exception caught in {} after {}ms - {}: {}", methodName,
                    duration, e.getClass().getSimpleName(), e.getMessage());

            throw e;
        }
    }
}
