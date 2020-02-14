package run.halo.app.handler.aspect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.model.annotation.DisableOnCondition;
import run.halo.app.model.enums.Mode;

/**
 * 自定义注解DisableApi的切面
 * @author guqing
 * @date 2020-02-14 14:08
 */
@Aspect
@Slf4j
@Component
public class DisableOnConditionAspect {
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Pointcut("@annotation(run.halo.app.model.annotation.DisableOnCondition)")
    public void pointcut(){}

    @Around("pointcut() && @annotation(disableApi)")
    public Object around(ProceedingJoinPoint joinPoint,
                         DisableOnCondition disableApi) throws Throwable {
        Mode mode = disableApi.mode();
        if(StringUtils.equalsIgnoreCase(mode.name(), activeProfile)) {
            throw new ForbiddenException("禁止访问");
        }

        return joinPoint.proceed();
    }
}
