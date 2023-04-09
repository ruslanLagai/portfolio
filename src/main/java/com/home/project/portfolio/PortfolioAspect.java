package com.home.project.portfolio;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author rlagay
 */
@Aspect
@Component
@Slf4j
public class PortfolioAspect {

    @Getter
    private final Semaphore semaphore = new Semaphore(0);

    @Around("execution (* com.home.project.portfolio.processor.PortfolioDistributionProcessorImpl.apply(*, *, *))")
    public Object afterShares(ProceedingJoinPoint proceedingJoinPoint) {
        try {
            if (semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                return proceedingJoinPoint.proceed();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
