package com.home.project.portfolio.aop;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

/**
 * @author rlagay
 */
@Aspect
@Component
@Slf4j
public class PortfolioAspect {

    @Getter
    private final Semaphore semaphore = new Semaphore(1);

    @Getter
    private final Semaphore semaphore1 = new Semaphore(1);

    @Around("execution (* com.home.project.portfolio.processor.PortfolioDistributionProcessorImpl.apply(*, *, *))")
    public Object afterShares(ProceedingJoinPoint proceedingJoinPoint) {
        Object result = null;
        try {
            semaphore.acquire();
            result = proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
        return result;
    }

    @Around("execution (* com.home.project.portfolio.processor.AccountProcessorImpl.apply(*, *, *))")
    public Object beforeDistribution(ProceedingJoinPoint proceedingJoinPoint) {
        Object result = null;
        try {
            semaphore.acquire();
            result = proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
            semaphore.release();
        }
        return result;
    }
}
