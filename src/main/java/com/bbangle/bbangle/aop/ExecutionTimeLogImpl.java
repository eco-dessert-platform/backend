package com.bbangle.bbangle.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Slf4j
@Component
public class ExecutionTimeLogImpl {

    @Around("@annotation(ExecutionTimeLog)")
    public Object assumeExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        StopWatch stopWatch = new StopWatch(signature.getMethod().getName());
        stopWatch.start();
        String methodName = signature.getMethod().getName();
        Object result = joinPoint.proceed();
        stopWatch.stop();
        log.info("Method Name : {}", methodName);
        log.info("Execution Time : {}", stopWatch.prettyPrint());
        return result;
    }

}
