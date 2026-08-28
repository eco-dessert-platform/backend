package com.bbangle.bbangle.common.aop;

import com.bbangle.bbangle.config.logging.MethodExecutionTimeContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 계층별 메서드 실행 시간 측정용 AOP
 */
@Aspect
@Component
public class LayerExecutionTimeAspect {

    @Pointcut("execution(* com.bbangle..*Controller.*(..))")
    public void controllerLayer() {}

    @Pointcut("execution(* com.bbangle..*Facade.*(..))")
    public void facadeLayer() {}

    // ResponseService의 경우 Record 타입이므로 AOP 불가능
    @Pointcut("""
    execution(* com.bbangle..*Service.*(..))
    && !within(com.bbangle.bbangle.common.service.ResponseService)
""")
    public void serviceLayer() {}

    @Pointcut("execution(* com.bbangle..*Repository.*(..))")
    public void repositoryLayer() {}

    @Around("controllerLayer() || facadeLayer() || serviceLayer() || repositoryLayer()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            MethodExecutionTimeContext.add(className + "." + methodName + "() - " + elapsed + "ms");
        }
    }
}
