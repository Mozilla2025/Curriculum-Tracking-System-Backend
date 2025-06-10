package com.mozilla.curriculum_tracking_system.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class CacheMonitoringAspect {

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object monitorCacheableMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();

            log.debug("Cacheable method {}#{} executed in {}ms with args: {}",
                    className, methodName, (endTime - startTime), Arrays.toString(args));

            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Cacheable method {}#{} failed in {}ms with args: {}",
                    className, methodName, (endTime - startTime), Arrays.toString(args), e);
            throw e;
        }
    }

    @Around("@annotation(org.springframework.cache.annotation.CacheEvict)")
    public Object monitorCacheEvictMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();

            log.debug("Cache evict method {}#{} executed in {}ms with args: {}",
                    className, methodName, (endTime - startTime), Arrays.toString(args));

            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Cache evict method {}#{} failed in {}ms with args: {}",
                    className, methodName, (endTime - startTime), Arrays.toString(args), e);
            throw e;
        }
    }
}
