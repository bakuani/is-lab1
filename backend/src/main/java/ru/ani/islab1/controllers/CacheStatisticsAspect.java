package ru.ani.islab1.controllers;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheStatisticsAspect {

    private final EntityManagerFactory entityManagerFactory;

    @Value("${app.cache.logging-enabled:false}")
    private boolean loggingEnabled;

    @Around("execution(* ru.ani.islab1.services.StudyGroupService.getById(..)) || " +
            "execution(* ru.ani.islab1.services.StudyGroupService.getAll(..))")
    public Object logCacheStatistics(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingEnabled) {
            return joinPoint.proceed();
        }

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();

        long hitCountBefore = stats.getSecondLevelCacheHitCount();
        long missCountBefore = stats.getSecondLevelCacheMissCount();

        Object result = joinPoint.proceed();

        long hitCountAfter = stats.getSecondLevelCacheHitCount();
        long missCountAfter = stats.getSecondLevelCacheMissCount();

        log.info("L2 Cache Stats for {}: Hits: +{}, Misses: +{}",
                joinPoint.getSignature().getName(),
                (hitCountAfter - hitCountBefore),
                (missCountAfter - missCountBefore));

        return result;
    }
}