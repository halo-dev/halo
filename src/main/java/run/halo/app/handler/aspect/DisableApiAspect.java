package run.halo.app.handler.aspect;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import run.halo.app.Application;
import run.halo.app.model.annotation.DisableApi;
import run.halo.app.model.enums.Mode;
import run.halo.app.model.support.BaseResponse;

/**
 * 自定义注解DisableApi的切面
 * @author guqing
 * @date 2020-02-14 14:08
 */
@Aspect
@Component
public class DisableApiAspect {
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @Pointcut("@annotation(run.halo.app.model.annotation.DisableApi)")
    public void pointcut(){}

    @Around("pointcut() && @annotation(disableApi)")
    public Object around(ProceedingJoinPoint joinPoint,
                         DisableApi disableApi){
        Mode mode = disableApi.mode();
        if(StringUtils.equalsIgnoreCase(mode.name(), activeProfile)) {
            return new BaseResponse<>(HttpStatus.FORBIDDEN.value(), "禁止访问",null);
        }
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return proceed;
    }
}
