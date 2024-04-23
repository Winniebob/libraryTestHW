package com.example.library.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TimerAspect {

    @Pointcut("within(@com.example.library.aspect.Timer *)")
    public void beansAnnotatedWith() {

    }

    @Pointcut("@annotation(com.example.library.aspect.Timer)")
    public void methodsAnnotatedWith() {

    }

    @Around("beansAnnotatedWith() || methodsAnnotatedWith()")
    public Object timerAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            long start = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            //  в лог записать следующее: className - methodName #(количество секунд выполнения)
            log.info(joinPoint.getTarget().getClass().getName() + " - " + joinPoint.getSignature().getName() + " #" +
                    elapsedTime * 0.001 + " sec.");
            return result;
        } catch (Throwable e) {
            log.error("exception: [{}, {}]", e.getClass(), e.getMessage());
            throw e;
        }
    }
}