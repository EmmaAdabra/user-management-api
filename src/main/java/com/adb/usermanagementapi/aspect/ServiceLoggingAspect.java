package com.adb.usermanagementapi.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;


@Component
@Aspect
public class ServiceLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    @Around("execution(* com.adb.usermanagementapi.service.UserServiceImpl.*(..))")
    public Object logUserServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName =
                signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        Object [] args = joinPoint.getArgs();
        String arguments = Arrays.toString(args);

        logger.info("Entering {} with args: {}", methodName, arguments);

        Long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            Long duration = System.currentTimeMillis() - startTime;
            logger.info("Existing {} with {} (took {}ms)", methodName, result, duration);

            return  result;
        } catch (Exception e){
            Long duration = System.currentTimeMillis() - startTime;
            logger.info("Exception in method {} after {}ms - {}: {}", methodName,
                    duration, e.getClass().getSimpleName(), e.getMessage());

            throw e;
        }
    }
}
