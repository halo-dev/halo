package run.halo.app.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.security.context.SecurityContextHolder;


/**
 * @author giveup
 * @description SensitiveMaskAspect
 * @date 10:22 PM 25/5/2020
 */
@Aspect
@Component
public class SensitiveConcealAspect {


    @Pointcut("@annotation(run.halo.app.annotation.SensitiveConceal)")
    public void pointCut() {
    }

    private Object sensitiveMask(Object comment) {
        if (comment instanceof BaseComment) {
            ((BaseComment) comment).setEmail("");
            ((BaseComment) comment).setIpAddress("");
        }
        return comment;
    }


    @Around("pointCut()")
    public Object mask(ProceedingJoinPoint joinPoint) throws Throwable {

        Object result = joinPoint.proceed();

        if (SecurityContextHolder.getContext().isAuthenticated()) {

            return result;

        }

        if (result instanceof Iterable) {

            ((Iterable<?>) result).forEach(this::sensitiveMask);

        }

        return sensitiveMask(result);

    }


}
